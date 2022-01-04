/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.attachment.compatibility;

import java.util.Set;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public final class MessageContentKey extends AlertSerializableModel {
    private static final String SEPARATOR = "_";
    private final String key;

    public static MessageContentKey from(String topicName, String topicValue) {
        String partialKey = String.format("%s%s%s", topicName, SEPARATOR, topicValue);
        return new MessageContentKey(partialKey);
    }

    public static MessageContentKey from(String topicName, String topicValue, String subTopicName, String subTopicValue) {
        if (subTopicName == null || subTopicValue == null) {
            return from(topicName, topicValue);
        }
        String fullKey = String.format("%s_%s_%s_%s", topicName, topicValue, subTopicName, subTopicValue);
        return new MessageContentKey(fullKey);
    }

    public static MessageContentKey from(String topicName, String topicValue, Set<LinkableItem> subTopics) {
        if (null == subTopics || subTopics.isEmpty()) {
            return from(topicName, topicValue);
        }

        StringBuilder builder = new StringBuilder();
        builder.append(topicName);
        builder.append(SEPARATOR);
        builder.append(topicValue);

        for (LinkableItem subTopic : subTopics) {
            builder.append(SEPARATOR);
            builder.append(subTopic.getLabel());
            builder.append(SEPARATOR);
            builder.append(subTopic.getValue());
        }

        return new MessageContentKey(builder.toString());
    }

    private MessageContentKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}
