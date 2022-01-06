/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.attachment.compatibility;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class MessageContentGroup extends AlertSerializableModel {
    private final List<ProviderMessageContent> subContent;

    private LinkableItem comonProvider;
    private LinkableItem commonTopic;

    public MessageContentGroup() {
        this.subContent = new LinkedList<>();
        this.commonTopic = null;
    }

    public void add(ProviderMessageContent message) {
        if (null == commonTopic) {
            comonProvider = message.getProvider();
            commonTopic = message.getTopic();
        } else if (!commonTopic.getValue().equals(message.getTopic().getValue())) {
            throw new IllegalArgumentException(String.format("The topic of this message did not match the group topic. Expected: %s. Actual: %s.", commonTopic.getValue(), message.getTopic().getValue()));
        }

        if (commonTopic.getUrl().isEmpty() && message.getTopic().getUrl().isPresent()) {
            commonTopic = message.getTopic();
        }

        subContent.add(message);
    }

    public void addAll(Collection<ProviderMessageContent> messages) {
        messages.forEach(this::add);
    }

    public List<ProviderMessageContent> getSubContent() {
        return subContent;
    }

    public LinkableItem getCommonProvider() {
        return comonProvider;
    }

    public LinkableItem getCommonTopic() {
        return commonTopic;
    }

    public boolean isEmpty() {
        return subContent.isEmpty() || StringUtils.isBlank(commonTopic.getLabel()) || StringUtils.isBlank(commonTopic.getValue());
    }

}
