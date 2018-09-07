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
package com.synopsys.integration.alert.common.workflow.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.alert.common.model.CategoryItem;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.common.model.TopicContent;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.workflow.filter.JsonExtractor;

public abstract class TopicCollector {
    private final JsonExtractor jsonExtractor;
    private final ProviderDescriptor providerDescriptor;
    private final Collection<ProviderContentType> contentTypes;

    private final List<TopicContent> collectedContent;

    public TopicCollector(final JsonExtractor jsonExtractor, final ProviderDescriptor providerDescriptor) {
        this.jsonExtractor = jsonExtractor;
        this.providerDescriptor = providerDescriptor;
        this.contentTypes = providerDescriptor.getProviderContentTypes();

        this.collectedContent = new ArrayList<>();
    }

    public abstract void insert(final NotificationContent notification);

    public abstract List<TopicContent> collect(final FormatType format);

    protected final List<TopicContent> getCopyOfCollectedContent() {
        return Collections.unmodifiableList(collectedContent);
    }

    protected final void addContent(final TopicContent content) {
        collectedContent.add(content);
    }

    protected final TopicContent getContentOrCreateIfDoesNotExist(final NotificationContent notification) {
        final List<HierarchicalField> notificationFields = getFieldsForNotificationType(notification.getNotificationType());
        final String notificationJson = notification.getContent();

        final String topicName = getTopicName(notificationFields);
        final String topicValue = getTopicValue(notificationFields, notificationJson);
        final String topicUrl = getTopicUrl(notificationFields, notificationJson).orElse(null);

        String subTopicName = null;
        String subTopicValue = null;
        LinkableItem subTopic = null;
        if (hasSubTopic(notificationFields)) {
            subTopicName = getSubTopicName(notificationFields).orElse("unknown");
            subTopicValue = getSubTopicValue(notificationFields, notificationJson).orElse("unknown");
            final String subTopicUrl = getSubTopicUrl(notificationFields, notificationJson).orElse(null);

            subTopic = new LinkableItem(subTopicName, subTopicValue, subTopicUrl);
        }

        final TopicContent foundContent = findTopicContent(topicName, topicValue, subTopicName, subTopicValue);
        if (foundContent != null) {
            return foundContent;
        }
        final List<CategoryItem> categoryList = new ArrayList<>();
        return new TopicContent(topicName, topicValue, topicUrl, subTopic, categoryList);
    }

    protected final String getTopicName(final List<HierarchicalField> notificationFields) {
        final HierarchicalField topicField = getFieldForLabel(notificationFields, HierarchicalField.LABEL_TOPIC);
        if (topicField != null) {
            return topicField.getFieldKey();
        }
        throw new IllegalStateException(String.format("The notification provided did not contain the required field: ", HierarchicalField.LABEL_TOPIC));
    }

    protected final String getTopicValue(final List<HierarchicalField> notificationFields, final String notificationJson) {
        final HierarchicalField topicField = getFieldForLabel(notificationFields, HierarchicalField.LABEL_TOPIC);
        if (topicField != null) {
            return getRequiredFieldValue(topicField, notificationJson);
        }
        throw new IllegalStateException(String.format("The notification provided did not contain the required field: ", HierarchicalField.LABEL_TOPIC));
    }

    protected final Optional<String> getTopicUrl(final List<HierarchicalField> notificationFields, final String notificationJson) {
        final HierarchicalField topicUrlField = getFieldForLabel(notificationFields, HierarchicalField.LABEL_TOPIC + HierarchicalField.LABEL_URL_SUFFIX);
        return getOptionalFieldValue(topicUrlField, notificationJson);
    }

    protected final boolean hasSubTopic(final List<HierarchicalField> notificationFields) {
        return notificationFields
                   .stream()
                   .anyMatch(field -> HierarchicalField.LABEL_SUB_TOPIC.equals(field.getLabel()));
    }

    protected final Optional<String> getSubTopicName(final List<HierarchicalField> notificationFields) {
        final HierarchicalField subTopicField = getFieldForLabel(notificationFields, HierarchicalField.LABEL_SUB_TOPIC);
        if (subTopicField != null) {
            return Optional.of(subTopicField.getFieldKey());
        }
        return Optional.empty();
    }

    protected final Optional<String> getSubTopicValue(final List<HierarchicalField> notificationFields, final String notificationJson) {
        final HierarchicalField subTopicField = getFieldForLabel(notificationFields, HierarchicalField.LABEL_SUB_TOPIC);
        return getOptionalFieldValue(subTopicField, notificationJson);
    }

    protected final Optional<String> getSubTopicUrl(final List<HierarchicalField> notificationFields, final String notificationJson) {
        final HierarchicalField subTopicUrlField = getFieldForLabel(notificationFields, HierarchicalField.LABEL_SUB_TOPIC + HierarchicalField.LABEL_URL_SUFFIX);
        return getOptionalFieldValue(subTopicUrlField, notificationJson);
    }

    protected final List<HierarchicalField> getFieldsForNotificationType(final String notificationType) {
        for (final ProviderContentType providerContentType : contentTypes) {
            if (providerContentType.getNotificationType().equals(notificationType)) {
                return providerContentType.getNotificationFields();
            }
        }
        throw new IllegalArgumentException(String.format("No such notification type '%s' for provider: %s", notificationType, providerDescriptor.getName()));
    }

    protected final HierarchicalField getFieldForLabel(final List<HierarchicalField> fields, final String label) {
        for (final HierarchicalField field : fields) {
            if (field.getLabel().equals(label)) {
                return field;
            }
        }
        return null;
    }

    protected final String getRequiredFieldValue(final HierarchicalField field, final String notificationJson) {
        final Optional<String> value = jsonExtractor.getFirstValueFromJson(field, notificationJson);
        if (value.isPresent()) {
            return value.get();
        }
        throw new IllegalStateException(String.format("The required field did not contain a value: ", field));
    }

    protected final Optional<String> getOptionalFieldValue(final HierarchicalField field, final String notificationJson) {
        if (field != null) {
            return jsonExtractor.getFirstValueFromJson(field, notificationJson);
        }
        return Optional.empty();
    }

    protected final Optional<HierarchicalField> getRelatedUrlField(final HierarchicalField relatedField, final List<HierarchicalField> categoryFields) {
        return categoryFields
                   .parallelStream()
                   .filter(field -> field.getLabel().equals(relatedField.getLabel() + HierarchicalField.LABEL_URL_SUFFIX))
                   .findFirst();
    }

    private final TopicContent findTopicContent(final String topicName, final String topicValue, final String subTopicName, final String subTopicValue) {
        for (final TopicContent contentItem : collectedContent) {
            if (contentItem.getName().equals(topicName) && contentItem.getValue().equals(topicValue)) {
                if (contentItem.getSubTopic().isPresent()) {
                    final LinkableItem subTopicItem = contentItem.getSubTopic().get();
                    if (subTopicName != null && subTopicName.equals(subTopicItem.getName()) && subTopicValue != null && subTopicValue.equals(subTopicItem.getValue())) {
                        return contentItem;
                    }
                } else if (subTopicName == null && subTopicValue == null) {
                    return contentItem;
                }
            }
        }
        return null;
    }
}
