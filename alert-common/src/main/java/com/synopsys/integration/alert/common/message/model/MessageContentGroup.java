package com.synopsys.integration.alert.common.message.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class MessageContentGroup extends AlertSerializableModel {
    private final List<AggregateMessageContent> subContent;

    private String commonTopicValue;

    public static MessageContentGroup singleton(final AggregateMessageContent message) {
        final MessageContentGroup group = new MessageContentGroup();
        group.add(message);
        return group;
    }

    public MessageContentGroup() {
        this.subContent = new ArrayList<>();
        this.commonTopicValue = null;
    }

    public boolean applies(final AggregateMessageContent message) {
        return null != commonTopicValue && commonTopicValue.equals(message.getValue());
    }

    public void add(final AggregateMessageContent message) {
        final String topicValue = message.getValue();
        if (null == commonTopicValue) {
            this.commonTopicValue = topicValue;
        } else if (!commonTopicValue.equals(message.getValue())) {
            throw new IllegalArgumentException(String.format("The topic of this message did not match the group topic. Expected: %s. Actual: %s.", commonTopicValue, topicValue));
        }
        subContent.add(message);
    }

    public void addAll(final Collection<AggregateMessageContent> messages) {
        messages.forEach(this::add);
    }

    public List<AggregateMessageContent> getSubContent() {
        return subContent;
    }

    public String getCommonTopicValue() {
        return commonTopicValue;
    }

}
