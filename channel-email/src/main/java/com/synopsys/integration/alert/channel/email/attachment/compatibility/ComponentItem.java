/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.attachment.compatibility;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;
import com.synopsys.integration.builder.Buildable;

public class ComponentItem extends AlertSerializableModel implements Buildable {
    private static final String[] EXCLUDED_COMPARISON_FIELDS = { "notificationIds" };

    private final String category;
    private final ItemOperation operation;
    private final ComponentItemPriority priority;

    private final LinkableItem component;
    private final LinkableItem subComponent;
    private final ComponentItemCallbackInfo callbackInfo;

    private final LinkableItem categoryItem;
    private final LinkableItem categoryGroupingAttribute;
    private final boolean collapseOnCategory;

    private final LinkedHashSet<LinkableItem> componentAttributes;
    private final Set<Long> notificationIds;

    private ComponentItem(
        String category,
        ItemOperation operation,
        ComponentItemPriority priority,
        LinkableItem component,
        LinkableItem subComponent,
        ComponentItemCallbackInfo callbackInfo,
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
        this.callbackInfo = callbackInfo;
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

    public Optional<ComponentItemCallbackInfo> getCallbackInfo() {
        return Optional.ofNullable(callbackInfo);
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

    public LinkedHashSet<LinkableItem> getComponentAttributes() {
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
        stringBuilder.append(linkableItem.getLabel());
        stringBuilder.append(linkableItem.getValue());
        linkableItem.getUrl().ifPresent(stringBuilder::append);
    }

    public static class Builder {
        private final LinkedHashSet<LinkableItem> componentAttributes = new LinkedHashSet<>();
        private String category;
        private ItemOperation operation;
        private ComponentItemPriority priority;

        private String componentLabel;
        private String componentValue;
        private String componentUrl;
        private String subComponentLabel;
        private String subComponentValue;
        private String subComponentUrl;

        private String componentCallbackUrl;
        private ProviderKey componentCallbackProviderKey;
        private String componentCallbackNotificationType;

        private boolean collapseOnCategory = false;

        private String categoryItemLabel;
        private String categoryItemValue;
        private String categoryItemUrl;
        private String categoryGroupingAttributeLabel;
        private String categoryGroupingAttributeValue;

        private final Set<Long> notificationIds = new LinkedHashSet<>();

        public ComponentItem build() throws AlertException {
            if (null == category || null == operation || null == componentLabel || null == componentValue || null == categoryItemLabel || null == categoryItemValue) {
                throw new AlertException("Missing required field(s)");
            }

            LinkableItem component = new LinkableItem(componentLabel, componentValue, componentUrl);
            LinkableItem subComponent = null;
            if (StringUtils.isNotBlank(subComponentLabel) && StringUtils.isNotBlank(subComponentValue)) {
                subComponent = new LinkableItem(subComponentLabel, subComponentValue, subComponentUrl);
            }

            ComponentItemCallbackInfo callbackInfo = null;
            if (StringUtils.isNotBlank(componentCallbackUrl) && componentCallbackProviderKey != null && StringUtils.isNotBlank(componentCallbackNotificationType)) {
                callbackInfo = new ComponentItemCallbackInfo(componentCallbackUrl, componentCallbackProviderKey, componentCallbackNotificationType);
            }

            LinkableItem categoryItem = new LinkableItem(categoryItemLabel, categoryItemValue, categoryItemUrl);
            LinkableItem subCategoryItem = null;
            if (StringUtils.isNotBlank(categoryGroupingAttributeLabel) && StringUtils.isNotBlank(categoryGroupingAttributeValue)) {
                subCategoryItem = new LinkableItem(categoryGroupingAttributeLabel, categoryGroupingAttributeValue);
            }

            ComponentItemPriority componentPriority = ComponentItemPriority.NONE;
            if (null != priority) {
                componentPriority = priority;
            }

            return new ComponentItem(category, operation, componentPriority, component, subComponent, callbackInfo, categoryItem, subCategoryItem, collapseOnCategory, componentAttributes, notificationIds);
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
                this.componentLabel = component.getLabel();
                this.componentValue = component.getValue();
                this.componentUrl = component.getUrl().orElse(null);
            }
            return this;
        }

        public Builder applyComponentData(String componentLabel, String componentValue) {
            this.componentLabel = componentLabel;
            this.componentValue = componentValue;
            return this;
        }

        public Builder applyComponentData(String componentLabel, String componentValue, String componentUrl) {
            this.componentLabel = componentLabel;
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
                this.subComponentLabel = subComponent.getLabel();
                this.subComponentValue = subComponent.getValue();
                this.subComponentUrl = subComponent.getUrl().orElse(null);
            }
            return this;
        }

        public Builder applySubComponent(String subComponentLabel, String subComponentValue) {
            this.subComponentLabel = subComponentLabel;
            this.subComponentValue = subComponentValue;
            return this;
        }

        public Builder applySubComponent(String subComponentLabel, String subComponentValue, String subComponentUrl) {
            this.subComponentLabel = subComponentLabel;
            this.subComponentValue = subComponentValue;
            applySubComponentUrl(subComponentUrl);
            return this;
        }

        public Builder applySubComponentUrl(String subComponentUrl) {
            this.subComponentUrl = subComponentUrl;
            return this;
        }

        public Builder applyComponentItemCallbackInfo(ComponentItemCallbackInfo callbackInfo) {
            if (callbackInfo != null) {
                applyComponentItemCallbackInfo(callbackInfo.getCallbackUrl(), callbackInfo.getProviderKey(), callbackInfo.getNotificationType());
            }
            return this;
        }

        public Builder applyComponentItemCallbackInfo(String callbackUrl, ProviderKey providerKey, String notificationType) {
            this.componentCallbackUrl = callbackUrl;
            this.componentCallbackProviderKey = providerKey;
            this.componentCallbackNotificationType = notificationType;
            return this;
        }

        public Builder applyCategoryItem(LinkableItem categoryItem) {
            if (null != categoryItem) {
                this.categoryItemLabel = categoryItem.getLabel();
                this.categoryItemValue = categoryItem.getValue();
                this.categoryItemUrl = categoryItem.getUrl().orElse(null);
            }
            return this;
        }

        public Builder applyCategoryItem(String label, String value) {
            this.categoryItemLabel = label;
            this.categoryItemValue = value;
            return this;
        }

        public Builder applyCategoryGroupingAttribute(LinkableItem categoryGroupingAttribute) {
            if (null != categoryGroupingAttribute) {
                this.categoryGroupingAttributeLabel = categoryGroupingAttribute.getLabel();
                this.categoryGroupingAttributeValue = categoryGroupingAttribute.getValue();
            }
            return this;
        }

        public Builder applyCategoryGroupingAttribute(String label, String value) {
            this.categoryGroupingAttributeLabel = label;
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
