package com.example.newssubscriber.controller;

import com.example.newssubscriber.NewsSubscriber;
import com.example.utils.news.News;
import com.example.utils.news.NewsApi;
import com.example.utils.news.NewsData;
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
    public ResponseEntity<List<NewsApi>> getBBC(){
        List<NewsApi> newsList = newsSubscriber.getNewsApiList("bbc");
        return ResponseEntity.ok().body(newsList);
    }

    @GetMapping("/cnn")
    public ResponseEntity<List<NewsApi>> getCNN(){
        List<NewsApi> newsList = newsSubscriber.getNewsApiList("cnn");
        return ResponseEntity.ok().body(newsList);
    }

    @GetMapping("/nytimes")
    public ResponseEntity<List<NewsData>> getNYTimes(){
        List<NewsData> newsList = newsSubscriber.getNewsDataList("nytimes");
        return ResponseEntity.ok().body(newsList);
    }

    @GetMapping("/reuters")
    public ResponseEntity<List<NewsData>> getReuters(){
        List<NewsData> newsList = newsSubscriber.getNewsDataList("reuters");
        return ResponseEntity.ok().body(newsList);
    }

    @GetMapping("/economist")
    public ResponseEntity<List<NewsData>> getEconomist(){
        List<NewsData> newsList = newsSubscriber.getNewsDataList("economist");
        return ResponseEntity.ok().body(newsList);
    }
}
