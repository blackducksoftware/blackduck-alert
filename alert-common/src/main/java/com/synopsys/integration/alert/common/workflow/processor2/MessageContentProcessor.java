/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.workflow.processor2;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.message.model2.MessageContentGroup;

public abstract class MessageContentProcessor {
    private final FormatType formatType;

    public MessageContentProcessor(final FormatType formatType) {
        this.formatType = formatType;
    }

    public FormatType getFormat() {
        return formatType;
    }

    public abstract List<MessageContentGroup> process(final List<ProviderMessageContent> messages);

    protected ProviderMessageContent createNewMessage(ProviderMessageContent oldMessage, Set<ComponentItem> componentItems) throws AlertException {
        LinkableItem provider = oldMessage.getProvider();
        LinkableItem topic = oldMessage.getTopic();
        Optional<LinkableItem> optionalSubTopic = oldMessage.getSubTopic();
        String subTopicName = optionalSubTopic.map(LinkableItem::getName).orElse(null);
        String subTopicValue = optionalSubTopic.map(LinkableItem::getValue).orElse(null);
        String subTopicUrl = optionalSubTopic.flatMap(LinkableItem::getUrl).orElse(null);

        return new ProviderMessageContent.Builder()
                   .applyProvider(provider.getValue(), provider.getUrl().orElse(null))
                   .applyTopic(topic.getName(), topic.getValue(), topic.getUrl().orElse(null))
                   .applySubTopic(subTopicName, subTopicValue, subTopicUrl)
                   .applyAllComponentItems(componentItems)
                   .build();
    }

    protected ComponentItem createNewComponentItem(ComponentItem oldItem, ItemOperation operation, Long notificationId, Collection<LinkableItem> componentAttributes) throws AlertException {
        LinkableItem component = oldItem.getComponent();
        final Optional<LinkableItem> subComponent = oldItem.getSubComponent();
        String subComponentName = subComponent.map(LinkableItem::getName).orElse(null);
        String subComponentValue = subComponent.map(LinkableItem::getValue).orElse(null);
        String subComponentUrl = subComponent.flatMap(LinkableItem::getUrl).orElse(null);
        return new ComponentItem.Builder()
                   .applyCategory(oldItem.getCategory())
                   .applyComponentData(component.getName(), component.getValue(), component.getUrl().orElse(null))
                   .applySubComponent(subComponentName, subComponentValue, subComponentUrl)
                   .applyOperation(operation)
                   .applyNotificationId(notificationId)
                   .applyAllComponentAttributes(componentAttributes)
                   .build();
    }

}
