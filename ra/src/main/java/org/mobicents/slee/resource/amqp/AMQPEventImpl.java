package org.mobicents.slee.resource.amqp;

import java.io.Serializable;



public class AMQPEventImpl implements AMQPEvent, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6962304839868584949L;
	private final String messageReceived;
	
	public AMQPEventImpl(String messageReceived) {
		super();
		this.messageReceived = messageReceived;
	}
	

	@Override
	public String getAmqpMessage() {
		return messageReceived;
	}


}
