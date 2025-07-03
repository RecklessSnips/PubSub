package com.example.newsmanager.newsManager.brokerImpl;

import com.example.newsmanager.newsManager.NewsBroker;
import com.example.newsmanager.openfeign.NewsAPICollector;
import com.example.utils.Connector;
import com.example.utils.news.News;
import com.example.utils.response.NewsApiResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class NewsApiBroker extends NewsBroker {

    private final NewsAPICollector newsAPICollector;

    public NewsApiBroker(
            @Autowired NewsAPICollector newsAPICollector,
            @Autowired Connector connector,
            @Value("${newsapi.org.API_KEY}") String API_KEY){
        super(connector, API_KEY);
        this.newsAPICollector = newsAPICollector;
    }

    // Publish news to the Event Broker
    @Override
    public void publish(){

    }

    @Override
    public void search(String query){

    }

    @Override
    public void searchByDomain(String source, String... excludeDomains) {
        NewsApiResponse response = newsAPICollector.getEverything(API_KEY, source);
        System.out.println("Response: ");
        System.out.println(response.getArticles());
        newsList.addAll(response.getArticles());
    }

    @Override
    public void searchByDomains(List<String> domains, String... excludeDomains) {

    }

    @Override
    public void searchByCategory(String category) {

    }

    @Override
    public void searchByLanguage(String... lang) {

    }

    @PostConstruct
    public void searchNews(){
        searchByDomain("cnn.com", API_KEY);
    }

    // Publish news to event broker
    public void publishNews(){
        // Randomly pick a new from the list
        int i = new Random().nextInt(newsList.size());
        News newsApi = newsList.get(i);
        connector.publishDirect(newsApi);
    }

    @Scheduled(fixedRate = 10000)
    public void extractAndPublish(){
        publishNews();
    }

}
