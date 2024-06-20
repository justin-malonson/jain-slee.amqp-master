package org.mobicents.slee.resource.amqp;


import javax.slee.ActivityContextInterface;
import javax.slee.FactoryException;
import javax.slee.UnrecognizedActivityException;
import javax.slee.resource.ResourceAdaptorTypeID;

/**
 * 
 * @author akrem benmarzouk
 * @author helmi benabdallah
 * 
 */

public interface AMQPActivityContextInterfaceFactory {
	
	public static final ResourceAdaptorTypeID RATYPE_ID = AMQPResourceAdaptorSbbInterface.RATYPE_ID;

	public ActivityContextInterface getActivityContextInterface(AMQPActivity amqpActivity)
			throws NullPointerException, UnrecognizedActivityException,
			FactoryException;

}
