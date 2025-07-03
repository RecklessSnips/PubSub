package com.example.newssubscriber.configuration;

import com.example.utils.Connector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    // Autowiring a connector to connect to the Solace Event Broker
    @Bean
    Connector connector(){
        return new Connector(
                "tcps://mr-connection-7ejhmatgdjk.messaging.mymaas.net:55443",
                "news",
                "solace-cloud-client",
                "3ns866r01o18uqgp5e552roptv");
    }
}
