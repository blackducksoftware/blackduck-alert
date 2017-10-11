package com.blackducksoftware.integration.hub.notification.channel.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.notification.channel.DistributionChannel;
import com.google.gson.Gson;

@Component
public class EmailChannel extends DistributionChannel<String> {
    private final static Logger logger = LoggerFactory.getLogger(EmailChannel.class);
    private final Gson gson;

    @Autowired
    public EmailChannel(final Gson gson) {
        this.gson = gson;
    }

    @JmsListener(destination = EmailChannelConfig.CHANNEL_NAME)
    @Override
    public void recieveMessage(final String message) {
        logger.info("Received email event message: {}", message);
        final EmailEvent emailEvent = gson.fromJson(message, EmailEvent.class);
        logger.info("Email event {}", emailEvent);
    }
}
