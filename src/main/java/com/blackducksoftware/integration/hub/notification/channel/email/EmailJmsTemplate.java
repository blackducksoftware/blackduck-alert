package com.blackducksoftware.integration.hub.notification.channel.email;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.notification.channel.AbstractJmsTemplate;

@Component
public class EmailJmsTemplate extends AbstractJmsTemplate {

    @Autowired
    public EmailJmsTemplate(final ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public String getDestinationName() {
        return EmailChannelConfig.CHANNEL_NAME;
    }
}
