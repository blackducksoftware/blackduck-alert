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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.builder.Buildable;
import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.builder.IntegrationBuilder;

public class ComponentItem implements Buildable {
    private static final String KEY_SEPARATOR = "_";

    private final LinkableItem component;
    private final LinkableItem subComponent;
    private final Set<LinkableItem> componentAttributes;
    private final String category;
    private final String componentKey;
    private final ItemOperation operation;
    private final Long notificationId;

    public ComponentItem(final LinkableItem component, final LinkableItem subComponent, final Set<LinkableItem> componentAttributes, final String category, final String componentKey, final ItemOperation operation,
        final Long notificationId) {
        this.component = component;
        this.subComponent = subComponent;
        this.componentAttributes = componentAttributes;
        this.category = category;
        this.componentKey = componentKey;
        this.operation = operation;
        this.notificationId = notificationId;
    }

    public static final Builder newBuilder() {
        return new Builder();
    }

    public LinkableItem getComponent() {
        return component;
    }

    public LinkableItem getSubComponent() {
        return subComponent;
    }

    public Set<LinkableItem> getComponentAttributes() {
        return componentAttributes;
    }

    public String getCategory() {
        return category;
    }

    public String getComponentKey() {
        return componentKey;
    }

    public ItemOperation getOperation() {
        return operation;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    // TODO IntegrationBuilder seems geared toward UI error messages. We should have complete control over everything we build, so is this the right approach?
    public static class Builder extends IntegrationBuilder<ComponentItem> {
        private String category;
        private String componentKey;
        private List<String> componentKeyParts = new LinkedList<>();
        private String componentName;
        private String componentValue;
        private String componentUrl;
        private String subComponentName;
        private String subComponentValue;
        private String subComponentUrl;
        private ItemOperation operation;
        private Long notificationId;
        private final Set<LinkableItem> componentAttributes = new LinkedHashSet<>();

        @Override
        protected ComponentItem buildWithoutValidation() {
            LinkableItem component = null;
            if (StringUtils.isNotBlank(componentName) && StringUtils.isNotBlank(componentValue)) {
                component = new LinkableItem(componentName, componentValue, componentUrl);
            }

            LinkableItem subComponent = null;
            if (StringUtils.isNotBlank(subComponentName) && StringUtils.isNotBlank(subComponentValue)) {
                subComponent = new LinkableItem(subComponentName, subComponentValue, subComponentUrl);
            }

            final String key;
            if (StringUtils.isNotBlank(componentKey)) {
                key = componentKey;
            } else {
                final List<String> keyParts = new LinkedList<>(componentKeyParts);
                key = String.join(KEY_SEPARATOR, keyParts);
            }
            return new ComponentItem(component, subComponent, componentAttributes, category, key, operation, notificationId);
        }

        @Override
        protected void validate(final BuilderStatus builderStatus) {
            if (StringUtils.isBlank(componentName)) {
                builderStatus.addErrorMessage("Component name required");
            }
            if (StringUtils.isBlank(componentValue)) {
                builderStatus.addErrorMessage("Component name required");
            }
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

        // this takes precedence over the component key prefix parts. If this is set this is used.
        public Builder applyComponentKey(final String componentKey) {
            this.componentKey = componentKey;
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

        public Builder applyAllComponentAttributes(final Collection<LinkableItem> componentAttributes) {
            this.componentAttributes.addAll(componentAttributes);
            return this;
        }
    }
}
