package com.example.utils.response;

import com.example.utils.news.NewsData;

import java.util.List;

public class NewsDataResponse extends NewsResponse{

    private List<NewsData> results;

    public List<NewsData> getResults(){ return results; }
}
