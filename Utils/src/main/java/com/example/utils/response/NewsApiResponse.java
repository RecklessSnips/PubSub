package com.example.utils.response;

import com.example.utils.news.NewsApi;

import java.util.List;

public class NewsApiResponse extends NewsResponse{

    private List<NewsApi> articles;

    public List<NewsApi> getArticles(){
        return articles;
    }
}
