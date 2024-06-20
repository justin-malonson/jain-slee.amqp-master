package org.mobicents.slee.resource.amqp;

import java.util.UUID;

import javax.slee.facilities.Tracer;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;



public class AMQPListener extends Thread{
	
	
	AMQPResourceAdaptor ra;
	
	//private UUID sequence;

	SimpleMessageListenerContainer container;
	String queueName;
	
	CachingConnectionFactory cf;
	
	private final Tracer tracer;

	public AMQPListener(Tracer tracer, AMQPResourceAdaptor ra,
			String amqpHost, int amqpPort, String username, String password, 
			String queueName) {
		
		this.ra = ra;
		this.cf = new CachingConnectionFactory(amqpHost, amqpPort);
		cf.setUsername(username);
		cf.setPassword(password);
		
		container =
	            new SimpleMessageListenerContainer(cf);
		
		this.queueName = queueName;
		this.tracer = tracer;
	}
	
	@Override
	public void run (){
		
		tracer.info("creating amqp container...");
		try {
			// Create a unique ID for the connection - this is also the
			// activity handle
			AMQPID id = new AMQPID(UUID.randomUUID().toString());

			// Create a new connection handler thread and start it
			AMQPHandler conn = new AMQPHandler(tracer, ra, id,
						cf, container, queueName);
				conn.start();

			} catch (Exception e) {
				tracer.severe("error accepting connection", e);
			}
	}
	
	/**
     * Close the listen socket and stop the thread, called when the RA is deactivated.
     */
	public void close() {
		
        shutdown();

	}

	private void shutdown() {
		try {
				container.stop();
			}
		 catch (Exception e) {
			tracer.severe("error stopping amqp container", e);
		}
	}

}
