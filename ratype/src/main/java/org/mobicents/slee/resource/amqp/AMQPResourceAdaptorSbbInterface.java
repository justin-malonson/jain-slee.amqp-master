package org.mobicents.slee.resource.amqp;

import javax.slee.resource.ResourceAdaptorTypeID;

public interface AMQPResourceAdaptorSbbInterface {
	
	/**
	 * the ID of the RA Type
	 */
	public static final ResourceAdaptorTypeID RATYPE_ID = new ResourceAdaptorTypeID(
			"AMQPResourceAdaptorType", "org.mobicents", "1.0");
	
	/**
	 * Creates a new {@link AMQPActivity}.
	 * 
	 * @return
	 */
	public AMQPActivity createActivity();
	
	

}
