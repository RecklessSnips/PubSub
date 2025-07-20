package com.example.notifications.subscription;

import com.example.utils.Connector;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/*
    Remember to create the Queue and Topic manually in Solace Broker GUI

    这个类来管理一切跟订阅有关的方法，比如开启订阅
 */
@Component
public class SubManager {

    //TODO: 记得选择性开启 direct 或者 persistent，不要一股脑全开了，并且在destroy的时候判断是否开了，再关
    private final Connector connector;

    // 这两个是负责储存订阅的新闻 Source，比如 BBC，CNN...
    private Set<String> newsApiSubscriptions;
    private Set<String> newsDataSubscriptions;

    public SubManager(@Autowired Connector connector) {
        this.connector = connector;
        this.newsApiSubscriptions = new HashSet<>();
        this.newsDataSubscriptions = new HashSet<>();
    }

    // Use persistent publisher to publish, client must ACK!
    public void subscribe(String source) {
        System.out.println("Subscribe to source: " + source);

        // 加入订阅集
        switch (source) {
            case "bbc", "cnn":
                newsApiSubscriptions.add(source);
                break;
            case "reuters", "nytimes", "economist":
                newsDataSubscriptions.add(source);
                break;
        }

        System.out.println("Subscription topics: ");
        System.out.println(newsApiSubscriptions);
        System.out.println(newsDataSubscriptions);
    }

    // 未应用取消订阅，懒......
    public void unsubscribe(String source) {
        newsApiSubscriptions.remove(source);
        newsDataSubscriptions.remove(source);
    }

    /*
     这个方法会被自动开启，然后每当有订阅进来的时候，前面的 newsApiSubscriptions，newsDataSubscriptions
     就会被添加新的，然后这个方法会根据里面的订阅源来进行 filter。

     因为 receiveDirect() 只能开启一次，所以这个方法只能被开启一次

     注：目前BBC和CNN都是 NewsAPI，
        NYTimes，Reuters，Economist 都是 NewsData

     调用逻辑：在 Post Construct 里开启一次且仅有一次，便开始实时监听所有的 News，每当有一个订阅进来
     调用了 subscribe() 方法，这个方法将订阅的 News 放到一个 Set 里，listeningToNews() 将会在每一次
     查询监听到的信息是否在这个 Set 里，如果有跟 Set 里匹配的订阅源，就发给 Client，从而实现订阅
     */
    private void listeningToNews() {
        connector.receiveDirect(
            // 注册每个的 CallBack func，每当收到新的信息，就调用
            newsApi -> {
                // 没有订阅
                if (newsApiSubscriptions.isEmpty()) {
                    System.out.println("No NewsAPI subscription, ignoring current News");
                    return;
                }

                System.out.println("NewsAPI updated, checking if client has subscription");
                System.out.println(newsApi);

                // 确定 Source，从而确定 publish 的时候，发布 Topic 的最后一级
                String source = "";
                if (newsApi.getSource().getName().toLowerCase().contains("bbc")) {
                    source = "bbc";
                } else if (newsApi.getSource().getName().toLowerCase().contains("cnn")) {
                    source = "cnn";
                }

                // Safety check, 但是不可能会出现，因为每一条 API 都会涵盖正确的信息。但以防万一
                if (!newsApiSubscriptions.contains(source)){
                    System.out.println("No matching NewsAPI subscription found");
                    return;
                }

                // 所有判断结束，将这个 News 发布到 Queue 里的对应 Topic，比如 news/bbc
                System.out.println("Publishing NewsAPI with source: " + source);
                connector.publishPersistent(newsApi, source);

            },
            newsData -> {
                // 没有订阅
                if (newsDataSubscriptions.isEmpty()) {
                    System.out.println("No NewsData subscription, ignoring current News");
                    return;
                }

                System.out.println("NewsData updated, checking if client has subscription");
                System.out.println(newsData);

                String source = "";
                if (newsData.getSourceId().toLowerCase().contains("reuters")) {
                    source = "reuters";
                } else if (newsData.getSourceId().toLowerCase().contains("nytimes")) {
                    source = "nytimes";
                } else if (newsData.getSourceId().toLowerCase().contains("economist")) {
                    source = "economist";
                }

                // Safety check, 但是不可能会出现，因为每一条 API 都会涵盖正确的信息。但以防万一
                if (!newsDataSubscriptions.contains(source)){
                    System.out.println("No matching NewsData subscription found");
                    return;
                }

                // 所有判断结束，将这个 News 发布到 Queue 里的对应 Topic，比如 news/bbc
                System.out.println("Publishing NewsData with source: " + source);
                connector.publishPersistent(newsData, source);
            }
        );
    }

    @PostConstruct
    public void init() {
        connector.connect();
        connector.startDirectReceiver();
        connector.startPersistentPublisher();
        // Start receiving messages, 然后只要有订阅进来，就会开始 filter！
        listeningToNews();
    }
}
