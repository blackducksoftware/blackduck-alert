/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
