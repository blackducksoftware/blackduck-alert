package com.blackducksoftware.integration.hub.notification.channel.hipchat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.notification.channel.DistributionChannel;
import com.google.gson.Gson;

@Component
public class HipChatChannel extends DistributionChannel<String> {
    private final static Logger logger = LoggerFactory.getLogger(HipChatChannel.class);
    private final Gson gson;

    @Autowired
    public HipChatChannel(final Gson gson) {
        this.gson = gson;
    }

    @JmsListener(destination = HipChatChannelConfig.CHANNEL_NAME)
    @Override
    public void recieveMessage(final String message) {
        logger.info("Received hipchat event message: {}", message);
        final HipChatEvent event = gson.fromJson(message, HipChatEvent.class);
        logger.info("HipChat event {}", event);
    }
}
