package org.mobicents.slee.resource.amqp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.slee.*;
import javax.slee.facilities.*;
import javax.slee.resource.*;

/**
 * 
 * @author akrem benmarzouk
 * @author helmi benabdallah
 * 
 */

public class AMQPResourceAdaptor implements ResourceAdaptor {

	
	// Config Property values
	private String amqpHost;
	private Integer amqpPort;
	private String username;
	private String password;
	private String amqpQeueName ;
	private String amqpExchangeName;
	
	
	
	AMQPListener amqpListener;
	private static final AtomicInteger instance = new AtomicInteger();

	private Map<AMQPID, AMQPHandler> connectionMap;
	private FireableEventType amqpEventID;
	

	/**
	 * The ResourceAdaptorContext interface is implemented by the SLEE. It 
	 * provides the Resource Adaptor with the required capabilities in the SLEE
	 * to execute its work. The ResourceAdaptorContext object holds references
	 * to a number of objects that are of interest to many Resource Adaptors.
	 * A resource adaptor object is provided with a ResourceAdaptorContext
	 * object when the setResourceAdaptorContext method of the ResourceAdaptor
	 * interface is invoked on the resource adaptor object.
	 */
	private ResourceAdaptorContext raContext;

	/**
	 * The SLEE Endpoint defines the interface used by a Resource Adaptor to
	 * start and end Activities within the SLEE, fire Events on those
	 * Activities, and to suspend an Activity. A SLEE must implement the
	 * SLEEEndpoint interface to be both multi-thread safe and reentrant safe.
	 */
	private SleeEndpoint sleeEndpoint;
	
	/**
	 * Resource Adaptors access the Event Lookup Facility through an
	 * EventLookupFacility object that implements the EventLookupFacility
	 * interface. An EventLookupFacility object can be obtained by a resource
	 * adaptor entity via its ResourceAdaptorContext.
	 */
	private EventLookupFacility eventLookupFacility;
	
	/**
	 * A tracer is represented in the SLEE by the Tracer interface.
	 * Notification sources access the Tracer Facility through a Tracer object
	 * that implements the Tracer interface. A Tracer object can be obtained by
	 * SBBs via the SbbContext interface, by resource adaptor entities via the
	 * ResourceAdaptorContext interface, and by profiles via the ProfileContext
	 * interface.
	 */
	private Tracer tracer;

	/**
	 * A Marshaler is used by the SLEE to convert Events and Activity Handles
	 * between object and marshaled forms. If a Resource Adaptor does not 
	 * provide an implementation of the Marshaler interface then the Resource 
	 * Adaptor is not permitted to use the SLEE_MAY_MARSHAL flag of the 
	 * ActivityFlags and EventFlags class when starting Activities or firing
	 * Events.
	 */
	private Marshaler marshaler;

	/**
	 * for all events we are interested in knowing when the event failed to be processed
	 */
	public static final int DEFAULT_EVENT_FLAGS = EventFlags.REQUEST_PROCESSING_FAILED_CALLBACK;

	public static final int UNREFERENCED_EVENT_FLAGS = EventFlags.setRequestEventReferenceReleasedCallback(DEFAULT_EVENT_FLAGS);

	public static final int NON_MARSHABLE_ACTIVITY_FLAGS = ActivityFlags.REQUEST_ENDED_CALLBACK;//.NO_FLAGS;
	
	public static final int MARSHABLE_ACTIVITY_FLAGS = ActivityFlags.setSleeMayMarshal(NON_MARSHABLE_ACTIVITY_FLAGS);
	
	// The internal state of the resource adaptor
	private static final int STATE_UNCONFIGURED = 0;
	private static final int STATE_INACTIVE = 1;
	private static final int STATE_ACTIVE = 2;
	private int state = STATE_UNCONFIGURED;
	private AMQPResourceAdaptorSbbInterfaceImpl sbbInterface = new AMQPResourceAdaptorSbbInterfaceImpl(this);
		
	/**
	 * Default constructor
	 */
	public AMQPResourceAdaptor() {
		
		
	}

	// Lifecycle methods ------------------------------------------------------

	public void setResourceAdaptorContext(ResourceAdaptorContext context) {
		this.raContext = context;
		this.tracer = context.getTracer(AMQPResourceAdaptor.class.getSimpleName());
		this.sleeEndpoint = context.getSleeEndpoint();
		this.eventLookupFacility = context.getEventLookupFacility();
	}

	public void unsetResourceAdaptorContext() {
		this.raContext = null;
		this.tracer = null;
		this.sleeEndpoint = null;
		this.eventLookupFacility = null;
	}

	private synchronized void setState(int newState) {
		state = newState;
	}
	
	private synchronized int getState() {
		return state;
	}
	
	public void raConfigure(ConfigProperties properties) {
		
		marshaler = new AMQPMarshaler();
		connectionMap = new HashMap<AMQPID, AMQPHandler>();
		
		
		// Get configuration property
		amqpHost = (String) properties.getProperty("amqpHost").getValue();
		amqpPort = (Integer) properties.getProperty("amqpPort").getValue();
		username = (String) properties.getProperty("username").getValue();
		password = (String) properties.getProperty("password").getValue();
		amqpQeueName = (String) properties.getProperty("amqpQeueName").getValue();
		amqpExchangeName = (String) properties.getProperty("amqpExchangeName").getValue();
		
		
		
		EventTypeID amqpEventType = new EventTypeID("AMQPEvent", "org.mobicents", "1.0");
		try {
			amqpEventID = raContext.getEventLookupFacility().getFireableEventType(amqpEventType);
			tracer.info("created amqpEventID " + amqpEventID);
			setState(STATE_INACTIVE);
		} catch (UnrecognizedEventException uee) {
			tracer.severe("No event ID found for " + amqpEventType	+ ", cannot initialise");
		}

		if (tracer.isFineEnabled()) {
			tracer.fine("Amqp Resource Adaptor configured.");
		}
	}
	
	public void raUnconfigure() {
		//release any system resources it has allocated in the
		//corresponding raConfigure method.
		if (tracer.isFineEnabled())
            tracer.fine("raUnconfigure");
		
		setState(STATE_UNCONFIGURED);
		
		if (tracer.isFineEnabled()) {
			tracer.fine("AMQP Resource Adaptor unconfigured.");
		}
	}
	
	public void raActive() {
		// allocate any system resources required to allow the resource
		// adaptor to interact with the underlying resource.
		// allocate any system resources required to allow the resource
		// adaptor to interact with the underlying resource.
		
		tracer.fine("Amqp Resource Adaptor raActive.");
		if (getState() != STATE_INACTIVE) {
			tracer.warning("Initialisation failed, not starting");
			return;
		}

		try{
			
			amqpListener = new AMQPListener (tracer, this, amqpHost, amqpPort, username, password, amqpQeueName);
			amqpListener.start();
			setState(STATE_ACTIVE);
		}
		catch (Exception e) {
            tracer.warning("Unable to activate RA entity", e);
        }
		

		if (tracer.isFineEnabled()) {
			tracer.fine("Amqp Resource Adaptor entity active.");
		}
	}		

	public void raStopping() {
		//update any internal state required such that it does not
		//attempt to start any new Activities once this method returns

		tracer.info("Amqp Resource Adaptor raStopping.");
		if (getState() != STATE_ACTIVE)
			return;

		// Shutdown the listener so that no new connections can be created
		tracer.info("stopping - no new activities will be created");
		// amqpListener.close();
		amqpListener.close();

		if (tracer.isFineEnabled()) {
			tracer.fine("Amqp Resource Adaptor entity stopping.");
		}
//		Runtime.getRuntime().addShutdownHook(new Thread() {
//			@Override
//			public void run() {
//				System.out.println("Shutting down BigOperationWorker");
//				container.shutdown();
//			}
//		});
	}

	public void raInactive() {
		//deallocate any system resources allocated to allow the resource
		//adaptor to interact with the underlying resource.
		amqpListener.close();
		connectionMap.clear();
		
		if (tracer.isFineEnabled()) {
			tracer.fine("AMQP Resource Adaptor entity inactive.");
		}	   
	}

	// Configuration Management -----------------------------------------------

	public void raVerifyConfiguration(ConfigProperties properties) throws InvalidConfigurationException {
		// TODO: verify that the configuration properties specified by the
		//       Administrator for a resource adaptor entity of the Resource
		//       Adaptor are valid. During this method the invoked resource
		//       adaptor object should inspect the configuration properties it
		//       is provided with to determine their validity. 
		//       If the configuration properties are considered valid, this
		//       method should return silently. If the configuration properties
		//       are considered invalid, this method should throw an 
		//       InvalidConfigurationException with an appropriate message.

		if (tracer.isFineEnabled()) {
			tracer.fine("AMQP Resource Adaptor configuration verified.");
		}	   
	}

	public void raConfigurationUpdate(ConfigProperties properties) {
		amqpHost = (String) properties.getProperty("amqpHost").getValue();
		amqpPort = (Integer) properties.getProperty("amqpPort").getValue();
		amqpQeueName = (String) properties.getProperty("amqpQeueName").getValue();
		amqpExchangeName = (String) properties.getProperty("amqpExchangeName").getValue();		
	}

	// Interface Access -------------------------------------------------------

	public Object getResourceAdaptorInterface(String className) {
//		if("org.springframework.amqp.core.AmqpAdmin".equals(className)) {
//			//TODO : return here
//			//return new AmqpAdminImpl();
			return this.sbbInterface;
//		}

//		return null;
	}

	public Marshaler getMarshaler() {
		return marshaler; // return null if no marshaler is to be used.
	}   

	// Event Filter -----------------------------------------------------------

	public void serviceActive(ReceivableService receivableService) {
		// TODO: update any internal state related to event filters as required
	}

	public void serviceStopping(ReceivableService receivableService) {
		// TODO: update any internal state related to event filters as required
	}

	public void serviceInactive(ReceivableService receivableService) {
		// TODO: update any internal state related to event filters as required
	}

	// Mandatory Callbacks ----------------------------------------------------

	public void queryLiveness(ActivityHandle handle) {
		// TODO: check the underlying resource to see if the Activity is still
		//       active. For example, it may be possible that ending of an 
		//       Activity did not occur due to message loss in the SLEE. In 
		//       this case the SLEE would retain the Activity Context of the
		//       Activity and SBBs attached to the Activity Context. If the 
		//       Activity is not alive the Resource Adaptor is expected to end
		//       the activity (via the SleeEndpoint interface). If the Activity
		//       is still alive the Resource Adaptor is not expected to do
		//       anything.
	}

	public Object getActivity(ActivityHandle handle) {
		//return a non-null object, to provide access to the Activity
		//object for an Activity Handle.
		synchronized (connectionMap) {
			return connectionMap.get(handle);
		}
	}
	
	public ActivityHandle getActivityHandle(Object activity) {
		//return an Activity Handle for an Activity object.
		tracer.info("AMQP Resource Adaptor getActivityHandle"
				+ activity.toString());

		synchronized (connectionMap) {
			return ((AMQPHandler) activity).getAmqpID();
		}
	}

	public void administrativeRemove(ActivityHandle handle) {
		// TODO: remove any internal state related to the Activity.
		//       Additionally may perform a protocol level operation to clean
		//       up any protocol-peer related state.
	}

	// Optional Callbacks -----------------------------------------------------

	public void eventProcessingSuccessful(ActivityHandle handle, FireableEventType eventType, Object event, Address address, ReceivableService service, int flags) {
		// used to inform the resource adaptor object that the specified Event 
		// was processed successfully by the SLEE.
		tracer.fine("eventProcessingSuccessful: ah=" + handle + 
				", eventType=" + eventType + ", event=" + event + ", flags=" + flags);
		if ((flags & EventFlags.SBB_PROCESSED_EVENT) == 0) {
			try {
				tracer.fine("RA: No SBBs active!");
			} catch (Exception e) {
				tracer.warning("error sending message", e);
			}
		}
	}

	public void eventProcessingFailed(ActivityHandle handle, FireableEventType eventType, Object event, Address address, ReceivableService service, int flags, FailureReason reason) {
		// used to inform the resource adaptor object that the specified Event 
		// could not be processed successfully by the SLEE..

				tracer.fine("eventProcessingFailed: ah=" + handle + ", eventType="
						+ eventType + ", event=" + event + ", flags=" + flags
						+ ", reason=" + reason);
				AMQPHandler amqp = (AMQPHandler) getActivity(handle);
				try {
					amqp.convertAndSend("RA: Event failed!");
				} catch (Exception e) {
					tracer.warning("error sending message", e);
				}
	}

	public void eventUnreferenced(ActivityHandle handle, FireableEventType eventType, Object event, Address address, ReceivableService service, int flags) {
		// used to inform the Resource Adaptor that the SLEE no longer
		// references an Event object which was previously fired by the
		// resource adaptor object.
	}

	public void activityEnded(ActivityHandle handle) {
		//release any resources related to this Activity as the SLEE
		//will not ask for it again.
		tracer.info("AMQP Resource Adaptor activityEnded" + handle.toString());
		synchronized (connectionMap) {
			connectionMap.remove(handle);
		}
		tracer.info("activity ended");
	}

	public void activityUnreferenced(ActivityHandle handle) {
		// TODO: decide to end the Activity at this time if desired.
	}

	
	// AMQP Resource Adaptor specific logic.
	
	public void publishEvent(AMQPHandler amqpHandler, String message) {
		// TODO Auto-generated method stub
		try {
			tracer.info("data received, firing message event");
			AMQPEventImpl event = new AMQPEventImpl(message);
			int flags = EventFlags.REQUEST_PROCESSING_SUCCESSFUL_CALLBACK
					| EventFlags.REQUEST_PROCESSING_FAILED_CALLBACK;
			
			
			sleeEndpoint.fireEvent(amqpHandler.getAmqpID(),
					amqpEventID, event, null, null, flags);

			tracer.info("AMQP  Resource Adaptor fire events "
					+ amqpHandler.getAmqpID().toString());
		} catch (Exception e) {
			tracer.warning("Error firing event to SLEE", e);
		}

	}
	
	public Object getResourceAdaptorContext() {
		// TODO Auto-generated method stub
		return this.raContext;
	}

	public Tracer getTracer(String name) {
		// TODO Auto-generated method stub
		return this.tracer;
	}

	public void connectionClosed(AMQPID id) {
		tracer.info("connection closed, ending activity");
		// Submit an activity end event - but only if we are not already deactivated
		if (getState() == STATE_ACTIVE) {
			try {
				// The SLEE will clean up and then call activityEnded() - we clean up our connection map
				sleeEndpoint.endActivity(id);
				
			} catch (UnrecognizedActivityHandleException uahe) {
				// ignore - the activity may have already ended via an administrative remove
			}
		}
		
	}

	public void connectionOpened(AMQPHandler amqpHandler) {
		synchronized (connectionMap) {
			connectionMap.put(amqpHandler.getAmqpID(), amqpHandler);
		}
		tracer.info("connection opened, starting activity");
		// Tell the SLEE about the new activity
		try {
			sleeEndpoint
			.startActivity(amqpHandler.getAmqpID(), amqpHandler);
		} catch (StartActivityException e) {
			tracer.warning("Failed to start the activity: " + e.getMessage(), e);
		}
		
	}

	public AMQPActivity createActivity() {
		AMQPHandler activity = new AMQPHandler(tracer, this, 
				new AMQPID(UUID.randomUUID().toString()), 
				amqpListener.cf, amqpListener.container, amqpListener.queueName);
		
		try{
			sleeEndpoint.startActivitySuspended(activity, activity, ActivityFlags.SLEE_MAY_MARSHAL);
		}catch (Exception e){
			tracer.severe("failed to start activity " + activity.getId(), e);
		}
		return activity;
	}
	

	/**
	 * 
	 * @param activity
	 */
	void endActivity(AMQPHandler activity) {
		try {
			sleeEndpoint.endActivityTransacted(activity);
		} catch (Exception e) {
			tracer.severe("failed to end activity", e);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public ResourceAdaptorContext getContext() {
		return this.raContext;
	}

}
