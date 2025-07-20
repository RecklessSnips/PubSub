package com.example.newsmanager.openfeign;

import com.example.utils.response.NewsApiResponse;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

// Collector from newsapi.org
@FeignClient(name = "NewsApiCollector", url = "https://newsapi.org/v2", configuration = FeignClientConfig.class)
@EnableFeignClients(basePackages = "com.example.newsmanager.openfeign")
public interface NewsAPICollector {

    @GetMapping("/everything")
    NewsApiResponse getEverything(
        @RequestParam Map<String, String> params,
        @RequestHeader("Authorization") String apiKey
    );

    @GetMapping("/top-headlines")
    NewsApiResponse getTopHeadlines(
        @RequestParam Map<String, String> params,
        @RequestHeader("Authorization") String apiKey
    );
}
