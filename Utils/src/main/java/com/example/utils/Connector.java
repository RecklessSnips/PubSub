package com.example.utils;

import com.example.utils.news.News;
import com.example.utils.news.NewsApi;
import com.example.utils.news.NewsData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.solace.messaging.MessagingService;
import com.solace.messaging.PubSubPlusClientException;
import com.solace.messaging.config.SolaceProperties;
import com.solace.messaging.config.SolaceProperties.MessageProperties;
import com.solace.messaging.config.profile.ConfigurationProfile;
import com.solace.messaging.publisher.DirectMessagePublisher;
import com.solace.messaging.publisher.OutboundMessage;
import com.solace.messaging.publisher.OutboundMessageBuilder;
import com.solace.messaging.publisher.PersistentMessagePublisher;
import com.solace.messaging.receiver.DirectMessageReceiver;
import com.solace.messaging.receiver.PersistentMessageReceiver;
import com.solace.messaging.resources.Queue;
import com.solace.messaging.resources.Topic;
import com.solace.messaging.resources.TopicSubscription;
import com.solace.messaging.util.Converter.ObjectToBytes;
import com.solace.messaging.util.Converter.BytesToObject;
import com.solace.messaging.util.ManageablePublisher;
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

public class Connector implements DisposableBean {

    private static final String TOPIC_PREFIX = "solace/samples/";  // used as the topic "root"
    private static final String API = "Java";
    private static final String QUEUE_NAME = "test";
    private static volatile boolean hasDetectedRedelivery = false;
    private final MessagingService messagingService;
    private final DirectMessagePublisher directMessagePublisher;
    private final PersistentMessagePublisher publisher;
    private final DirectMessageReceiver directMessageReceiver;
    private final PersistentMessageReceiver receiver;

    public Connector(String host, String vpn, String username, String password) {
        final Properties properties = new Properties();
        // host:port
        properties.setProperty(SolaceProperties.TransportLayerProperties.HOST, host);
        // message-vpn
        properties.setProperty(SolaceProperties.ServiceProperties.VPN_NAME,  vpn);
        // client-username
        properties.setProperty(SolaceProperties.AuthenticationProperties.SCHEME_BASIC_USER_NAME, username);
        // client-password
        properties.setProperty(SolaceProperties.AuthenticationProperties.SCHEME_BASIC_PASSWORD, password);
        // recommended settings
        properties.setProperty(SolaceProperties.TransportLayerProperties.RECONNECTION_ATTEMPTS, "20");
        properties.setProperty(SolaceProperties.TransportLayerProperties.CONNECTION_RETRIES_PER_HOST, "5");

        messagingService = MessagingService.builder(ConfigurationProfile.V1)
                .fromProperties(properties)
                .build();

        // Direct
        directMessagePublisher = messagingService
                .createDirectMessagePublisherBuilder()
                .build();

        directMessageReceiver = messagingService
                .createDirectMessageReceiverBuilder()
                // Dynamic Topic! Accept everything on the next Topic level
                .withSubscriptions(TopicSubscription.of("news/*"))
                .build();

        // Persistent
        publisher = messagingService
                .createPersistentMessagePublisherBuilder()
                .build();

        receiver = messagingService
                .createPersistentMessageReceiverBuilder()
                .build(Queue.durableExclusiveQueue(QUEUE_NAME));
    }

    public void connect() {
        messagingService.connect();  // blocking connect to the broker
    }

    // Make sure called after connect()
    public void startDirectPublisher() {
        // Start the publisher
        directMessagePublisher.startAsync(
                // Every Async excepts a CompletionListener, and this is a Functional interface
                (publisher, throwable) -> {
                    // If there's an error occurred during start
                    if (throwable != null) {
                        throw new RuntimeException(
                                String.format("Failed to start directMessagePublisher due to: %s", throwable.getCause())
                        );
                    } else {
                        // Start successfully, start publishing
                        System.out.println("Publisher starts successfully!");
                    }
                }
        );
    }

    // Make sure called after connect()
    public void startDirectReceiver() {
        // Start the receiver
        directMessageReceiver.startAsync(
                (receiver, throwable) -> {
                    if (throwable != null) {
                        throw new RuntimeException(
                                String.format("Failed to start directMessageReceiver due to: %s", throwable.getCause())
                        );
                    } else {
                        System.out.println("Receiver starts successfully!");
                    }
                }
        );
    }

    public void disconnect() {
        if (messagingService != null) {
            messagingService.disconnect();
        }
    }

    public ObjectToBytes<News> getNewsToBytesConverter(){
        return newsApi -> {
            try{
                ObjectMapper mapper = new ObjectMapper();
                // Add this line to support Instant data type
                // maintain UTC time when sending to the broker
                mapper.registerModule(new JavaTimeModule());
                return mapper.writeValueAsBytes(newsApi);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Cannot process JSON", e);
            } catch (RuntimeException e){
                throw new RuntimeException("Failed to convert News to bytes", e);
            }
        };
    }

    public <T extends News> BytesToObject<T> getBytesToNewsConverter(Class<T> clazz) {
        // bytes are the payload, will be called by Solace and convert them into News
        return bytes -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                return mapper.readValue(bytes, clazz);
            } catch (IOException e) {
                throw new RuntimeException("Cannot process JSON", e);
            }
        };
    }


    // Publish a list of same source of news to a Topic
    public void publishDirect(List<News> newsList, String newsType, String topic){
        // Since News is an object, we need to convert into bytes to send to the Broker
        ObjectToBytes<News> newsConverter = getNewsToBytesConverter();
        // Create the message builder for best practise
        OutboundMessageBuilder outboundMessageBuilder = messagingService.messageBuilder();

        // Set a failure listener
        directMessagePublisher.setPublishFailureListener(
            (failedPublishEvent) -> {
                System.out.println("Failed to send message: ");
                System.out.println(failedPublishEvent);
            }
        );

        for (News newsItem : newsList) {
            // Construct the message
            OutboundMessage message = outboundMessageBuilder
                    .withProperty("News type", newsType)
                    .build(newsItem, newsConverter);
            System.out.println("📤 Sending message: ");
            System.out.println(message.getPayloadAsString());

            try {
                directMessagePublisher.publish(message, Topic.of(topic));
            } catch (PubSubPlusClientException e) {
                throw new PubSubPlusClientException(e);
            } catch (IllegalStateException e) {
                throw new IllegalArgumentException(e);
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }

    }

    // Keep receiving messages
    public void receiveDirect(Consumer<NewsApi> onNewAPIReceived, Consumer<NewsData> onNewsDataReceived) {
        // 这些async 都是异步线程，竞争执行
        directMessageReceiver.receiveAsync(
            inboundMessage -> {
                // Successfully received messages!
                String newsType = inboundMessage.getProperty("News type");
                News news = null;
                // Different News type!
                System.out.println("💌Message received!");

                if (newsType.equals("NewsData")) {
                    BytesToObject<NewsData> bytesToNewsConverter = getBytesToNewsConverter(NewsData.class);
                    news = inboundMessage.getAndConvertPayload(bytesToNewsConverter, NewsData.class);
                    // Receiver will save the news to the news data list
                    onNewsDataReceived.accept((NewsData) news);
                } else if (newsType.equals("NewsAPI")) {
                    BytesToObject<NewsApi> bytesToNewsConverter = getBytesToNewsConverter(NewsApi.class);
                    news = inboundMessage.getAndConvertPayload(bytesToNewsConverter, NewsApi.class);
                    // Receiver will save the news to the news api list
                    onNewAPIReceived.accept((NewsApi) news);
                }
                System.out.println(news);

                // Callback function, 用来将这个拿到的函数 return 出去

//                System.out.println("AppType: " + inboundMessage.getApplicationMessageType());
//                System.out.println("SenderId: " + inboundMessage.getSenderId());
//                System.out.println("Priority: " + inboundMessage.getPriority());
//                System.out.println("SequenceNumber: " + inboundMessage.getSequenceNumber());
//                System.out.println("Property[key]: " + inboundMessage.getProperty("key"));
            }
        );
    }

    public void publishPersistent(String payload){
        publisher.start();
        Properties messageProps = new Properties();
        messageProps.put(MessageProperties.PERSISTENT_ACK_IMMEDIATELY, "true");
        OutboundMessageBuilder messageBuilder = messagingService.messageBuilder().fromProperties(messageProps);
        try{
            OutboundMessage message = messageBuilder.build(payload.getBytes());
            String topicString = new StringBuilder(TOPIC_PREFIX)
                    .append(API.toLowerCase())
                    .append("/pers/pub/")
                    .append("test")
                    .toString();
            try {
                publisher.publishAwaitAcknowledgement(message,Topic.of(topicString), 2000L);
            } catch (PubSubPlusClientException e) {
                System.out.printf("NACK for Message %s - %s%n", message, e);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
            System.out.println("Message sent successfully");
        } catch (RuntimeException e) {
            System.out.printf("### Caught while trying to publisher.publish() %s\n",e);
        } finally {
            publisher.terminate(1500);
            System.out.println("Publisher terminated.");
        }
    }

    public void receivePersistent() throws InterruptedException, IOException {
        try {
            receiver.start();
        } catch (RuntimeException e) {
            System.err.printf("%n*** Could not establish a connection to queue '%s': %s%n", QUEUE_NAME, e.getMessage());
            System.err.println("Create queue using PubSub+ Manager WebGUI, and add subscription "+ TOPIC_PREFIX+"*/pers/>");
            System.err.println("  or see the SEMP CURL scripts inside the 'semp-rest-api' directory.");
            System.err.println("NOTE: see HowToEnableAutoCreationOfMissingResourcesOnBroker.java sample for how to construct queue with consumer app.");
            System.err.println("Exiting.");
            return;
        }

        receiver.receiveAsync(message -> {
            if (message.isRedelivered()) {
                System.out.println("*** Redelivery detected ***");
                hasDetectedRedelivery = true;
            }
            String payload = new String(message.getPayloadAsBytes());
            System.out.println("Received message payload: " + payload);
            receiver.ack(message);
        });
        System.out.println("Waiting for messages... press Enter to exit.");
        System.in.read();

        receiver.terminate(1500L);
    }

    // Disconnect from the broker once the bean is destroyed
    @Override
    public void destroy() throws Exception {
        directMessagePublisher.terminate(1000L);
        System.out.println("Publisher terminated");
        directMessageReceiver.terminate(1000L);
        System.out.println("Receiver terminated");
        System.out.println("Disconnecting from event broker...");
        disconnect();
        System.out.println("Done");
    }
}
