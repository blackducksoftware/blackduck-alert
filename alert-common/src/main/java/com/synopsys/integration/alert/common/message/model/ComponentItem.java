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
package com.synopsys.integration.alert.common.message.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.builder.Buildable;

public class ComponentItem extends AlertSerializableModel implements Buildable {
    private final LinkableItem component;
    private final LinkableItem subComponent;
    private final Set<LinkableItem> componentAttributes;
    private final String category;
    private final ComponentKey componentKey;
    private final ItemOperation operation;
    private final Long notificationId;

    private ComponentItem(LinkableItem component, LinkableItem subComponent, Set<LinkableItem> componentAttributes, String category, ComponentKey componentKey, ItemOperation operation, Long notificationId) {
        this.component = component;
        this.subComponent = subComponent;
        this.componentAttributes = componentAttributes;
        this.category = category;
        this.componentKey = componentKey;
        this.operation = operation;
        this.notificationId = notificationId;
    }

    public LinkableItem getComponent() {
        return component;
    }

    public Optional<LinkableItem> getSubComponent() {
        return Optional.ofNullable(subComponent);
    }

    public Set<LinkableItem> getComponentAttributes() {
        return componentAttributes;
    }

    public String getCategory() {
        return category;
    }

    public ComponentKey getComponentKey() {
        return componentKey;
    }

    public ItemOperation getOperation() {
        return operation;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    /**
     * Intended to be used for display purposes (such as freemarker templates).
     * @return A map from the name of a LinkableItem to all the LinkableItems with that name.
     */
    public Map<String, List<LinkableItem>> getItemsOfSameName() {
        final Map<String, List<LinkableItem>> map = new LinkedHashMap<>();
        if (null == componentAttributes || componentAttributes.isEmpty()) {
            return map;
        }
        for (final LinkableItem item : componentAttributes) {
            final String name = item.getName();
            map.computeIfAbsent(name, ignored -> new ArrayList<>()).add(item);
        }
        return map;
    }

    public static class Builder {
        private final Set<LinkableItem> componentAttributes = new LinkedHashSet<>();
        private String category;
        private List<String> componentKeyParts = new LinkedList<>();
        private String componentName;
        private String componentValue;
        private String componentUrl;
        private String subComponentName;
        private String subComponentValue;
        private String subComponentUrl;
        private ItemOperation operation;
        private Long notificationId;

        public ComponentItem build() throws AlertException {
            if (null == componentName || null == componentValue || null == category || componentKeyParts.isEmpty() || null == operation || null == notificationId) {
                throw new AlertException("Missing required field(s)");
            }

            final LinkableItem component = new LinkableItem(componentName, componentValue, componentUrl);
            LinkableItem subComponent = null;
            if (StringUtils.isNotBlank(subComponentName) && StringUtils.isNotBlank(subComponentValue)) {
                subComponent = new LinkableItem(subComponentName, subComponentValue, subComponentUrl);
            }

            final String additionalDataString = ComponentKey.generateAdditionalDataString(componentAttributes);
            ComponentKey key = new ComponentKey(category, componentName, componentValue, subComponentName, subComponentValue, additionalDataString);
            return new ComponentItem(component, subComponent, componentAttributes, category, key, operation, notificationId);
        }

        public Builder applyComponentData(final String componentName, final String componentValue) {
            this.componentName = componentName;
            this.componentValue = componentValue;
            return this;
        }

        public Builder applyComponentData(final String componentName, final String componentValue, final String componentUrl) {
            this.componentName = componentName;
            this.componentValue = componentValue;
            applyComponentUrl(componentUrl);
            return this;
        }

        public Builder applyComponentUrl(final String componentUrl) {
            this.componentUrl = componentUrl;
            return this;
        }

        public Builder applySubComponent(final String subComponentName, final String subComponentValue) {
            this.subComponentName = subComponentName;
            this.subComponentValue = subComponentValue;
            return this;
        }

        public Builder applySubComponent(final String subComponentName, final String subComponentValue, final String subComponentUrl) {
            this.subComponentName = subComponentName;
            this.subComponentValue = subComponentValue;
            addSubComponentUrl(subComponentUrl);
            return this;
        }

        public Builder addSubComponentUrl(final String subComponentUrl) {
            this.subComponentUrl = subComponentUrl;
            return this;
        }

        public Builder applyCategory(final String category) {
            this.category = category;
            return this;
        }

        // if the component key isn't set then the component key is constructed by joining these parts with a separator character and the subtopic name and value.
        // This method replaces the key parts
        public Builder setComponentKeyPrefix(final String... keyParts) {
            this.componentKeyParts = new LinkedList<>(Arrays.asList(keyParts));
            return this;
        }

        public Builder applyOperation(final ItemOperation operation) {
            this.operation = operation;
            return this;
        }

        public Builder applyNotificationId(final Long notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder applyComponentAttribute(final LinkableItem componentAttribute) {
            this.componentAttributes.add(componentAttribute);
            return this;
        }

        public Builder applyComponentAttribute(final LinkableItem componentAttribute, boolean isPartOfKey) {
            componentAttribute.setPartOfKey(isPartOfKey);
            this.componentAttributes.add(componentAttribute);
            return this;
        }

        public Builder applyAllComponentAttributes(final Collection<LinkableItem> componentAttributes) {
            this.componentAttributes.addAll(componentAttributes);
            return this;
        }

    }

}
