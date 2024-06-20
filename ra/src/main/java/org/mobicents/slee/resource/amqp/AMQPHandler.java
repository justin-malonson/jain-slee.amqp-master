package org.mobicents.slee.resource.amqp;

import java.util.Properties;

import javax.slee.facilities.Tracer;
import javax.slee.resource.ActivityHandle;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.ReceiveAndReplyCallback;
import org.springframework.amqp.core.ReplyToAddressCallback;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

import com.rabbitmq.tools.json.JSONReader;

public class AMQPHandler extends Thread implements AMQPActivity, ActivityHandle{

    private boolean closed = false;
    
	private final AMQPResourceAdaptor ra;
	private final AMQPID id;
	private final Tracer tracer;
	
	private final SimpleMessageListenerContainer container;
	private final String queueName;
	private final AmqpTemplate amqpTemplate;
	private final AmqpAdmin amqpAdmin;
	

	public AMQPHandler(Tracer tracer, AMQPResourceAdaptor ra, AMQPID id,
			CachingConnectionFactory cf, SimpleMessageListenerContainer container, String queueName) {
		
		super( "AMQPHandler thread for " + id );
		
		this.tracer = tracer;
        this.ra = ra;
        this.id = id;
       
        this.queueName = queueName;
        
        this.container = container;
        this.amqpAdmin = new RabbitAdmin(cf);
        this.amqpTemplate = new RabbitTemplate(cf);
        
        tracer.info( "New amqp connection to " + cf.getHost() );
        
     // Inform the RA of this new connection
        ra.connectionOpened( this );
		
	}

	// Called from SBB
	@Override
	public void close(){
		shutdown();
	}

	 
	// Called from SBB
	@Override
	public boolean isOpen(){
		
		return container.isRunning();
	}
	
	/**
     * {@inheritDoc}
     */
	@Override
	public void run() {
		tracer.info("running new container ID : " + this.id);

			try {
				final AMQPHandler handler = this;
				Object listener = new Object() {
			        public void handleMessage(String foo) {
			        	//check if it's a json string
			        	try{
			        	JSONReader jr = new JSONReader();
			        	jr.read(foo);
		                ra.publishEvent(handler, foo);
			        	}catch(IllegalStateException e){
			        		tracer.info("this is not valid json string and will be ignored");
			        	}
			        }
			    };
			    
			    //declaring the queue
			    
			    amqpAdmin.declareQueue(new Queue(queueName));
			    
			    
			    //starting the container
			    MessageListenerAdapter adapter = new MessageListenerAdapter(listener);
			    
			    container.setMessageListener(adapter);
			    container.setQueueNames(queueName);
			    container.start();
				
			} catch (Exception e) {
				tracer.warning( "socket closed" );
			}
	}
	    
	public AMQPID getAmqpID() {
		return id;
	}
	
	
	private synchronized void shutdown(){
		
		if (!closed) {
            closed = true;
            tracer.fine( "shutting down..." );
			try {
//				channel.close();
//				connection.close();
				container.stop();
				ra.connectionClosed( id );
			} catch (Exception e) {
				tracer.warning( "error while shutting down", e );
			}
		}
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public void convertAndSend(Object arg0) throws AmqpException {
		amqpTemplate.convertAndSend(arg0);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public void convertAndSend(String arg0, Object arg1) throws AmqpException {
		amqpTemplate.convertAndSend(arg0, arg1);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public void convertAndSend(Object arg0, MessagePostProcessor arg1)
			throws AmqpException {
		amqpTemplate.convertAndSend(arg0, arg1);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public void convertAndSend(String arg0, String arg1, Object arg2)
			throws AmqpException {
		amqpTemplate.convertAndSend(arg0, arg1, arg2);
	}

	@Override
	public void convertAndSend(String arg0, Object arg1,
			MessagePostProcessor arg2) throws AmqpException {
		amqpTemplate.convertAndSend(arg0, arg1, arg2);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public void convertAndSend(String arg0, String arg1, Object arg2,
			MessagePostProcessor arg3) throws AmqpException {
		amqpTemplate.convertAndSend(arg0, arg1, arg2, arg3);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public Object convertSendAndReceive(Object arg0) throws AmqpException {
		return amqpTemplate.convertSendAndReceive(arg0);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public Object convertSendAndReceive(String arg0, Object arg1)
			throws AmqpException {
		return amqpTemplate.convertSendAndReceive(arg0, arg1);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public Object convertSendAndReceive(Object arg0, MessagePostProcessor arg1)
			throws AmqpException {
		return amqpTemplate.convertSendAndReceive(arg0, arg1);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public Object convertSendAndReceive(String arg0, String arg1, Object arg2)
			throws AmqpException {
		return amqpTemplate.convertSendAndReceive(arg0, arg1, arg2);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public Object convertSendAndReceive(String arg0, Object arg1,
			MessagePostProcessor arg2) throws AmqpException {
		return amqpTemplate.convertSendAndReceive(arg0, arg1, arg2);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public Object convertSendAndReceive(String arg0, String arg1, Object arg2,
			MessagePostProcessor arg3) throws AmqpException {
		return amqpTemplate.convertSendAndReceive(arg0, arg1, arg2, arg3);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public Message receive() throws AmqpException {
		return amqpTemplate.receive();
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public Message receive(String arg0) throws AmqpException {
		return amqpTemplate.receive(arg0);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public Object receiveAndConvert() throws AmqpException {
		return amqpTemplate.receiveAndConvert();
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public Object receiveAndConvert(String arg0) throws AmqpException {
		return amqpTemplate.receiveAndConvert(arg0);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public <R, S> boolean receiveAndReply(ReceiveAndReplyCallback<R, S> arg0)
			throws AmqpException {
		return amqpTemplate.receiveAndReply(arg0);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public <R, S> boolean receiveAndReply(String arg0,
			ReceiveAndReplyCallback<R, S> arg1) throws AmqpException {
		return amqpTemplate.receiveAndReply(arg0, arg1);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public <R, S> boolean receiveAndReply(ReceiveAndReplyCallback<R, S> arg0,
			ReplyToAddressCallback<S> arg1) throws AmqpException {
		return amqpTemplate.receiveAndReply(arg0, arg1);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public <R, S> boolean receiveAndReply(ReceiveAndReplyCallback<R, S> arg0,
			String arg1, String arg2) throws AmqpException {
		return amqpTemplate.receiveAndReply(arg0, arg1, arg2);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public <R, S> boolean receiveAndReply(String arg0,
			ReceiveAndReplyCallback<R, S> arg1, ReplyToAddressCallback<S> arg2)
			throws AmqpException {
		return amqpTemplate.receiveAndReply(arg0, arg1, arg2);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public <R, S> boolean receiveAndReply(String arg0,
			ReceiveAndReplyCallback<R, S> arg1, String arg2, String arg3)
			throws AmqpException {
		return amqpTemplate.receiveAndReply(arg0, arg1, arg2, arg3);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public void send(Message arg0) throws AmqpException {
		amqpTemplate.send(arg0);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public void send(String arg0, Message arg1) throws AmqpException {
		amqpTemplate.send(arg0, arg1);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public void send(String arg0, String arg1, Message arg2)
			throws AmqpException {
		amqpTemplate.send(arg0, arg1, arg2);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public Message sendAndReceive(Message arg0) throws AmqpException {
		return amqpTemplate.sendAndReceive(arg0);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public Message sendAndReceive(String arg0, Message arg1)
			throws AmqpException {
		return amqpTemplate.sendAndReceive(arg0, arg1);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public Message sendAndReceive(String arg0, String arg1, Message arg2)
			throws AmqpException {
		return amqpTemplate.sendAndReceive(arg0, arg1, arg2);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public void declareBinding(Binding arg0) {
		amqpAdmin.declareBinding(arg0);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public void declareExchange(Exchange arg0) {
		amqpAdmin.declareExchange(arg0);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public Queue declareQueue() {
		return amqpAdmin.declareQueue();
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public String declareQueue(Queue arg0) {
		return amqpAdmin.declareQueue(arg0);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public boolean deleteExchange(String arg0) {
		return amqpAdmin.deleteExchange(arg0);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public boolean deleteQueue(String arg0) {
		return amqpAdmin.deleteQueue(arg0);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public void deleteQueue(String arg0, boolean arg1, boolean arg2) {
		amqpAdmin.deleteQueue(arg0, arg1, arg2);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public Properties getQueueProperties(String arg0) {
		return amqpAdmin.getQueueProperties(arg0);
	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public void purgeQueue(String arg0, boolean arg1) {
		// TODO Auto-generated method stub
		amqpAdmin.purgeQueue(arg0, arg1);

	}

	/**
	 *
	 *{@inheritDoc}
	 *
	 */
	@Override
	public void removeBinding(Binding arg0) {
		amqpAdmin.removeBinding(arg0);
	}

	@Override
	public void endActivity() {
		ra.endActivity(this);
		
		
	}

	@Override
	public String getRaEntityName() {
		return ra.getContext().getEntityName();
	}

	@Override
	public String getQueueName() {
		return this.queueName;
	}


}
