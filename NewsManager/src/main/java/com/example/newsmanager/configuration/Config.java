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
                "tcps://mr-connection-442yn86oasb.messaging.mymaas.net:55443",
                "news",
                "solace-cloud-client",
                "512k2kpo11favku651dv37lgmk");
    }
}
