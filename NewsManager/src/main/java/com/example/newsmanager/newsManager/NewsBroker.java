package com.example.newsmanager.newsManager;

import com.example.newsmanager.newsManager.interfaces.NewsCollector;
import com.example.utils.Connector;
import com.example.utils.news.News;

import java.util.ArrayList;
import java.util.List;

/*
    A news broker will need to collect the news and publish the news to the
    Event Broker
 */
public abstract class NewsBroker implements NewsCollector {

    protected String API_KEY;

    protected final Connector connector;

    protected List<News> newsList;

    protected NewsBroker(Connector connector, String API_KEY) {
        this.connector = connector;
        this.API_KEY = API_KEY;
        this.newsList = new ArrayList<>();
    }

    protected List<News> getNewsList(){
        return newsList;
    }

    protected String getApiKey() {
        return this.API_KEY;
    }

    protected abstract void publish();
}
