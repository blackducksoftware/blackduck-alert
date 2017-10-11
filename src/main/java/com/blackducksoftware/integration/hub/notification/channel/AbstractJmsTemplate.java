package com.blackducksoftware.integration.hub.notification.channel;

import javax.jms.ConnectionFactory;

import org.springframework.jms.core.JmsTemplate;

public abstract class AbstractJmsTemplate extends JmsTemplate {

    public AbstractJmsTemplate(final ConnectionFactory connectionFactory) {
        super();
        this.setConnectionFactory(connectionFactory);
        this.setDefaultDestinationName(getDestinationName());
        this.setExplicitQosEnabled(true);
        // Give the messages two minutes before setting them expired
        this.setTimeToLive(1000l * 60 * 2);
    }

    public abstract String getDestinationName();
}
