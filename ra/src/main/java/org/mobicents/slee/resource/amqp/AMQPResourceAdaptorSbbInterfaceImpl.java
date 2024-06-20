package org.mobicents.slee.resource.amqp;

public class AMQPResourceAdaptorSbbInterfaceImpl implements
		AMQPResourceAdaptorSbbInterface {
	
	
	private final AMQPResourceAdaptor ra;
	
	
	

	public AMQPResourceAdaptorSbbInterfaceImpl(AMQPResourceAdaptor ra) {
		super();
		this.ra = ra;
	}




	@Override
	public AMQPActivity createActivity() {
		if (ra == null) {
			throw new IllegalStateException("RA entity not active");
		}
		return ra.createActivity();
	}

}
