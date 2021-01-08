/**
 * alert-common
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
package com.synopsys.integration.alert.common.message.model;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.builder.Buildable;
import com.synopsys.integration.datastructure.SetMap;

public class ProviderMessageContent extends AlertSerializableModel implements Buildable {
    private static final long serialVersionUID = -9019185621384719085L;

    @Deprecated(since = "6.5.0")
    private static final String PROJECT_SERIALIZATION_NAME = "topic";
    @Deprecated(since = "6.5.0")
    private static final String SUB_PROJECT_SERIALIZATION_NAME = "subTopic";

    private final LinkableItem provider;
    @JsonProperty(PROJECT_SERIALIZATION_NAME)
    @SerializedName(PROJECT_SERIALIZATION_NAME)
    private final LinkableItem project;
    @JsonProperty(SUB_PROJECT_SERIALIZATION_NAME)
    @SerializedName(SUB_PROJECT_SERIALIZATION_NAME)
    private final LinkableItem projectVersion;
    private final ContentKey contentKey;

    private final ItemOperation action;
    private final Long notificationId;

    private final Set<ComponentItem> componentItems;
    private final OffsetDateTime providerCreationTime;
    private final Long providerConfigId;

    private ProviderMessageContent(LinkableItem provider, LinkableItem project, LinkableItem projectVersion, ContentKey contentKey, ItemOperation action, Long notificationId, Set<ComponentItem> componentItems,
        OffsetDateTime providerCreationTime, Long providerConfigId) {
        this.provider = provider;
        this.project = project;
        this.projectVersion = projectVersion;
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

    public LinkableItem getProject() {
        return project;
    }

    public Optional<LinkableItem> getProjectVersion() {
        return Optional.ofNullable(projectVersion);
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

    public OffsetDateTime getProviderCreationTime() {
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
        private String projectLabel;
        private String projectName;
        private String projectUrl;
        private String projectVersionLabel;
        private String projectVersionName;
        private String projectVersionUrl;
        private ItemOperation action;
        private Long notificationId;
        private OffsetDateTime providerCreationTime;
        private Long providerConfigId;

        public ProviderMessageContent build() throws AlertException {
            if (null == providerName || null == providerConfigId || null == providerConfigName || null == projectLabel || null == projectName) {
                throw new AlertException("Missing required field(s)");
            }

            LinkableItem provider = new LinkableItem(providerName, providerConfigName, providerUrl);
            LinkableItem projectItem = new LinkableItem(projectLabel, projectName, projectUrl);
            LinkableItem projectVersionItem = null;
            if (StringUtils.isNotBlank(projectVersionLabel) && StringUtils.isNotBlank(projectVersionName)) {
                projectVersionItem = new LinkableItem(projectVersionLabel, projectVersionName, projectVersionUrl);
            }
            ContentKey key = ContentKey.of(providerName, providerConfigId, projectLabel, projectName, projectVersionLabel, projectVersionName, action);
            return new ProviderMessageContent(provider, projectItem, projectVersionItem, key, action, notificationId, componentItems, providerCreationTime, providerConfigId);
        }

        public ContentKey getCurrentContentKey() {
            return ContentKey.of(providerName, providerConfigId, projectLabel, projectName, projectVersionLabel, projectVersionName, action);
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

        public Builder applyProject(String projectLabel, String projectName) {
            this.projectLabel = projectLabel;
            this.projectName = projectName;
            return this;
        }

        public Builder applyProject(String projectLabel, String projectName, String projectUrl) {
            this.projectLabel = projectLabel;
            this.projectName = projectName;
            this.projectUrl = projectUrl;
            return this;
        }

        public Builder applyProjectUrl(String projectUrl) {
            this.projectUrl = projectUrl;
            return this;
        }

        public Builder applyProjectVersion(String projectVersionLabel, String projectVersionName) {
            this.projectVersionLabel = projectVersionLabel;
            this.projectVersionName = projectVersionName;
            return this;
        }

        public Builder applyProjectVersion(String projectVersionLabel, String projectVersionName, String projectVersionUrl) {
            this.projectVersionLabel = projectVersionLabel;
            this.projectVersionName = projectVersionName;
            this.projectVersionUrl = projectVersionUrl;
            return this;
        }

        public Builder applyProjectVersionUrl(String projectVersionUrl) {
            this.projectVersionUrl = projectVersionUrl;
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

        public Builder applyEarliestProviderCreationTime(OffsetDateTime providerCreationTime) {
            if (null == this.providerCreationTime) {
                return applyProviderCreationTime(providerCreationTime);
            }

            if (this.providerCreationTime.toLocalTime().compareTo(providerCreationTime.toLocalTime()) > 0) {
                return applyProviderCreationTime(providerCreationTime);
            }
            return this;
        }

        public Builder applyProviderCreationTime(OffsetDateTime providerCreationTime) {
            this.providerCreationTime = providerCreationTime;
            return this;
        }

    }

}
