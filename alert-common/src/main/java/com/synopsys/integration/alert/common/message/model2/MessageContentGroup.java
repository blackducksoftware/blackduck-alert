package com.synopsys.integration.alert.common.message.model2;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class MessageContentGroup extends AlertSerializableModel {
    private final List<ProviderMessageContent> subContent;

    private LinkableItem commonTopic;

    public static MessageContentGroup singleton(final ProviderMessageContent message) {
        final MessageContentGroup group = new MessageContentGroup();
        group.add(message);
        return group;
    }

    public MessageContentGroup() {
        this.subContent = new LinkedList<>();
        this.commonTopic = null;
    }

    public boolean applies(final ProviderMessageContent message) {
        return null != commonTopic && commonTopic.getValue().equals(message.getTopic().getValue());
    }

    public void add(final ProviderMessageContent message) {
        if (null == commonTopic) {
            this.commonTopic = message.getTopic();
        } else if (!commonTopic.getValue().equals(message.getTopic().getValue())) {
            throw new IllegalArgumentException(String.format("The topic of this message did not match the group topic. Expected: %s. Actual: %s.", commonTopic.getValue(), message.getTopic().getValue()));
        }
        subContent.add(message);
    }

    public void addAll(final Collection<ProviderMessageContent> messages) {
        messages.forEach(this::add);
    }

    public List<ProviderMessageContent> getSubContent() {
        return subContent;
    }

    public LinkableItem getCommonTopic() {
        return commonTopic;
    }

    public boolean isEmpty() {
        return subContent.isEmpty() || StringUtils.isBlank(commonTopic.getName()) || StringUtils.isBlank(commonTopic.getValue());
    }

}
