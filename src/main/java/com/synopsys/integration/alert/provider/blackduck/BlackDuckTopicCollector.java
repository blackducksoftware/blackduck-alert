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
package com.synopsys.integration.alert.provider.blackduck;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.alert.common.model.CategoryItem;
import com.synopsys.integration.alert.common.model.CategoryKey;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.common.model.TopicContent;
import com.synopsys.integration.alert.common.workflow.processor.TopicCollector;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.workflow.filter.JsonExtractor;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

@Component
public class BlackDuckTopicCollector extends TopicCollector {

    @Autowired
    public BlackDuckTopicCollector(final JsonExtractor jsonExtractor, final BlackDuckDescriptor blackDuckDescriptor) {
        super(jsonExtractor, blackDuckDescriptor);
    }

    @Override
    public void insert(final NotificationContent notification) {
        final TopicContent content = getContentOrCreateIfDoesNotExist(notification);
        addCategoryItems(content, notification);
        addContent(content);
    }

    @Override
    public List<TopicContent> collect(final FormatType format) {
        // TODO implement
        return getCopyOfCollectedContent();
    }

    private void addCategoryItems(final TopicContent content, final NotificationContent notification) {
        final List<CategoryItem> categoryItems = content.getCategoryItemList();
        final List<HierarchicalField> notificationFields = getFieldsForNotificationType(notification.getNotificationType());
        final List<HierarchicalField> categoryFields = notificationFields
                                                           .parallelStream()
                                                           .filter(field -> field.getLabel().startsWith(HierarchicalField.LABEL_CATEGORY_ITEM_PREFIX))
                                                           .collect(Collectors.toList());

        // FIXME create this key correctly
        final CategoryKey key = CategoryKey.from(notification.getNotificationType());
        for (final HierarchicalField field : categoryFields) {
            final Optional<String> fieldValue = getOptionalFieldValue(field, notification.getContent());
            if (fieldValue.isPresent()) {
                final String fieldName = field.getFieldKey();
                final Optional<HierarchicalField> urlField = getRelatedUrlField(field, categoryFields);

                Optional<String> fieldUrl = Optional.empty();
                if (urlField.isPresent()) {
                    fieldUrl = getOptionalFieldValue(urlField.get(), notification.getContent());
                }

                final ItemOperation op = getOperationFromNotification(notification);
                // TODO figure out how we will know how to properly get this stuff
                final LinkableItem fieldItem = new LinkableItem(fieldName, fieldValue.get(), fieldUrl.orElse(null));
                final CategoryItem categoryItem = new CategoryItem(key, op, Arrays.asList(fieldItem));
                categoryItems.add(categoryItem);
            }
        }
    }

    private ItemOperation getOperationFromNotification(final NotificationContent notification) {
        final String notificationType = notification.getNotificationType();
        if (NotificationType.RULE_VIOLATION_CLEARED.name().equals(notificationType)) {
            return ItemOperation.DELETE;
        } else if (NotificationType.RULE_VIOLATION.name().equals(notificationType)) {
            return ItemOperation.ADD;
        } else if (NotificationType.POLICY_OVERRIDE.name().equals(notificationType)) {
            return ItemOperation.DELETE;
        } else if (NotificationType.VULNERABILITY.name().equals(notificationType)) {
            // TODO get this from the field(s)
        }
        return ItemOperation.NOOP;
    }
}
