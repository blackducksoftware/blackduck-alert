package com.blackducksoftware.integration.hub.notification.channel;

import java.util.HashMap;
import java.util.Map;

public class ChannelTemplateManager {
    private final Map<String, AbstractJmsTemplate> jmsTemplateMap;

    public ChannelTemplateManager() {
        jmsTemplateMap = new HashMap<>();
    }

    public boolean hasTemplate(final String destination) {
        return jmsTemplateMap.containsKey(destination);
    }

    public AbstractJmsTemplate getTemplate(final String destination) {
        return jmsTemplateMap.get(destination);
    }

    public void addTemplate(final String destination, final AbstractJmsTemplate template) {
        jmsTemplateMap.put(destination, template);
    }
}
