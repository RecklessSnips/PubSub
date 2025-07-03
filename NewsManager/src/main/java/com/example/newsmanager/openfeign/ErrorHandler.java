package com.example.newsmanager.openfeign;

import com.example.utils.entities.NewsError;
import com.example.utils.exception.NewsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;

public class ErrorHandler implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            // Interpret the NewsError object from the openfeign response
            NewsError error = objectMapper.readValue(response.body().asInputStream(), NewsError.class);
            return new NewsException(error.getCode(), error.getMessage());
        } catch (IOException e) {
            return new RuntimeException("Cannot parse error response: " + e.getMessage());
        }
    }
}
