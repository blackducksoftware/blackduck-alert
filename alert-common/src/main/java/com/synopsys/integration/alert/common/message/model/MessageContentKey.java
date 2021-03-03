/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.message.model;

import java.util.Set;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public final class MessageContentKey extends AlertSerializableModel {
    private static final String SEPARATOR = "_";
    private final String key;

    public static MessageContentKey from(final String topicName, final String topicValue) {
        final String partialKey = String.format("%s%s%s", topicName, SEPARATOR, topicValue);
        return new MessageContentKey(partialKey);
    }

    public static MessageContentKey from(final String topicName, final String topicValue, final String subTopicName, final String subTopicValue) {
        if (subTopicName == null || subTopicValue == null) {
            return from(topicName, topicValue);
        }
        final String fullKey = String.format("%s_%s_%s_%s", topicName, topicValue, subTopicName, subTopicValue);
        return new MessageContentKey(fullKey);
    }

    public static MessageContentKey from(final String topicName, final String topicValue, final Set<LinkableItem> subTopics) {
        if (null == subTopics || subTopics.isEmpty()) {
            return from(topicName, topicValue);
        }

        final StringBuilder builder = new StringBuilder();
        builder.append(topicName);
        builder.append(SEPARATOR);
        builder.append(topicValue);

        for (final LinkableItem subTopic : subTopics) {
            builder.append(SEPARATOR);
            builder.append(subTopic.getName());
            builder.append(SEPARATOR);
            builder.append(subTopic.getValue());
        }

        return new MessageContentKey(builder.toString());
    }

    private MessageContentKey(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}
