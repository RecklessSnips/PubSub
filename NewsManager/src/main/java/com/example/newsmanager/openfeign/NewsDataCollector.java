package com.example.newsmanager.openfeign;


import com.example.utils.response.NewsDataResponse;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

//Collector from newsdata.io
@FeignClient(name = "NewsDataCollector", url = "https://newsdata.io/api/1", configuration = FeignClientConfig.class)
@EnableFeignClients(basePackages = "com.example.newsmanager.openfeign")
public interface NewsDataCollector {

    @GetMapping("/latest")
    NewsDataResponse getLatest(
//            Pass in multiple headers or params
            @RequestHeader Map<String, String> headers,
            @RequestParam Map<String, String> queryParams
    );
}
