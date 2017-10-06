package com.blackducksoftware.integration.hub.notification.channel;

import javax.jms.ConnectionFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableJms
public class MessageQueueConfig {
    public static final String NOTIFICATION_EVENT_QUEUE = "email_channel";
    public static final String DEFAULT_BROKER_URL = "vm://localhost";

    @Bean
    public JmsTemplate notificationJmsTemplate(final ConnectionFactory connectionFactory) {
        final JmsTemplate template = new JmsTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setDefaultDestinationName(NOTIFICATION_EVENT_QUEUE);
        template.setExplicitQosEnabled(true);
        // Give the messages two minutes before setting them expired
        template.setTimeToLive(1000l * 60 * 2);
        return template;
    }
}
