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
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.synopsys.integration.alert.common.enumeration.ComponentItemPriority;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.builder.Buildable;

public class ComponentItem extends AlertSerializableModel implements Buildable {
    private static final String[] EXCLUDED_COMPARISON_FIELDS = { "notificationIds" };

    private String category;
    private ItemOperation operation;
    private ComponentItemPriority priority;

    private LinkableItem component;
    private LinkableItem subComponent;

    private LinkableItem categoryItem;
    private LinkableItem categoryGroupingAttribute;
    private boolean collapseOnCategory;

    private LinkedHashSet<LinkableItem> componentAttributes;
    private Set<Long> notificationIds;

    private ComponentItem(
        String category,
        ItemOperation operation,
        ComponentItemPriority priority,
        LinkableItem component,
        LinkableItem subComponent,
        LinkableItem categoryItem,
        LinkableItem categoryGroupingAttribute,
        boolean collapseOnCategory,
        LinkedHashSet<LinkableItem> componentAttributes,
        Set<Long> notificationIds
    ) {
        this.category = category;
        this.operation = operation;
        this.priority = priority;
        this.component = component;
        this.subComponent = subComponent;
        this.categoryItem = categoryItem;
        this.categoryGroupingAttribute = categoryGroupingAttribute;
        this.collapseOnCategory = collapseOnCategory;
        this.componentAttributes = componentAttributes;
        this.notificationIds = notificationIds;
    }

    public static Comparator<ComponentItem> createDefaultComparator() {
        return Comparator
                   .comparing(ComponentItem::getCategory)
                   .thenComparing(ComponentItem::getOperation)
                   .thenComparing(ComponentItem::getPriority)
                   .thenComparing(ComponentItem::getComponent)
                   .thenComparing(ComponentItem::getSubComponent, ComponentItem::compareOptionalItems)
                   .thenComparing(ComponentItem::getCategoryItem);
    }

    private static int compareOptionalItems(Optional<LinkableItem> optionalItem1, Optional<LinkableItem> optionalItem2) {
        if (optionalItem1.isPresent()) {
            return optionalItem2
                       .map(item2 -> optionalItem1.get().compareTo(item2))
                       .orElse(1);
        } else if (optionalItem2.isPresent()) {
            return -1;
        } else {
            return 0;
        }
    }

    public String getCategory() {
        return category;
    }

    public ItemOperation getOperation() {
        return operation;
    }

    public ComponentItemPriority getPriority() {
        return priority;
    }

    public LinkableItem getComponent() {
        return component;
    }

    public Optional<LinkableItem> getSubComponent() {
        return Optional.ofNullable(subComponent);
    }

    public LinkableItem getCategoryItem() {
        return categoryItem;
    }

    public Optional<LinkableItem> getCategoryGroupingAttribute() {
        return Optional.ofNullable(categoryGroupingAttribute);
    }

    public boolean getCollapseOnCategory() {
        return collapseOnCategory;
    }

    /**
     * Alias for getCollapseOnCategory()
     */
    public boolean collapseOnCategory() {
        return getCollapseOnCategory();
    }

    public Set<LinkableItem> getComponentAttributes() {
        return componentAttributes;
    }

    public Set<Long> getNotificationIds() {
        return notificationIds;
    }

    /**
     * Intended to be used for logical grouping of ComponentItems. By default, operation is included and categoryItem is only included if collapseOnCategoryItem is false.
     * @return A String that will identify this ComponentItem by category, operation, component, subComponent, and categoryItem if applicable.
     */
    public String createKey() {
        return createKey(true, false);
    }

    /**
     * Intended to be used for logical grouping of ComponentItems.
     * @param includeOperation         Indicates whether or not to include operation in the key.
     * @param forceIncludeCategoryItem By default, if collapseOnCategory() returns true, categoryItem will be excluded from the key. Setting this to true will always include it.
     * @return A String that will identify this ComponentItem by category, operation (if applicable), component, subComponent, and categoryItem (if applicable).
     */
    public String createKey(boolean includeOperation, boolean forceIncludeCategoryItem) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(getCategory());
        if (includeOperation) {
            keyBuilder.append(getOperation());
        }

        appendLinkableItem(keyBuilder, getComponent());
        getSubComponent().ifPresent(currentSubComponent -> appendLinkableItem(keyBuilder, currentSubComponent));

        if (!collapseOnCategory() || forceIncludeCategoryItem) {
            appendLinkableItem(keyBuilder, getCategoryItem());
        }

        return keyBuilder.toString();
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, EXCLUDED_COMPARISON_FIELDS);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, EXCLUDED_COMPARISON_FIELDS);
    }

    private void appendLinkableItem(StringBuilder stringBuilder, LinkableItem linkableItem) {
        stringBuilder.append(linkableItem.getName());
        stringBuilder.append(linkableItem.getValue());
        linkableItem.getUrl().ifPresent(stringBuilder::append);
    }

    public static class Builder {
        private final LinkedHashSet<LinkableItem> componentAttributes = new LinkedHashSet<>();
        private String category;
        private ItemOperation operation;
        private ComponentItemPriority priority;

        private String componentName;
        private String componentValue;
        private String componentUrl;
        private String subComponentName;
        private String subComponentValue;
        private String subComponentUrl;
        private boolean collapseOnCategory = false;

        private String categoryItemName;
        private String categoryItemValue;
        private String categoryItemUrl;
        private String categoryGroupingAttributeName;
        private String categoryGroupingAttributeValue;

        private Set<Long> notificationIds = new LinkedHashSet<>();

        public ComponentItem build() throws AlertException {
            if (null == category || null == operation || null == componentName || null == componentValue || null == categoryItemName || null == categoryItemValue || (null == notificationIds || notificationIds.isEmpty())) {
                throw new AlertException("Missing required field(s)");
            }

            LinkableItem component = new LinkableItem(componentName, componentValue, componentUrl);
            LinkableItem subComponent = null;
            if (StringUtils.isNotBlank(subComponentName) && StringUtils.isNotBlank(subComponentValue)) {
                subComponent = new LinkableItem(subComponentName, subComponentValue, subComponentUrl);
            }

            LinkableItem categoryItem = new LinkableItem(categoryItemName, categoryItemValue, categoryItemUrl);
            LinkableItem subCategoryItem = null;
            if (StringUtils.isNotBlank(categoryGroupingAttributeName) && StringUtils.isNotBlank(categoryGroupingAttributeValue)) {
                subCategoryItem = new LinkableItem(categoryGroupingAttributeName, categoryGroupingAttributeValue);
            }

            ComponentItemPriority componentPriority = ComponentItemPriority.NONE;
            if (null != priority) {
                componentPriority = priority;
            }

            return new ComponentItem(category, operation, componentPriority, component, subComponent, categoryItem, subCategoryItem, collapseOnCategory, componentAttributes, notificationIds);
        }

        public Builder applyCategory(String category) {
            this.category = category;
            return this;
        }

        public Builder applyOperation(ItemOperation operation) {
            this.operation = operation;
            return this;
        }

        public Builder applyPriority(ComponentItemPriority priority) {
            this.priority = priority;
            return this;
        }

        public Builder applyComponentData(LinkableItem component) {
            if (null != component) {
                this.componentName = component.getName();
                this.componentValue = component.getValue();
                this.componentUrl = component.getUrl().orElse(null);
            }
            return this;
        }

        public Builder applyComponentData(String componentName, String componentValue) {
            this.componentName = componentName;
            this.componentValue = componentValue;
            return this;
        }

        public Builder applyComponentData(String componentName, String componentValue, String componentUrl) {
            this.componentName = componentName;
            this.componentValue = componentValue;
            applyComponentUrl(componentUrl);
            return this;
        }

        public Builder applyComponentUrl(String componentUrl) {
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

        public Builder applySubComponent(String subComponentName, String subComponentValue) {
            this.subComponentName = subComponentName;
            this.subComponentValue = subComponentValue;
            return this;
        }

        public Builder applySubComponent(String subComponentName, String subComponentValue, String subComponentUrl) {
            this.subComponentName = subComponentName;
            this.subComponentValue = subComponentValue;
            addSubComponentUrl(subComponentUrl);
            return this;
        }

        public Builder addSubComponentUrl(String subComponentUrl) {
            this.subComponentUrl = subComponentUrl;
            return this;
        }

        public Builder applyCategoryItem(LinkableItem categoryItem) {
            if (null != categoryItem) {
                this.categoryItemName = categoryItem.getName();
                this.categoryItemValue = categoryItem.getValue();
                this.categoryItemUrl = categoryItem.getUrl().orElse(null);
            }
            return this;
        }

        public Builder applyCategoryItem(String name, String value) {
            this.categoryItemName = name;
            this.categoryItemValue = value;
            return this;
        }

        public Builder applyCategoryGroupingAttribute(LinkableItem categoryGroupingAttribute) {
            if (null != categoryGroupingAttribute) {
                this.categoryGroupingAttributeName = categoryGroupingAttribute.getName();
                this.categoryGroupingAttributeValue = categoryGroupingAttribute.getValue();
            }
            return this;
        }

        public Builder applyCategoryGroupingAttribute(String name, String value) {
            this.categoryGroupingAttributeName = name;
            this.categoryGroupingAttributeValue = value;
            return this;
        }

        public Builder applyCollapseOnCategory(boolean collapseOnCategory) {
            this.collapseOnCategory = collapseOnCategory;
            return this;
        }

        public Builder applyNotificationId(Long notificationId) {
            this.notificationIds.add(notificationId);
            return this;
        }

        public Builder applyNotificationIds(Collection<Long> notificationIds) {
            this.notificationIds.addAll(notificationIds);
            return this;
        }

        public Builder applyComponentAttribute(LinkableItem componentAttribute) {
            this.componentAttributes.add(componentAttribute);
            return this;
        }

        public Builder applyAllComponentAttributes(Collection<LinkableItem> componentAttributes) {
            this.componentAttributes.addAll(componentAttributes);
            return this;
        }

    }

}
