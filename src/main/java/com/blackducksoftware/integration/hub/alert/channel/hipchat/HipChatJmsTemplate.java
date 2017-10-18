package com.blackducksoftware.integration.hub.alert.channel.hipchat;

import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.AbstractJmsTemplate;

@Component
public class HipChatJmsTemplate extends AbstractJmsTemplate {

    @Autowired
    public HipChatJmsTemplate(final ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public String getDestinationName() {
        return HipChatChannelConfig.CHANNEL_NAME;
    }

}
