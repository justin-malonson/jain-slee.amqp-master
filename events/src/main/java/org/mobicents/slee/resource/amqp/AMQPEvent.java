package org.mobicents.slee.resource.amqp;

/**
 * <p>
 * The AMQP protocol is based on sending and receiving {@link String} 
 * JSON message to/from an AMQP Server.
 * </p>
 * <p>
 * We have defined one Event for the AMQPRA called {@link AMQPEvent}
 * </p>
 * @author akrem benmarzouk
 * @author helmi benabdallah
 *
 */

public interface AMQPEvent {	
	
	/**
    * The Message included in the AmqpEvent.
    * 
    * @return Message containing data sent through this event
    */
	public String getAmqpMessage();

}
