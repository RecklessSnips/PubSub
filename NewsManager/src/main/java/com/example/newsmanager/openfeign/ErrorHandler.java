package com.example.newsmanager.openfeign;

import com.example.utils.entities.NewsApiError;
import com.example.utils.entities.NewsDataError;
import com.example.utils.exception.NewsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;

public class ErrorHandler implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        String url = response.request().url(); // 获取当前请求的 URL

        try {
            // Interpret the NewsError object from the openfeign response
            if (url.contains("newsapi.org")) {
                NewsApiError error = objectMapper.readValue(response.body().asInputStream(), NewsApiError.class);
                return new NewsException(error.getCode(), error.getMessage());
            }
            // Interpret the NewsDataError object from the openfeign response
            else if (url.contains("newsdata.io")) {
                NewsDataError error = objectMapper.readValue(response.body().asInputStream(), NewsDataError.class);
                return new NewsException(error.getResults().getCode(), error.getResults().getMessage());
            }
        } catch (IOException e) {
            return new RuntimeException("Cannot parse error response: " + e.getMessage());
        }
        return new RuntimeException("Unknown API error");
    }
}
