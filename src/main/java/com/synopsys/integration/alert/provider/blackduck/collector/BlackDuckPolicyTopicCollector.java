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
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.field.HierarchicalField;
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
        final Map<String, HierarchicalField> categoryFields = getCategoryFieldMap(notification.getNotificationType());
        final String notificationJson = notification.getContent();

        final List<String> componentNames = getFieldValuesByLabelSuffix(categoryFields, BlackDuckProviderContentTypes.LABEL_SUFFIX_COMPONENT_NAME, notificationJson);
        final List<String> componentUrls = getFieldValuesByLabelSuffix(categoryFields, BlackDuckProviderContentTypes.LABEL_SUFFIX_COMPONENT_URL, notificationJson);

        final List<String> componentVersionNames = getFieldValuesByLabelSuffix(categoryFields, BlackDuckProviderContentTypes.LABEL_SUFFIX_COMPONENT_VERSION_NAME, notificationJson);
        final List<String> componentVersionUrls = getFieldValuesByLabelSuffix(categoryFields, BlackDuckProviderContentTypes.LABEL_SUFFIX_COMPONENT_VERSION_URL, notificationJson);

        final List<String> policyNames = getFieldValuesByLabelSuffix(categoryFields, BlackDuckProviderContentTypes.LABEL_SUFFIX_POLICY_NAME, notificationJson);
        final List<String> policyUrls = getFieldValuesByLabelSuffix(categoryFields, BlackDuckProviderContentTypes.LABEL_SUFFIX_POLICY_URL, notificationJson);

        final ItemOperation operation = getOperationFromNotification(notification.getNotificationType());
        for (int policyIndex = 0; policyIndex < policyUrls.size(); policyIndex++) {
            final String policyName = policyNames.get(policyIndex);
            final String policyUrl = policyUrls.get(policyIndex);
            final LinkableItem policyItem = new LinkableItem(BlackDuckProviderContentTypes.LABEL_SUFFIX_POLICY_NAME, policyName, policyUrl);
            for (int componentIndex = 0; componentIndex < componentUrls.size(); componentIndex++) {
                // TODO figure out how to deal with versionless components (url mappings will not be one to one)
            }
            for (int componentVersionIndex = 0; componentVersionIndex < componentVersionUrls.size(); componentVersionIndex++) {
                final String componentName = componentNames.get(componentVersionIndex);
                final String componentVersionName = componentVersionNames.get(componentVersionIndex);
                final String componentVersionUrl = componentVersionUrls.get(componentVersionIndex);

                final CategoryKey categoryKey = CategoryKey.from(notification.getNotificationType(), policyUrl, componentVersionUrl);
                final LinkableItem componentVersionItem = new LinkableItem(BlackDuckProviderContentTypes.LABEL_SUFFIX_COMPONENT_VERSION_NAME, String.format("%s > %s", componentName, componentVersionName), componentVersionUrl);
                addItem(categoryItems, new CategoryItem(categoryKey, operation, asList(policyItem, componentVersionItem)));
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
