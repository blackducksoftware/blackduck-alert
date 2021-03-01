/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.common.util;

import java.util.Optional;

import com.synopsys.integration.alert.channel.jira.common.JiraIssueSearchProperties;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueSearchProperties;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class JiraIssuePropertiesUtil {
    private JiraIssuePropertiesUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static IssueSearchProperties create(String providerName, String providerUrl, LinkableItem topic, LinkableItem nullableSubTopic, ComponentItem componentItem, String trackingKey) {
        Optional<LinkableItem> subComponent = componentItem != null ? componentItem.getSubComponent() : Optional.empty();
        String category = componentItem != null ? componentItem.getCategory() : null;
        String subTopicName = nullableSubTopic != null ? nullableSubTopic.getLabel() : null;
        String subTopicValue = nullableSubTopic != null ? nullableSubTopic.getValue() : null;
        String componentName = componentItem != null ? componentItem.getComponent().getLabel() : null;
        String componentValue = componentItem != null ? componentItem.getComponent().getValue() : null;

        return new JiraIssueSearchProperties(providerName, providerUrl, topic.getLabel(), topic.getValue(), subTopicName, subTopicValue,
            category, componentName, componentValue, subComponent.map(LinkableItem::getLabel).orElse(null), subComponent.map(LinkableItem::getValue).orElse(null), trackingKey);
    }

}
