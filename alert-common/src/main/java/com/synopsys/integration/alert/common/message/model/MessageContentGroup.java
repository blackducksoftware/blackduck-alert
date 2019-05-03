package com.synopsys.integration.alert.common.message.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class MessageContentGroup extends AlertSerializableModel {
    private final List<AggregateMessageContent> subContent;

    private LinkableItem commonTopic;

    public static MessageContentGroup singleton(final AggregateMessageContent message) {
        final MessageContentGroup group = new MessageContentGroup();
        group.add(message);
        return group;
    }

    public MessageContentGroup() {
        this.subContent = new ArrayList<>();
        this.commonTopic = null;
    }

    public boolean applies(final AggregateMessageContent message) {
        return null != commonTopic && commonTopic.getValue().equals(message.getValue());
    }

    public void add(final AggregateMessageContent message) {
        final String topicValue = message.getValue();
        if (null == commonTopic) {
            this.commonTopic = new LinkableItem(message.getName(), message.getValue(), message.getUrl().orElse(null));
        } else if (!commonTopic.getValue().equals(message.getValue())) {
            throw new IllegalArgumentException(String.format("The topic of this message did not match the group topic. Expected: %s. Actual: %s.", commonTopic.getValue(), topicValue));
        }
        subContent.add(message);
    }

    public void addAll(final Collection<AggregateMessageContent> messages) {
        messages.forEach(this::add);
    }

    public List<AggregateMessageContent> getSubContent() {
        return subContent;
    }

    public LinkableItem getCommonTopic() {
        return commonTopic;
    }

    public boolean isEmpty() {
        return subContent.isEmpty();
    }

}
