package com.blackducksoftware.integration.hub.notification.batch.digest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

import com.blackducksoftware.integration.hub.notification.event.AbstractChannelEvent;

public class DigestItemWriter implements ItemWriter<List<AbstractChannelEvent>> {
    private final static Logger logger = LoggerFactory.getLogger(DigestItemWriter.class);
    private final JmsTemplate notificationJmsTemplate;

    @Autowired
    public DigestItemWriter(final JmsTemplate notificatioJmsTemplate) {
        this.notificationJmsTemplate = notificatioJmsTemplate;
    }

    @Override
    public void write(final List<? extends List<AbstractChannelEvent>> eventList) throws Exception {
        logger.info("Real Time Item Writer called");
        eventList.forEach(channelEventList -> {
            channelEventList.forEach(event -> {
                notificationJmsTemplate.convertAndSend(event.getTopic(), event);
            });
        });
    }
}
