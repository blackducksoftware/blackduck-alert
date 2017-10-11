package com.blackducksoftware.integration.hub.notification.batch.digest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.notification.channel.AbstractJmsTemplate;
import com.blackducksoftware.integration.hub.notification.event.AbstractChannelEvent;
import com.google.gson.Gson;

public class DigestItemWriter implements ItemWriter<List<AbstractChannelEvent>> {
    private final static Logger logger = LoggerFactory.getLogger(DigestItemWriter.class);
    private final List<AbstractJmsTemplate> jmsTemplateList;
    private final Gson gson;

    @Autowired
    public DigestItemWriter(final List<AbstractJmsTemplate> jmsTemplateList, final Gson gson) {
        this.jmsTemplateList = jmsTemplateList;
        this.gson = gson;
    }

    @Override
    public void write(final List<? extends List<AbstractChannelEvent>> eventList) throws Exception {
        logger.info("Real Time Item Writer called");
        eventList.forEach(channelEventList -> {
            channelEventList.forEach(event -> {
                final String jsonMessage = gson.toJson(event);
                jmsTemplateList.forEach(template -> {
                    if (template.getDestinationName().equals(event.getTopic())) {
                        template.convertAndSend(event.getTopic(), jsonMessage);
                    }
                });
            });
        });
    }
}
