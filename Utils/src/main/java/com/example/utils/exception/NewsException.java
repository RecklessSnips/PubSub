package com.example.utils.exception;

public class NewsException extends RuntimeException{

    private final String code;

    public NewsException(String code, String message){
        super(message);
        this.code = code;
    }

    public String getErrorCode() {
        return code;
    }
}
