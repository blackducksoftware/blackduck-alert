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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.enumeration.ComponentItemPriority;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.builder.Buildable;

public class ComponentItem extends AlertSerializableModel implements Buildable {
    private final LinkableItem component;
    private final LinkableItem subComponent;
    private final Set<LinkableItem> componentAttributes;
    private final ComponentItemPriority priority;
    private final String category;
    private final ComponentKeys componentKeys;
    private final ItemOperation operation;
    private final Set<Long> notificationIds;

    private ComponentItem(LinkableItem component, LinkableItem subComponent, Set<LinkableItem> componentAttributes, ComponentItemPriority priority, String category, ComponentKeys componentKeys, ItemOperation operation,
        Set<Long> notificationIds) {
        this.component = component;
        this.subComponent = subComponent;
        this.componentAttributes = componentAttributes;
        this.priority = priority;
        this.category = category;
        this.componentKeys = componentKeys;
        this.operation = operation;
        this.notificationIds = notificationIds;
    }

    public static Comparator<ComponentItem> createDefaultComparator() {
        return Comparator
                   .comparing(ComponentItem::getCategory)
                   .thenComparing(ComponentItem::getOperation)
                   .thenComparing(ComponentItem::getPriority)
                   .thenComparing(ComponentItem::getComponentKeys);
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

    public ComponentItemPriority getPriority() {
        return priority;
    }

    public String getCategory() {
        return category;
    }

    public ComponentKeys getComponentKeys() {
        return componentKeys;
    }

    public ItemOperation getOperation() {
        return operation;
    }

    public Set<Long> getNotificationIds() {
        return notificationIds;
    }

    /**
     * Intended to be used for display purposes (such as freemarker templates).
     */
    public ComponentAttributeMap getItemsOfSameName() {
        final ComponentAttributeMap map = new ComponentAttributeMap();
        if (null == componentAttributes || componentAttributes.isEmpty()) {
            return map;
        }
        for (final LinkableItem item : componentAttributes) {
            final String name = item.getName();
            map.computeIfAbsent(name, ignored -> new LinkedList<>()).add(item);
        }
        return map;
    }

    public static class Builder {
        private final Set<LinkableItem> componentAttributes = new LinkedHashSet<>();
        private ComponentItemPriority priority;
        private String category;
        private String componentName;
        private String componentValue;
        private String componentUrl;
        private String subComponentName;
        private String subComponentValue;
        private String subComponentUrl;
        private ItemOperation operation;
        private Set<Long> notificationIds = new LinkedHashSet<>();

        public ComponentItem build() throws AlertException {
            if (null == componentName || null == componentValue || null == category || null == operation || (null == notificationIds && !notificationIds.isEmpty())) {
                throw new AlertException("Missing required field(s)");
            }

            final LinkableItem component = new LinkableItem(componentName, componentValue, componentUrl);
            LinkableItem subComponent = null;
            if (StringUtils.isNotBlank(subComponentName) && StringUtils.isNotBlank(subComponentValue)) {
                subComponent = new LinkableItem(subComponentName, subComponentValue, subComponentUrl);
            }

            final String additionalDataString = ComponentKeys.generateAdditionalDataString(componentAttributes);
            ComponentKeys key = new ComponentKeys(category, componentName, componentValue, subComponentName, subComponentValue, additionalDataString);
            ComponentItemPriority componentPriority = ComponentItemPriority.NONE;
            if (null != priority) {
                componentPriority = priority;
            }

            return new ComponentItem(component, subComponent, componentAttributes, componentPriority, category, key, operation, notificationIds);
        }

        public Builder applyComponentData(LinkableItem component) {
            if (null != component) {
                this.componentName = component.getName();
                this.componentValue = component.getValue();
                this.componentUrl = component.getUrl().orElse(null);
            }
            return this;
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

        public Builder applySubComponent(LinkableItem subComponent) {
            if (null != subComponent) {
                this.subComponentName = subComponent.getName();
                this.subComponentValue = subComponent.getValue();
                this.subComponentUrl = subComponent.getUrl().orElse(null);
            }
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

        public Builder applyPriority(ComponentItemPriority priority) {
            this.priority = priority;
            return this;
        }

        public Builder applyCategory(final String category) {
            this.category = category;
            return this;
        }

        public Builder applyOperation(final ItemOperation operation) {
            this.operation = operation;
            return this;
        }

        public Builder applyNotificationId(final Long notificationId) {
            this.notificationIds.add(notificationId);
            return this;
        }

        public Builder applyNotificationIds(final Collection<Long> notificationIds) {
            this.notificationIds.addAll(notificationIds);
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
