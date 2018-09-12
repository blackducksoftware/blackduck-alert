/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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

package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.field.StringHierarchicalField;
import com.synopsys.integration.alert.common.model.CategoryItem;
import com.synopsys.integration.alert.common.model.CategoryKey;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.common.model.TopicContent;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderContentTypes;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.workflow.filter.JsonExtractor;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

@Component
public class BlackDuckPolicyTopicCollector extends BlackDuckTopicCollector {

    @Autowired
    public BlackDuckPolicyTopicCollector(final JsonExtractor jsonExtractor, final BlackDuckDescriptor blackDuckDescriptor) {
        super(jsonExtractor, blackDuckDescriptor);
    }

    @Override
    protected void addCategoryItemsToContent(final TopicContent content, final NotificationContent notification) {
        final List<CategoryItem> categoryItems = content.getCategoryItemList();
        final List<StringHierarchicalField> categoryFields = getStringFields(notification.getNotificationType());
        final String notificationJson = notification.getContent();

        final List<LinkableItem> componentItems = getLinkableItemsByLabel(categoryFields, BlackDuckProviderContentTypes.LABEL_SUFFIX_COMPONENT_NAME, notificationJson);
        final List<LinkableItem> componentVersionItems = getLinkableItemsByLabel(categoryFields, BlackDuckProviderContentTypes.LABEL_SUFFIX_COMPONENT_VERSION_NAME, notificationJson);
        final List<LinkableItem> policyItems = getLinkableItemsByLabel(categoryFields, BlackDuckProviderContentTypes.LABEL_SUFFIX_POLICY_NAME, notificationJson);

        final ItemOperation operation = getOperationFromNotification(notification.getNotificationType());
        for (final LinkableItem policyItem : policyItems) {
            for (int i = 0; i < componentItems.size(); i++) {
                final String policyUrl = policyItem.getUrl().orElse(null);
                final LinkableItem componentItem = componentItems.get(i);
                final Optional<String> componentUrl = componentItem.getUrl();

                if (componentUrl.isPresent()) {
                    final CategoryKey categoryKey = CategoryKey.from(notification.getNotificationType(), policyUrl, componentUrl.get());
                    addItem(categoryItems, new CategoryItem(categoryKey, operation, asList(policyItem, componentItem)));
                } else {
                    final LinkableItem componentVersionItem = componentVersionItems.get(i);
                    final Optional<String> componentVersionUrl = componentVersionItem.getUrl();
                    if (componentVersionUrl.isPresent()) {
                        final CategoryKey categoryKey = CategoryKey.from(notification.getNotificationType(), policyUrl, componentVersionUrl.get());
                        addItem(categoryItems, new CategoryItem(categoryKey, operation, asList(policyItem, componentVersionItem)));
                    }
                }
            }
        }
    }

    protected ItemOperation getOperationFromNotification(final String notificationType) {
        if (NotificationType.RULE_VIOLATION_CLEARED.name().equals(notificationType)) {
            return ItemOperation.DELETE;
        } else if (NotificationType.RULE_VIOLATION.name().equals(notificationType)) {
            return ItemOperation.ADD;
        } else if (NotificationType.POLICY_OVERRIDE.name().equals(notificationType)) {
            return ItemOperation.DELETE;
        }
        throw new IllegalArgumentException(String.format("The notification type '%s' is not valid for this collector.", notificationType));
    }

    private List<LinkableItem> asList(final LinkableItem... items) {
        final List<LinkableItem> list = new ArrayList<>();
        if (items != null) {
            for (final LinkableItem item : items) {
                list.add(item);
            }
        }
        return list;
    }
}
