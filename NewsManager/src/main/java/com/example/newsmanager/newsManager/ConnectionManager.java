package com.example.newsmanager.newsManager;

import com.example.utils.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConnectionManager {

    /*
        To make sure only connect to the Event Broker once
    */

    private final Connector connector;

    public ConnectionManager(@Autowired Connector connector) {
        this.connector = connector;
        // Blocking connect to Solace Event Broker
        this.connector.connect();
    }

    public Connector getConnector() {
        return connector;
    }
}
