package com.example.newsmanager.configuration;

import com.example.utils.Connector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    // Autowiring a connector to connect to the Solace Event Broker
    @Bean
    Connector connector(){
        return new Connector(
                "tcps://mr-connection-pj7lvlk6kp9.messaging.mymaas.net:55443",
                "elysra",
                "solace-cloud-client",
                "ghl0r9ert82m356hgomr0h16ph");
    }
}
