package com.example.newsmanager.newsManager;


import com.example.newsmanager.openfeign.NewsAPICollector;
import com.example.newsmanager.openfeign.NewsDataCollector;
import com.example.utils.Connector;
import com.example.utils.news.News;
import com.example.utils.news.NewsData;
import com.example.utils.response.NewsApiResponse;
import com.example.utils.response.NewsDataResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class NewsManagerTest {

    @Value("${newsapi.org.API_KEY}")
    private String newsapi_API_KEY;
    @Value("${newsdata.io.API_KEY}")
    private String newsdata_API_KEY;
    private final NewsAPICollector newsAPICollector;
    private final NewsDataCollector newsDataCollector;
    private final Connector connector;
    private List<News> newsApiList;

    public NewsManagerTest(@Autowired NewsAPICollector newsAPICollector,
                           @Autowired NewsDataCollector newsDataCollector,
                           @Autowired Connector connector){
        this.newsAPICollector = newsAPICollector;
        this.newsDataCollector = newsDataCollector;
        // Openfeign use ArrayList as container
        this.newsApiList = new LinkedList<>();
        this.connector = connector;
        // Blocking connect to the broker
        connector.connect();
    }

    // Search the news! (newsapi.org in this case)
    public void search(){
        NewsApiResponse response = newsAPICollector.getEverything(newsapi_API_KEY, "wsj.com");
        newsApiList.addAll(response.getArticles());
    }

    public void search2(){
        NewsDataResponse latestNews = newsDataCollector.getLatest(
                Map.of("X-ACCESS-KEY", newsdata_API_KEY),
                Map.of("domainurl", "nytimes.com")
        );
        List<NewsData> articles = latestNews.getResults();
        System.out.println("Articles: ");
        System.out.println(latestNews);
        System.out.println(articles);
        newsApiList.addAll(articles);
    }

    // Publish news to event broker
    public void publishNews(){
        // Randomly pick a new from the list
        int i = new Random().nextInt(newsApiList.size());
        News newsApi = newsApiList.get(i);
        connector.publishDirect(newsApi);
    }

    //TODO: 将这个类抽象出来，再添加一个 interface 用来约束 pub sub 的方法，对于每一个子类，创建独立的 category filter

    // Search once and only once after application boot up
    @PostConstruct
    public void searchNews(){
//        search();
//        search2();
    }

    // Will run after the Application Context finish loading, so will call after the @PostConstruct
    // by that time, we should have <newsList> initialized
    @Scheduled(fixedRate = 10000)
    public void extractAndPublish(){
//        publishNews();
    }
}
