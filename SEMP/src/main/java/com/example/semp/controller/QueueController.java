package com.example.semp.controller;

import com.example.semp.semp.SempManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
    Get the information in a queue
 */
@RestController
@RequestMapping("/api/semp")
public class QueueController {

    private final SempManager sempManager;

    public QueueController(@Autowired SempManager sempManager){
        this.sempManager = sempManager;
    }

    // Get the number of queued message in a queue
    @GetMapping("/queue/msg")
    public Integer getQueuedMsg() {
        return sempManager.getQueuedMsg();
    }
}
