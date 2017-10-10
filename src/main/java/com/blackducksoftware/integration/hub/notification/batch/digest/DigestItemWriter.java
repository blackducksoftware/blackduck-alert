package com.blackducksoftware.integration.hub.notification.batch.digest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

import com.blackducksoftware.integration.hub.notification.event.AbstractChannelEvent;
import com.google.gson.Gson;

public class DigestItemWriter implements ItemWriter<List<AbstractChannelEvent>> {
    private final static Logger logger = LoggerFactory.getLogger(DigestItemWriter.class);
    private final JmsTemplate notificationJmsTemplate;
    private final Gson gson;

    @Autowired
    public DigestItemWriter(final JmsTemplate notificatioJmsTemplate, final Gson gson) {
        this.notificationJmsTemplate = notificatioJmsTemplate;
        this.gson = gson;
    }

    @Override
    public void write(final List<? extends List<AbstractChannelEvent>> eventList) throws Exception {
        logger.info("Real Time Item Writer called");
        eventList.forEach(channelEventList -> {
            channelEventList.forEach(event -> {
                final String jsonMessage = gson.toJson(event);
                notificationJmsTemplate.convertAndSend(event.getTopic(), jsonMessage);
            });
        });
    }
}
