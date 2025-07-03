package com.example.newsmanager.newsManager.brokerImpl;

import com.example.newsmanager.newsManager.NewsBroker;
import com.example.newsmanager.openfeign.NewsDataCollector;
import com.example.utils.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NewsDataBroker extends NewsBroker {

    private final NewsDataCollector newsDataCollector;

    public NewsDataBroker(
            @Autowired NewsDataCollector newsDataCollector,
            @Autowired Connector connector,
            @Value("${newsdata.io.API_KEY}") String API_KEY){
        super(connector, API_KEY);
        this.newsDataCollector = newsDataCollector;
    }

    // Publish news to the Event Broker
    @Override
    public void publish(){

    }

    @Override
    public void search(String query) {

    }

    @Override
    public void searchByDomain(String source, String... excludeDomains) {

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
}
