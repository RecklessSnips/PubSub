package com.example.newsmanager.openfeign;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Bean
    public ErrorHandler errorHandler(){
        return new ErrorHandler();
    }
}
