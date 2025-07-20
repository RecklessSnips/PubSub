package com.example.newssubscriber;

import com.example.utils.Connector;
import com.example.utils.news.NewsApi;
import com.example.utils.news.NewsData;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class NewsSubscriber {

    private final Connector connector;
    private List<NewsApi> newsApiList;
    private List<NewsData> newsDataList;

    public NewsSubscriber(@Autowired Connector connector){
        this.connector = connector;
        this.newsApiList = new ArrayList<>();
        this.newsDataList = new ArrayList<>();
    }

    public void subscribeTo(){
        connector.receiveDirect(
//            Callback function to store the news into newsList
            newsApi -> newsApiList.add(newsApi),
            newsData -> newsDataList.add(newsData)
        );
    }

    //***************** News API news *****************//
    public List<NewsApi> getNewsApiList(String source) {
        if (!newsApiList.isEmpty()){
            // Filter news
            if (source.equalsIgnoreCase("cnn")) {
                return newsApiList.stream()
                        .filter(news -> news.getSource().getName().toLowerCase().contains("cnn"))
                        .toList();
            } else if (source.equalsIgnoreCase("bbc")) {
                return newsApiList.stream()
                        .filter(news -> news.getSource().getName().toLowerCase().contains("bbc"))
                        .toList();
            } else {
                System.out.println("Cannot find any news api");
                return Collections.emptyList();
            }
        }else{
            System.out.println("Empty news api list, returning empty list");
            return Collections.emptyList();
        }
    }

    //***************** News Data news *****************//
    public List<NewsData> getNewsDataList(String source){
        if (!newsDataList.isEmpty()){
            // Filter news
            if (source.equalsIgnoreCase("reuters")) {
                return newsDataList.stream()
                        .filter(news -> news.getSourceId().toLowerCase().contains("reuters"))
                        .toList();
            } else if (source.equalsIgnoreCase("nytimes")) {
                return newsDataList.stream()
                        .filter(news -> news.getSourceId().toLowerCase().contains("nytimes"))
                        .toList();
            } else if (source.equalsIgnoreCase("economist")) {
                return newsDataList.stream()
                        .filter(news -> news.getSourceId().toLowerCase().contains("economist"))
                        .toList();
            } else {
                System.out.println("Cannot find any news data");
                return Collections.emptyList();
            }
        }else{
            System.out.println("Empty news data list, returning empty list");
            return Collections.emptyList();
        }
    }


    @PostConstruct
    public void init() {
        // Blocking connect
        connector.connect();
        connector.startDirectReceiver();

        subscribeTo();
    }
}
