package com.example.semp.configuration;

import com.example.semp.semp.SempManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    // Connect to the SEMP REST API
    @Bean
    public SempManager sempManager() {
        return new SempManager(
                "https://mr-connection-pj7lvlk6kp9.messaging.mymaas.net:943/SEMP/v2/monitor",
                "mission-control-manager",
                "95os6ipifsilrt1jab59uhu16g",
                "elysra",
                "subscription"
        );
    }
}
