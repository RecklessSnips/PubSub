package com.example.newssubscriber.controller;

import com.example.newssubscriber.NewsSubscriber;
import com.example.utils.news.News;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/subscribe")
public class NewsController {

    private final NewsSubscriber newsSubscriber;

    public NewsController(@Autowired NewsSubscriber newsSubscriber){
        this.newsSubscriber = newsSubscriber;
    }

    @GetMapping("/bbc")
    public ResponseEntity<List<News>> getBBC(){
        List<News> newsList = newsSubscriber.getNewsList();
        return ResponseEntity.ok().body(newsList);
    }
}
