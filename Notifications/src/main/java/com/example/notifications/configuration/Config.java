package com.example.notifications.configuration;

import com.example.utils.Connector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    Connector connector(){
        return new Connector(
                "tcps://mr-connection-pj7lvlk6kp9.messaging.mymaas.net:55443",
                "elysra",
                "solace-cloud-client",
                "ghl0r9ert82m356hgomr0h16ph");
    }
}
