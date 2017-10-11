package com.blackducksoftware.integration.hub.notification.batch.digest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.notification.channel.AbstractJmsTemplate;
import com.blackducksoftware.integration.hub.notification.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.notification.event.AbstractChannelEvent;
import com.google.gson.Gson;

public class DigestItemWriter implements ItemWriter<List<AbstractChannelEvent>> {
    private final static Logger logger = LoggerFactory.getLogger(DigestItemWriter.class);
    private final ChannelTemplateManager channelTemplateManager;
    private final Gson gson;

    @Autowired
    public DigestItemWriter(final ChannelTemplateManager channelTemplateManager, final Gson gson) {
        this.channelTemplateManager = channelTemplateManager;
        this.gson = gson;
    }

    @Override
    public void write(final List<? extends List<AbstractChannelEvent>> eventList) throws Exception {
        logger.info("Digest Item Writer called");
        eventList.forEach(channelEventList -> {
            channelEventList.forEach(event -> {
                final String destination = event.getTopic();
                if (channelTemplateManager.hasTemplate(destination)) {
                    final String jsonMessage = gson.toJson(event);
                    final AbstractJmsTemplate template = channelTemplateManager.getTemplate(destination);
                    template.convertAndSend(destination, jsonMessage);
                }
            });
        });
    }
}
