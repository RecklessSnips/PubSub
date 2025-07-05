package com.example.newssubscriber;

import com.example.utils.Connector;
import com.example.utils.news.News;
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
    private List<News> newsList;

    public NewsSubscriber(@Autowired Connector connector){
        this.connector = connector;
        this.newsList = new ArrayList<>();
        connector.connect();
    }

    public void subscribeTo(){
        connector.receiveDirect(
//            Callback function to store the news into newsList
            news -> newsList.add(news)
        );
    }

    public List<News> getNewsList(){
        if (!newsList.isEmpty()){
            return newsList;
        }else{
            System.out.println("Empty news list, returning empty list");
            return Collections.emptyList();
        }
    }

    @PostConstruct
    public void listenToTopic(){
        subscribeTo();
    }
}
