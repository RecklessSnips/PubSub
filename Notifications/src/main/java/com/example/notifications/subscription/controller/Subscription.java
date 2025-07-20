package com.example.notifications.subscription.controller;

import com.example.notifications.subscription.SubManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 这个类负责接收用户的订阅，每一次请求都会附带一个订阅源
 */
@RestController
@RequestMapping("/api/subscription")
public class Subscription {

    // Request to create a subscription, return if created successfully
    private final SubManager subManager;

    public Subscription(@Autowired SubManager subManager) {
        this.subManager = subManager;
    }

    // Client 调用这个来进行自己的订阅
    @GetMapping("/subscribe/{source}")
    public ResponseEntity<Boolean> subscribe(@PathVariable("source") String source) {
        System.out.println("Creating subscription on source: " + source);
        subManager.subscribe(source.toLowerCase());
        return ResponseEntity.ok(true);
    }
}
