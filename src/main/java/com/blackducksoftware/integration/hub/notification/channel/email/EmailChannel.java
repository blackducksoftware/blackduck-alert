package com.blackducksoftware.integration.hub.notification.channel.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.notification.channel.DistributionChannel;

@Component
public class EmailChannel extends DistributionChannel<EmailEvent> {
    private final static Logger logger = LoggerFactory.getLogger(EmailChannel.class);

    @JmsListener(destination = EmailChannelConfig.CHANNEL_NAME)
    @Override
    public void recieveMessage(final EmailEvent message) {
        logger.info("Received email event message: {}", message.getNotificationEntity());
    }
}
