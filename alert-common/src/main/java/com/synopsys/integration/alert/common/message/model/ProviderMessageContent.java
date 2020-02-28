/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.message.model;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.builder.Buildable;
import com.synopsys.integration.datastructure.SetMap;

public class ProviderMessageContent extends AlertSerializableModel implements Buildable {
    public static final String LABEL_PROVIDER = "Provider";
    private static final long serialVersionUID = -9019185621384719085L;

    private final LinkableItem provider;
    private final LinkableItem topic;
    private final LinkableItem subTopic;
    private final ContentKey contentKey;

    private final ItemOperation action;
    private final Long notificationId;

    private final Set<ComponentItem> componentItems;
    private final Date providerCreationTime;
    private final Long providerConfigId;

    private ProviderMessageContent(LinkableItem provider, LinkableItem topic, LinkableItem subTopic, ContentKey contentKey, ItemOperation action, Long notificationId, Set<ComponentItem> componentItems,
        Date providerCreationTime, Long providerConfigId) {
        this.provider = provider;
        this.topic = topic;
        this.subTopic = subTopic;
        this.contentKey = contentKey;
        this.action = action;
        this.notificationId = notificationId;
        this.componentItems = componentItems;
        this.providerCreationTime = providerCreationTime;
        this.providerConfigId = providerConfigId;
    }

    public LinkableItem getProvider() {
        return provider;
    }

    public LinkableItem getTopic() {
        return topic;
    }

    public Optional<LinkableItem> getSubTopic() {
        return Optional.ofNullable(subTopic);
    }

    public ContentKey getContentKey() {
        return contentKey;
    }

    public Optional<ItemOperation> getAction() {
        return Optional.ofNullable(action);
    }

    public Optional<Long> getNotificationId() {
        return Optional.ofNullable(notificationId);
    }

    /**
     * Indicates whether the information conveyed in this ProviderMessageContent is only relevant to the topic (and subTopic) rather than to the componentItems.
     */
    public boolean isTopLevelActionOnly() {
        return getAction().isPresent() && getNotificationId().isPresent() && getComponentItems().isEmpty();
    }

    public Set<ComponentItem> getComponentItems() {
        return componentItems;
    }

    public Date getProviderCreationTime() {
        return providerCreationTime;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    /**
     * Creates a logical grouping of ComponentItems using ComponentItem.createKey()
     */
    public SetMap<String, ComponentItem> groupRelatedComponentItems() {
        return groupRelatedComponentItems(true);
    }

    /**
     * Creates a logical grouping of ComponentItems using ComponentItem.createKey()
     * @param includeOperation Indicates whether or not to include operation in the key.
     */
    public SetMap<String, ComponentItem> groupRelatedComponentItems(boolean includeOperation) {
        SetMap<String, ComponentItem> componentItemSetMap = SetMap.createLinked();
        for (ComponentItem componentItem : componentItems) {
            String key = componentItem.createKey(includeOperation, false);
            componentItemSetMap.add(key, componentItem);
        }
        return componentItemSetMap;
    }

    public static class Builder {
        private final Set<ComponentItem> componentItems = new LinkedHashSet<>();
        private String providerName;
        private String providerConfigName;
        private String providerUrl;
        private String topicName;
        private String topicValue;
        private String topicUrl;
        private String subTopicName;
        private String subTopicValue;
        private String subTopicUrl;
        private ItemOperation action;
        private Long notificationId;
        private Date providerCreationTime;
        private Long providerConfigId;

        public ProviderMessageContent build() throws AlertException {
            if (null == providerName || null == providerConfigId || null == providerConfigName || null == topicName || null == topicValue) {
                throw new AlertException("Missing required field(s)");
            }

            LinkableItem provider = new LinkableItem(providerName, providerConfigName, providerUrl);
            LinkableItem topic = new LinkableItem(topicName, topicValue, topicUrl);
            LinkableItem subTopic = null;
            if (StringUtils.isNotBlank(subTopicName) && StringUtils.isNotBlank(subTopicValue)) {
                subTopic = new LinkableItem(subTopicName, subTopicValue, subTopicUrl);
            }
            ContentKey key = ContentKey.of(providerName, providerConfigId, topicName, topicValue, subTopicName, subTopicValue, action);
            return new ProviderMessageContent(provider, topic, subTopic, key, action, notificationId, componentItems, providerCreationTime, providerConfigId);
        }

        public ContentKey getCurrentContentKey() {
            return ContentKey.of(providerName, providerConfigId, topicName, topicValue, subTopicName, subTopicValue, action);
        }

        public Builder applyCommonData(CommonMessageData commonMessageData) {
            return applyNotificationId(commonMessageData.getNotificationId())
                       .applyProvider(commonMessageData.getProviderName(), commonMessageData.getProviderConfigId(), commonMessageData.getProviderConfigName(), commonMessageData.getProviderURL())
                       .applyProviderCreationTime(commonMessageData.getProviderCreationDate());
        }

        public Builder applyProvider(String providerName, Long providerConfigId, String providerConfigName) {
            this.providerName = providerName;
            this.providerConfigId = providerConfigId;
            this.providerConfigName = providerConfigName;
            return this;
        }

        public Builder applyProvider(String providerName, Long providerConfigId, String providerConfigName, String providerUrl) {
            this.providerName = providerName;
            this.providerConfigId = providerConfigId;
            this.providerConfigName = providerConfigName;
            this.providerUrl = providerUrl;
            return this;
        }

        public Builder applyProviderUrl(String providerUrl) {
            this.providerUrl = providerUrl;
            return this;
        }

        public Builder applyTopic(String topicName, String topicValue) {
            this.topicName = topicName;
            this.topicValue = topicValue;
            return this;
        }

        public Builder applyTopic(String topicName, String topicValue, String topicUrl) {
            this.topicName = topicName;
            this.topicValue = topicValue;
            this.topicUrl = topicUrl;
            return this;
        }

        public Builder applyTopicUrl(String topicUrl) {
            this.topicUrl = topicUrl;
            return this;
        }

        public Builder applySubTopic(String subTopicName, String subTopicValue) {
            this.subTopicName = subTopicName;
            this.subTopicValue = subTopicValue;
            return this;
        }

        public Builder applySubTopic(String subTopicName, String subTopicValue, String subTopicUrl) {
            this.subTopicName = subTopicName;
            this.subTopicValue = subTopicValue;
            this.subTopicUrl = subTopicUrl;
            return this;
        }

        public Builder applySubTopicUrl(String subTopicUrl) {
            this.subTopicUrl = subTopicUrl;
            return this;
        }

        public Builder applyAction(ItemOperation action) {
            this.action = action;
            return this;
        }

        public Builder applyNotificationId(Long notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder applyComponentItem(ComponentItem componentItem) {
            this.componentItems.add(componentItem);
            return this;
        }

        public Builder applyAllComponentItems(Collection<ComponentItem> componentItems) {
            this.componentItems.addAll(componentItems);
            return this;
        }

        public Builder applyEarliestProviderCreationTime(Date providerCreationTime) {
            if (null == this.providerCreationTime) {
                return applyProviderCreationTime(providerCreationTime);
            }

            if (this.providerCreationTime.getTime() > providerCreationTime.getTime()) {
                return applyProviderCreationTime(providerCreationTime);
            }
            return this;
        }

        public Builder applyProviderCreationTime(Date providerCreationTime) {
            this.providerCreationTime = providerCreationTime;
            return this;
        }

    }

}
