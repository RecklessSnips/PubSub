package com.example.utils;

import com.example.utils.news.News;
import com.example.utils.news.NewsApi;
import com.example.utils.news.NewsData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.solace.messaging.MessagingService;
import com.solace.messaging.PubSubPlusClientException;
import com.solace.messaging.config.MissingResourcesCreationConfiguration;
import com.solace.messaging.config.SolaceProperties;
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
import org.springframework.beans.factory.DisposableBean;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * This is a class of Solace Event Broker API I extracted from the Solace API
 * <a href="https://docs.solace.com/Get-Started/get-started-lp.htm">Link</a>
 * This class has the simplest function to publish / receive -> direct / persistent messages
 */
public class Connector implements DisposableBean {

    private static final String TOPIC_PREFIX = "subscriptions/";  // used as the topic "root"

    // Create this beforehand!
    private static final String QUEUE_NAME = "subscription";

    // The guy talks to the Event Broker himself
    private final MessagingService messagingService;

    // Publishers and Receivers
    private final DirectMessagePublisher directMessagePublisher;
    private final DirectMessageReceiver directMessageReceiver;
    private final PersistentMessagePublisher persistentMessagePublisher;

    private final PersistentMessageReceiver persistentMessageReceiver;

    public Connector(String host, String vpn, String username, String password) {
        // Mandatory fields
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

        // The service to connect with Solace Event Broker
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

        /*
            Solace 的 Queue是这样工作的：Topic to Queue
            1. 首先 Publisher 需要制定一个目标 Topic，然后将消息发送到这个 Topic上，不需要制定 Queue
            2. 然后 Receiver 需要绑定到一个 Queue，不需要制定 Topic
            3. 但是 这个 Queue 必须要制定感兴趣的 Topic！手动指定
            4. 然后 receiver 就会接收到这个 Queue 里所有感兴趣的 Topic 的消息

            经验：
            1. 尽量让 Queue exclusive，只能有一个消费者
            2. 启动 Receiver 的 自动创建 Queue 的配置
         */

        // Persistent
        persistentMessagePublisher = messagingService
                .createPersistentMessagePublisherBuilder()
                .build();

        persistentMessageReceiver = messagingService
            .createPersistentMessageReceiverBuilder()
            .withMissingResourcesCreationStrategy(
                    // 如果 Broker 没有底下 build() 里配置的 Queue，启动时自动创建
                    MissingResourcesCreationConfiguration.MissingResourcesCreationStrategy.CREATE_ON_START)
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
        // Blocking start
        directMessageReceiver.start();
        System.out.println("DirectMessageReceiver starts successfully!");
    }

    public void disconnect() {
        if (messagingService != null) {
            messagingService.disconnect();
        }
    }

    // 负责将 News instance 转换成 Bytes 进行网络传输！
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

    // 转换回来
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
    // newsType 用来辨别是 NewsAPI 还是 NewsData 发布的，对于接收方有帮助
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
            // Construct the message, extra properties allows granular control
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

    // Keep receiving messages from the Topic "news/*"
    public void receiveDirect(Consumer<NewsApi> onNewAPIReceived, Consumer<NewsData> onNewsDataReceived) {
        // 启动一个 async 异步线程，只能被启动一次，用来接收消息
        directMessageReceiver.receiveAsync(
            inboundMessage -> {
                // Successfully received messages!
                String newsType = inboundMessage.getProperty("News type");
                News news = null;
                // Different News type!
                System.out.println("💌Message received!");

                if (newsType.equals("NewsData")) {
                    // 让 NewsData Callback 来处理
                    BytesToObject<NewsData> bytesToNewsConverter = getBytesToNewsConverter(NewsData.class);
                    news = inboundMessage.getAndConvertPayload(bytesToNewsConverter, NewsData.class);
                    // Receiver will save the news to the news data list
                    onNewsDataReceived.accept((NewsData) news);
                } else if (newsType.equals("NewsAPI")) {
                    // 让 NewsAPI Callback 来处理
                    BytesToObject<NewsApi> bytesToNewsConverter = getBytesToNewsConverter(NewsApi.class);
                    news = inboundMessage.getAndConvertPayload(bytesToNewsConverter, NewsApi.class);
                    // Receiver will save the news to the news api list
                    onNewAPIReceived.accept((NewsApi) news);
                }
                System.out.println(news);
            }
        );

    }

    /*
        Persistent
     */
    public void startPersistentPublisher() {
        persistentMessagePublisher.startAsync(
            (onCompletionListener, throwable) -> {
                if (throwable != null) {
                    throw new RuntimeException(
                            String.format("Failed to start persistentMessagePublisher due to: %s", throwable.getCause())
                    );
                } else {
                    System.out.println("PersistentMessagePublisher starts successfully!");
                }
            }
        );
    }

    public void startPersistentReceiver() {
        persistentMessageReceiver.startAsync(
            (onCompletionListener, throwable) -> {
                if (throwable != null) {
                    throw new RuntimeException(
                            String.format("Failed to start persistentMessageReceiver due to: %s", throwable.getCause())
                    );
                } else {
                    System.out.println("PersistentMessageReceiver starts successfully!");
                }
            }
        );
    }


    /*
     Payload 是新闻本身，topicSuffix 是 Topic 的路径，用来附着到最后一级的 Topic 路径上
     而且 Queue 里是用 news/* 来匹配的，所以任何这一级的 News 都会被持久化到 Broker 上
     */
    public void publishPersistent(News payload, String topicSuffix){
        // 建立 OutboundMessage
        OutboundMessageBuilder messageBuilder = messagingService.messageBuilder();
        ObjectToBytes<News> newsConverter = getNewsToBytesConverter();
        String newsType = payload instanceof NewsApi ? "NewsAPI" : "NewsData";

        try{
            OutboundMessage message = messageBuilder
                    .withProperty("News type", newsType)
                    .build(payload, newsConverter);

            System.out.println("📤 Sending persistent message: ");
            System.out.println(message.getPayloadAsString());
            // Specify Topic name, dynamic Topic!
            String topicString = new StringBuilder(TOPIC_PREFIX)
                    .append("news/")
                    .append(topicSuffix) // 这里
                    .toString();
            try {
                persistentMessagePublisher.publish(message,Topic.of(topicString));
            } catch (PubSubPlusClientException e) {
                System.out.printf("Fail to publish message: %s - %s\n", message, e);
            }
            System.out.printf("Message %s sent successfully\n", payload);
        } catch (RuntimeException e) {
            System.out.printf("### Caught while trying to publisher.publish() %s\n",e);
        }
    }

    // JS 端自己处理接收，这里的 Receiver 是为了接收别的微服务发来的，暂时用不到
    public void receivePersistent(Consumer<NewsApi> onNewAPIReceived, Consumer<NewsData> onNewsDataReceived) {
        persistentMessageReceiver.receiveAsync(inboundMessage -> {
            /*
                Redelivery 说明queue中的消息被发给消费者，但是没有被成功接收
                可能的原因：
                1. 但没有 ack ，就会触发 redelivery
                2. 消费者处理消息后崩溃, ack 没机会发出，Broker 以为失败
                3. 如果 receiver 只是链接上，但没有任何行为，但这时候 publisher 依然发送了，
                    broker 也会投递给 consumer，但是因为 receiver 没有任何消费意图，所以投递失败
                    然后下一次再接收的时候就会触发 redelivery. 所以尽量保证 receiver 和 publisher
                    同时在线！
             */
            if (inboundMessage.isRedelivered()) {
                System.err.println("*** Redelivery detected, please call the ack() or check receiver connection. ***");
            }
            // Successfully received messages!
            String newsType = inboundMessage.getProperty("News type");
            News news = null;
            System.out.println("Persistent message type: ");
            System.out.println(newsType);

            // Must acknowledge the message!
            persistentMessageReceiver.ack(inboundMessage);

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
        });
    }

    // Disconnect from the broker once the bean is destroyed
    @Override
    public void destroy() {
        directMessagePublisher.terminate(1000L);
        System.out.println("Direct Publisher terminated");

        directMessageReceiver.terminate(1000L);
        System.out.println("Direct Receiver terminated");

        persistentMessagePublisher.terminate(1500L);
        System.out.println("Persistent Publisher terminated.");

        persistentMessageReceiver.terminate(1500L);
        System.out.println("Persistent Receiver terminated.");

        System.out.println("Disconnecting from event broker...");
        disconnect();
        System.out.println("Done");
    }
}
