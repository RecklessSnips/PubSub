package com.example.newsmanager.openfeign;


import com.example.utils.response.NewsApiResponse;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

// Collector from newsapi.org
@FeignClient(name = "NewsApiCollector", url = "https://newsapi.org/v2", configuration = FeignClientConfig.class)
@EnableFeignClients(basePackages = "com.example.newsmanager.openfeign")
public interface NewsAPICollector {

    @GetMapping("/everything")
    NewsApiResponse getEverything(
            @RequestHeader("Authorization") String apiKey,
            @RequestParam("domains") String domains
    );
}
