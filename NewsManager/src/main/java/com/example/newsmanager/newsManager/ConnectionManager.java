package com.example.newsmanager.newsManager;

import com.example.utils.Connector;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConnectionManager {

    /*
        To make sure only connect to the Event Broker once

        Just for the Publisher yet, since there are 2 News Broker instance
    */

    private final Connector connector;

    public ConnectionManager(@Autowired Connector connector) {
        this.connector = connector;
    }

    @PostConstruct
    public void init() {
        // Blocking connect to Solace Event Broker
        this.connector.connect();
        // Since it's blocking connect, so this line can execute
        this.connector.startDirectPublisher();
    }

    public Connector getConnector() {
        return connector;
    }
}
