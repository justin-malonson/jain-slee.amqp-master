package org.mobicents.slee.resource.amqp;


import java.util.Properties;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.ReceiveAndReplyCallback;
import org.springframework.amqp.core.ReplyToAddressCallback;

/**
 * Specifies a basic set of AMQP operations for:
 * Provides synchronous send and receive methods. The {@link #convertAndSend(Object)} and {@link #receiveAndConvert()}
 * methods allow let you send and receive POJO objects. Implementations are expected to delegate to an instance of
 * {@link MessageConverter} to perform conversion to and from AMQP byte[] payload type.
 *
 * It also specifies a basic set of portable AMQP administrative operations for AMQP &gt; 0.8
 * 
 * the methods was written by : 
 * @author Mark Pollack
 * @author Mark Fisher
 * @author Artem Bilan
 * 
 * combined and modified by :
 * @author akrem benmarzouk
 * @author helmi benabdallah
 * 
 */
public interface AMQPActivity {
	
	/**
     * @return Uniques Id for this activity
     */
//    public String getSessionId();
    
    /**
     * service using this activity
     * has to explicitly call endActivity() method to end the Activity.
     */
	
	public void endActivity();
	
	/**
     * Get the name of the entity that owns this activity.
     */
    public String getRaEntityName();
	
    public void close();
    
    
    public boolean isOpen();
    
    
    // send methods for messages

 	/**
 	 * Send a message to a default exchange with a default routing key.
 	 *
 	 * @param message a message to send
 	 * @throws AmqpException if there is a problem
 	 */
 	void send(Message message) throws AmqpException;

 	/**
 	 * Send a message to a default exchange with a specific routing key.
 	 *
 	 * @param routingKey the routing key
 	 * @param message a message to send
 	 * @throws AmqpException if there is a problem
 	 */
 	void send(String routingKey, Message message) throws AmqpException;

 	/**
 	 * Send a message to a specific exchange with a specific routing key.
 	 *
 	 * @param exchange the name of the exchange
 	 * @param routingKey the routing key
 	 * @param message a message to send
 	 * @throws AmqpException if there is a problem
 	 */
 	void send(String exchange, String routingKey, Message message) throws AmqpException;

 	// send methods with conversion

 	/**
 	 * Convert a Java object to an Amqp {@link Message} and send it to a default exchange with a default routing key.
 	 *
 	 * @param message a message to send
 	 * @throws AmqpException if there is a problem
 	 */
 	void convertAndSend(Object message) throws AmqpException;

 	/**
 	 * Convert a Java object to an Amqp {@link Message} and send it to a default exchange with a specific routing key.
 	 *
 	 * @param routingKey the routing key
 	 * @param message a message to send
 	 * @throws AmqpException if there is a problem
 	 */
 	void convertAndSend(String routingKey, Object message) throws AmqpException;

 	/**
 	 * Convert a Java object to an Amqp {@link Message} and send it to a specific exchange with a specific routing key.
 	 *
 	 * @param exchange the name of the exchange
 	 * @param routingKey the routing key
 	 * @param message a message to send
 	 * @throws AmqpException if there is a problem
 	 */
 	void convertAndSend(String exchange, String routingKey, Object message) throws AmqpException;

 	/**
 	 * Convert a Java object to an Amqp {@link Message} and send it to a default exchange with a default routing key.
 	 *
 	 * @param message a message to send
 	 * @param messagePostProcessor a processor to apply to the message before it is sent
 	 * @throws AmqpException if there is a problem
 	 */
 	void convertAndSend(Object message, MessagePostProcessor messagePostProcessor) throws AmqpException;

 	/**
 	 * Convert a Java object to an Amqp {@link Message} and send it to a default exchange with a specific routing key.
 	 *
 	 * @param routingKey the routing key
 	 * @param message a message to send
 	 * @param messagePostProcessor a processor to apply to the message before it is sent
 	 * @throws AmqpException if there is a problem
 	 */
 	void convertAndSend(String routingKey, Object message, MessagePostProcessor messagePostProcessor)
 			throws AmqpException;

 	/**
 	 * Convert a Java object to an Amqp {@link Message} and send it to a specific exchange with a specific routing key.
 	 *
 	 * @param exchange the name of the exchange
 	 * @param routingKey the routing key
 	 * @param message a message to send
 	 * @param messagePostProcessor a processor to apply to the message before it is sent
 	 * @throws AmqpException if there is a problem
 	 */
 	void convertAndSend(String exchange, String routingKey, Object message, MessagePostProcessor messagePostProcessor)
 			throws AmqpException;

 	// receive methods for messages

 	/**
 	 * Receive a message if there is one from a default queue. Returns immediately, possibly with a null value.
 	 *
 	 * @return a message or null if there is none waiting
 	 * @throws AmqpException if there is a problem
 	 */
 	Message receive() throws AmqpException;

 	/**
 	 * Receive a message if there is one from a specific queue. Returns immediately, possibly with a null value.
 	 *
 	 * @param queueName the name of the queue to poll
 	 * @return a message or null if there is none waiting
 	 * @throws AmqpException if there is a problem
 	 */
 	Message receive(String queueName) throws AmqpException;

 	// receive methods with conversion

 	/**
 	 * Receive a message if there is one from a default queue and convert it to a Java object. Returns immediately,
 	 * possibly with a null value.
 	 *
 	 * @return a message or null if there is none waiting
 	 * @throws AmqpException if there is a problem
 	 */
 	Object receiveAndConvert() throws AmqpException;

 	/**
 	 * Receive a message if there is one from a specific queue and convert it to a Java object. Returns immediately,
 	 * possibly with a null value.
 	 *
 	 * @param queueName the name of the queue to poll
 	 * @return a message or null if there is none waiting
 	 * @throws AmqpException if there is a problem
 	 */
 	Object receiveAndConvert(String queueName) throws AmqpException;

 	// receive and send methods for provided callback

 	/**
 	 * Receive a message if there is one from a default queue, invoke provided {@link ReceiveAndReplyCallback}
 	 * and send reply message, if the {@code callback} returns one,
 	 * to the {@code replyTo} {@link org.springframework.amqp.core.Address}
 	 * from {@link org.springframework.amqp.core.MessageProperties}
 	 * or to default exchange and default routingKey.
 	 *
 	 * @param callback a user-provided {@link ReceiveAndReplyCallback} implementation to process received message
 	 *                 and return a reply message.
 	 * @param <R> The type of the request after conversion from the {@link Message}.
 	 * @param <S> The type of the response.
 	 * @return {@code true}, if message was received
 	 * @throws AmqpException if there is a problem
 	 */
 	<R, S> boolean receiveAndReply(ReceiveAndReplyCallback<R, S> callback) throws AmqpException;

 	/**
 	 * Receive a message if there is one from provided queue, invoke provided {@link ReceiveAndReplyCallback}
 	 * and send reply message, if the {@code callback} returns one,
 	 * to the {@code replyTo} {@link org.springframework.amqp.core.Address}
 	 * from {@link org.springframework.amqp.core.MessageProperties}
 	 * or to default exchange and default routingKey.
 	 *
 	 * @param queueName the queue name to receive a message.
 	 * @param callback a user-provided {@link ReceiveAndReplyCallback} implementation to process received message
 	 *                 and return a reply message.
 	 * @param <R> The type of the request after conversion from the {@link Message}.
 	 * @param <S> The type of the response.
 	 * @return {@code true}, if message was received.
 	 * @throws AmqpException if there is a problem.
 	 */
 	<R, S> boolean receiveAndReply(String queueName, ReceiveAndReplyCallback<R, S> callback) throws AmqpException;

 	/**
 	 * Receive a message if there is one from default queue, invoke provided {@link ReceiveAndReplyCallback}
 	 * and send reply message, if the {@code callback} returns one,
 	 * to the provided {@code exchange} and {@code routingKey}.
 	 *
 	 * @param callback a user-provided {@link ReceiveAndReplyCallback} implementation to process received message
 	 *                 and return a reply message.
 	 * @param replyExchange the exchange name to send reply message.
 	 * @param replyRoutingKey the routing key to send reply message.
 	 * @param <R> The type of the request after conversion from the {@link Message}.
 	 * @param <S> The type of the response.
 	 * @return {@code true}, if message was received.
 	 * @throws AmqpException if there is a problem.
 	 */
 	<R, S> boolean receiveAndReply(ReceiveAndReplyCallback<R, S> callback, String replyExchange, String replyRoutingKey)
 			throws AmqpException;

 	/**
 	 * Receive a message if there is one from provided queue, invoke provided {@link ReceiveAndReplyCallback}
 	 * and send reply message, if the {@code callback} returns one,
 	 * to the provided {@code exchange} and {@code routingKey}.
 	 *
 	 *
 	 * @param queueName the queue name to receive a message.
 	 * @param callback a user-provided {@link ReceiveAndReplyCallback} implementation to process received message
 	 *                 and return a reply message.
 	 * @param replyExchange the exchange name to send reply message.
 	 * @param replyRoutingKey the routing key to send reply message.
 	 * @param <R> The type of the request after conversion from the {@link Message}.
 	 * @param <S> The type of the response.
 	 * @return {@code true}, if message was received
 	 * @throws AmqpException if there is a problem
 	 */
 	<R, S> boolean receiveAndReply(String queueName, ReceiveAndReplyCallback<R, S> callback, String replyExchange,
 								   String replyRoutingKey) throws AmqpException;

 	/**
 	 * Receive a message if there is one from a default queue, invoke provided {@link ReceiveAndReplyCallback}
 	 * and send reply message, if the {@code callback} returns one,
 	 * to the {@code replyTo} {@link org.springframework.amqp.core.Address}
 	 * from result of {@link ReplyToAddressCallback}.
 	 *
 	 * @param callback a user-provided {@link ReceiveAndReplyCallback} implementation to process received message
 	 *                 and return a reply message.
 	 * @param replyToAddressCallback the callback to determine replyTo address at runtime.
 	 * @param <R> The type of the request after conversion from the {@link Message}.
 	 * @param <S> The type of the response.
 	 * @return {@code true}, if message was received.
 	 * @throws AmqpException if there is a problem.
 	 */
 	<R, S> boolean receiveAndReply(ReceiveAndReplyCallback<R, S> callback, ReplyToAddressCallback<S> replyToAddressCallback)
 			throws AmqpException;

 	/**
 	 * Receive a message if there is one from provided queue, invoke provided {@link ReceiveAndReplyCallback}
 	 * and send reply message, if the {@code callback} returns one,
 	 * to the {@code replyTo} {@link org.springframework.amqp.core.Address}
 	 * from result of {@link ReplyToAddressCallback}.
 	 *
 	 * @param queueName the queue name to receive a message.
 	 * @param callback a user-provided {@link ReceiveAndReplyCallback} implementation to process received message
 	 *                 and return a reply message.
 	 * @param replyToAddressCallback the callback to determine replyTo address at runtime.
 	 * @param <R> The type of the request after conversion from the {@link Message}.
 	 * @param <S> The type of the response.
 	 * @return {@code true}, if message was received
 	 * @throws AmqpException if there is a problem
 	 */
 	<R, S> boolean receiveAndReply(String queueName, ReceiveAndReplyCallback<R, S> callback,
 			ReplyToAddressCallback<S> replyToAddressCallback) throws AmqpException;

 	// send and receive methods for messages

 	/**
 	 * Basic RPC pattern. Send a message to a default exchange with a default routing key and attempt to receive a
 	 * response. Implementations will normally set the reply-to header to an exclusive queue and wait up for some time
 	 * limited by a timeout.
 	 *
 	 * @param message a message to send
 	 * @return the response if there is one
 	 * @throws AmqpException if there is a problem
 	 */
 	Message sendAndReceive(Message message) throws AmqpException;

 	/**
 	 * Basic RPC pattern. Send a message to a default exchange with a specific routing key and attempt to receive a
 	 * response. Implementations will normally set the reply-to header to an exclusive queue and wait up for some time
 	 * limited by a timeout.
 	 *
 	 * @param routingKey the routing key
 	 * @param message a message to send
 	 * @return the response if there is one
 	 * @throws AmqpException if there is a problem
 	 */
 	Message sendAndReceive(String routingKey, Message message) throws AmqpException;

 	/**
 	 * Basic RPC pattern. Send a message to a specific exchange with a specific routing key and attempt to receive a
 	 * response. Implementations will normally set the reply-to header to an exclusive queue and wait up for some time
 	 * limited by a timeout.
 	 *
 	 * @param exchange the name of the exchange
 	 * @param routingKey the routing key
 	 * @param message a message to send
 	 * @return the response if there is one
 	 * @throws AmqpException if there is a problem
 	 */
 	Message sendAndReceive(String exchange, String routingKey, Message message) throws AmqpException;

 	// send and receive methods with conversion

 	/**
 	 * Basic RPC pattern with conversion. Send a Java object converted to a message to a default exchange with a default
 	 * routing key and attempt to receive a response, converting that to a Java object. Implementations will normally
 	 * set the reply-to header to an exclusive queue and wait up for some time limited by a timeout.
 	 *
 	 * @param message a message to send
 	 * @return the response if there is one
 	 * @throws AmqpException if there is a problem
 	 */
 	Object convertSendAndReceive(Object message) throws AmqpException;

 	/**
 	 * Basic RPC pattern with conversion. Send a Java object converted to a message to a default exchange with a
 	 * specific routing key and attempt to receive a response, converting that to a Java object. Implementations will
 	 * normally set the reply-to header to an exclusive queue and wait up for some time limited by a timeout.
 	 *
 	 * @param routingKey the routing key
 	 * @param message a message to send
 	 * @return the response if there is one
 	 * @throws AmqpException if there is a problem
 	 */
 	Object convertSendAndReceive(String routingKey, Object message) throws AmqpException;

 	/**
 	 * Basic RPC pattern with conversion. Send a Java object converted to a message to a specific exchange with a
 	 * specific routing key and attempt to receive a response, converting that to a Java object. Implementations will
 	 * normally set the reply-to header to an exclusive queue and wait up for some time limited by a timeout.
 	 *
 	 * @param exchange the name of the exchange
 	 * @param routingKey the routing key
 	 * @param message a message to send
 	 * @return the response if there is one
 	 * @throws AmqpException if there is a problem
 	 */
 	Object convertSendAndReceive(String exchange, String routingKey, Object message) throws AmqpException;

 	/**
 	 * Basic RPC pattern with conversion. Send a Java object converted to a message to a default exchange with a default
 	 * routing key and attempt to receive a response, converting that to a Java object. Implementations will normally
 	 * set the reply-to header to an exclusive queue and wait up for some time limited by a timeout.
 	 *
 	 * @param message a message to send
 	 * @param messagePostProcessor a processor to apply to the message before it is sent
 	 * @return the response if there is one
 	 * @throws AmqpException if there is a problem
 	 */
 	Object convertSendAndReceive(Object message, MessagePostProcessor messagePostProcessor) throws AmqpException;

 	/**
 	 * Basic RPC pattern with conversion. Send a Java object converted to a message to a default exchange with a
 	 * specific routing key and attempt to receive a response, converting that to a Java object. Implementations will
 	 * normally set the reply-to header to an exclusive queue and wait up for some time limited by a timeout.
 	 *
 	 * @param routingKey the routing key
 	 * @param message a message to send
 	 * @param messagePostProcessor a processor to apply to the message before it is sent
 	 * @return the response if there is one
 	 * @throws AmqpException if there is a problem
 	 */
 	Object convertSendAndReceive(String routingKey, Object message, MessagePostProcessor messagePostProcessor) throws AmqpException;

 	/**
 	 * Basic RPC pattern with conversion. Send a Java object converted to a message to a specific exchange with a
 	 * specific routing key and attempt to receive a response, converting that to a Java object. Implementations will
 	 * normally set the reply-to header to an exclusive queue and wait up for some time limited by a timeout.
 	 *
 	 * @param exchange the name of the exchange
 	 * @param routingKey the routing key
 	 * @param message a message to send
 	 * @param messagePostProcessor a processor to apply to the message before it is sent
 	 * @return the response if there is one
 	 * @throws AmqpException if there is a problem
 	 */
 	Object convertSendAndReceive(String exchange, String routingKey, Object message, MessagePostProcessor messagePostProcessor) throws AmqpException;
 	
 	
 	// Queue Operations
 	/**
	 * Declare an exchange
	 * @param exchange the exchange to declare.
	 */
	void declareExchange(Exchange exchange);

	/**
	 * Delete an exchange. Look at implementation specific subclass for implementation specific behavior, for example
	 * for RabbitMQ this will delete the exchange without regard for whether it is in use or not.
	 * @param exchangeName the name of the exchange
	 * @return true if the exchange existed and was deleted
	 */
	boolean deleteExchange(String exchangeName);

	// Queue Operations

	/**
	 * Declare a queue whose name is automatically named. It is created with exclusive = true, autoDelete=true, and
	 * durable = false.
	 *
	 * @return The queue.
	 */
	Queue declareQueue();

	/**
	 * Declare the given queue
	 * @param queue the queue to declare
	 * @return the name of the queue.
	 */
	String declareQueue(Queue queue);

	/**
	 * Delete a queue, without regard for whether it is in use or has messages on it
	 * @param queueName the name of the queue
	 * @return true if the queue existed and was deleted
	 */
	boolean deleteQueue(String queueName);

	// Note that nowait option is not readily exposed in Rabbit Java API but is for Rabbit .NET API.

	/**
	 * Delete a queue
	 * @param queueName the name of the queue
	 * @param unused true if the queue should be deleted only if not in use
	 * @param empty true if the queue should be deleted only if empty
	 */
	void deleteQueue(String queueName, boolean unused, boolean empty);

	/**
	 * Purges the contents of the given queue.
	 * @param queueName the name of the queue
	 * @param noWait true to not await completion of the purge
	 */
	void purgeQueue(String queueName, boolean noWait);

	// Binding opertaions

	/**
	 * Declare a binding of a queue to an exchange.
	 * @param binding a description of the binding to declare.
	 */
	void declareBinding(Binding binding);

	/**
	 * Remove a binding of a queue to an exchange. Note unbindQueue/removeBinding was not introduced until 0.9 of the
	 * specification.
	 * @param binding a description of the binding to remove.
	 */
	void removeBinding(Binding binding);

	/**
	 * Returns an implementation-specific Map of properties if the queue exists.
	 * @param queueName the name of the queue.
	 * @return the properties or null if the queue doesn't exist
	 */
	Properties getQueueProperties(String queueName);
	
	/**
	 * return the queue name
	 */
	
	public String getQueueName();
}
