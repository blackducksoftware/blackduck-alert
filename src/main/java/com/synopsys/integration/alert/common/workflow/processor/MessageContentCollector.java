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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.enumeration.FieldContentIdentifier;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.alert.common.field.ObjectHierarchicalField;
import com.synopsys.integration.alert.common.field.StringHierarchicalField;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.CategoryItem;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.common.model.MessageContentKey;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.workflow.filter.JsonExtractor;
import com.synopsys.integration.alert.workflow.filter.JsonFieldAccessor;

public abstract class MessageContentCollector {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final JsonExtractor jsonExtractor;
    private final Collection<ProviderContentType> contentTypes;
    private final Map<FormatType, MessageContentProcessor> messageContentProcessorMap;
    private final Set<String> supportedNotificationTypes;
    private final List<AggregateMessageContent> collectedContent;

    public MessageContentCollector(final JsonExtractor jsonExtractor, final List<MessageContentProcessor> messageContentProcessorList, final Collection<ProviderContentType> contentTypes) {
        this.jsonExtractor = jsonExtractor;
        this.contentTypes = contentTypes;
        this.messageContentProcessorMap = messageContentProcessorList.stream().collect(Collectors.toMap(MessageContentProcessor::getFormat, Function.identity()));
        this.supportedNotificationTypes = contentTypes.stream().map(ProviderContentType::getNotificationType).collect(Collectors.toSet());
        this.collectedContent = new ArrayList<>();
    }

    public Set<String> getSupportedNotificationTypes() {
        return supportedNotificationTypes;
    }

    public void insert(final NotificationContent notification) {
        try {
            final List<HierarchicalField> notificationFields = getFieldsForNotificationType(notification.getNotificationType());
            final JsonFieldAccessor jsonFieldAccessor = createJsonAccessor(notificationFields, notification.getContent());
            final List<AggregateMessageContent> contents = getContentsOrCreateIfDoesNotExist(jsonFieldAccessor, notificationFields);
            for (final AggregateMessageContent content : contents) {
                addCategoryItems(content.getCategoryItemList(), jsonFieldAccessor, notificationFields, notification);
                addContent(content);
            }
        } catch (final IllegalArgumentException ex) {
            final String message = String.format("Error inserting notification into collector: %s", notification);
            logger.error(message, ex);
        }
    }

    public List<AggregateMessageContent> collect(final FormatType format) {
        if (messageContentProcessorMap.containsKey(format)) {
            final MessageContentProcessor processor = messageContentProcessorMap.get(format);
            return processor.process(collectedContent);
        } else {
            return Collections.emptyList();
        }
    }

    protected abstract void addCategoryItems(final List<CategoryItem> categoryItems, final JsonFieldAccessor jsonFieldAccessor, final List<HierarchicalField> notificationFields, final NotificationContent notificationContent);

    protected final List<AggregateMessageContent> getCopyOfCollectedContent() {
        return Collections.unmodifiableList(collectedContent);
    }

    protected final List<StringHierarchicalField> getStringFields(final List<HierarchicalField> fields) {
        return getTypedFields(fields, StringHierarchicalField.class);
    }

    protected final List<ObjectHierarchicalField> getObjectFields(final List<HierarchicalField> fields) {
        return getTypedFields(fields, ObjectHierarchicalField.class);
    }

    protected void addItem(final List<CategoryItem> categoryItems, final CategoryItem newItem) {
        final Optional<CategoryItem> foundItem = categoryItems
                                                     .stream()
                                                     .filter(item -> item.getCategoryKey().equals(newItem.getCategoryKey()))
                                                     .findFirst();
        if (foundItem.isPresent()) {
            final CategoryItem categoryItem = foundItem.get();
            if (categoryItem.getOperation().equals(newItem.getOperation())) {
                categoryItem.getItemList().addAll(newItem.getItemList());
            } else {
                // operation is different treat like another category item
                categoryItems.add(newItem);
            }
        } else {
            categoryItems.add(newItem);
        }
    }

    protected final List<LinkableItem> getLinkableItemsByLabel(final JsonFieldAccessor accessor, final List<StringHierarchicalField> fields, final String label) {
        final Optional<StringHierarchicalField> foundField = getFieldByLabel(fields, label);
        if (foundField.isPresent()) {
            final StringHierarchicalField valueField = foundField.get();
            final Optional<StringHierarchicalField> foundUrlField = getRelatedUrlField(fields, label);
            return createLinkableItemsFromFields(accessor, valueField, foundUrlField.orElse(null));
        }
        return Collections.emptyList();
    }

    protected final <T> List<T> getFieldValueObjectsByLabel(final JsonFieldAccessor accessor, final List<ObjectHierarchicalField> fields, final String label, final Class<T> targetClass) throws AlertException {
        final Optional<ObjectHierarchicalField> field = getFieldByLabel(fields, label);
        if (field.isPresent()) {
            return accessor.get(field.get(), targetClass);
        }
        throw new IllegalStateException(String.format("The list provided did not contain the required field: %s", label));
    }

    protected final List<LinkableItem> createLinkableItemList(final LinkableItem... items) {
        final List<LinkableItem> list = new ArrayList<>();
        if (items != null) {
            for (final LinkableItem item : items) {
                list.add(item);
            }
        }
        return list;
    }

    private List<HierarchicalField> getFieldsForNotificationType(final String notificationType) {
        for (final ProviderContentType providerContentType : contentTypes) {
            if (providerContentType.getNotificationType().equals(notificationType)) {
                return providerContentType.getNotificationFields();
            }
        }
        throw new IllegalArgumentException(String.format("No such notification type '%s' supported; accepted values are: %s", notificationType, String.join(",", getSupportedNotificationTypes())));
    }

    private JsonFieldAccessor createJsonAccessor(final List<HierarchicalField> notificationFields, final String notificationJson) {
        return jsonExtractor.createJsonFieldAccessor(notificationFields, notificationJson);
    }

    private List<AggregateMessageContent> getContentsOrCreateIfDoesNotExist(final JsonFieldAccessor accessor, final List<HierarchicalField> notificationFields) {
        final List<AggregateMessageContent> aggregateMessageContentsForNotifications = new ArrayList<>();

        final List<LinkableItem> topicItems = getTopicItems(accessor, notificationFields);
        final List<LinkableItem> subTopicItems = getSubTopicItems(accessor, notificationFields);
        // for the number of topics assume there is an equal number of sub topics and the order is the same.
        // this seems fragile at the moment.
        final int count = topicItems.size();
        for (int index = 0; index < count; index++) {
            final LinkableItem topicItem = topicItems.get(index);
            final LinkableItem subTopic = subTopicItems.get(index);

            final AggregateMessageContent foundContent = findTopicContent(topicItem.getName(), topicItem.getValue(), subTopic.getName(), subTopic.getValue());
            if (foundContent != null) {
                aggregateMessageContentsForNotifications.add(foundContent);
            } else {
                final List<CategoryItem> categoryList = new ArrayList<>();
                aggregateMessageContentsForNotifications.add(new AggregateMessageContent(topicItem.getName(), topicItem.getValue(), topicItem.getUrl().orElse(null), subTopic, categoryList));
            }
        }
        return aggregateMessageContentsForNotifications;
    }

    private List<LinkableItem> getTopicItems(final JsonFieldAccessor accessor, final List<HierarchicalField> fields) {
        return getLinkableItems(accessor, fields, FieldContentIdentifier.TOPIC, FieldContentIdentifier.TOPIC_URL);
    }

    private List<LinkableItem> getSubTopicItems(final JsonFieldAccessor accessor, final List<HierarchicalField> fields) {
        // TODO is SubTopic something we should require? If not then we should not use the same method to get it.
        return getLinkableItems(accessor, fields, FieldContentIdentifier.SUB_TOPIC, FieldContentIdentifier.SUB_TOPIC_URL);
    }

    private List<LinkableItem> getLinkableItems(final JsonFieldAccessor accessor, final List<HierarchicalField> fields, final FieldContentIdentifier fieldContentIdentifier, final FieldContentIdentifier urlFieldContentIdentifier) {
        final Optional<HierarchicalField> optionalField = getFieldForContentIdentifier(fields, fieldContentIdentifier);
        if (!optionalField.isPresent()) {
            throw new IllegalStateException(String.format("The list provided did not contain the required field: %s", fieldContentIdentifier));
        }
        final Optional<HierarchicalField> optionalUrlField = getFieldForContentIdentifier(fields, urlFieldContentIdentifier);

        // These will always be String fields
        final StringHierarchicalField valueField = (StringHierarchicalField) optionalField.get();
        final StringHierarchicalField urlField = (StringHierarchicalField) optionalUrlField.orElse(null);

        return createLinkableItemsFromFields(accessor, valueField, urlField);
    }

    private AggregateMessageContent findTopicContent(final String topicName, final String topicValue, final String subTopicName, final String subTopicValue) {
        final MessageContentKey contentKey = MessageContentKey.from(topicName, topicValue, subTopicName, subTopicValue);
        for (final AggregateMessageContent contentItem : collectedContent) {
            if (contentKey.equals(contentItem.getKey())) {
                return contentItem;
            }
        }
        return null;
    }

    private Optional<HierarchicalField> getFieldForContentIdentifier(final List<HierarchicalField> fields, final FieldContentIdentifier contentIdentifier) {
        return fields
                   .parallelStream()
                   .filter(field -> contentIdentifier.equals(field.getContentIdentifier()))
                   .findFirst();
    }

    private List<LinkableItem> createLinkableItemsFromFields(final JsonFieldAccessor jsonFieldAccessor, final StringHierarchicalField dataField, final StringHierarchicalField linkField) {
        final List<String> values = jsonFieldAccessor.get(dataField);
        if (linkField != null) {
            final List<String> links = jsonFieldAccessor.get(linkField);
            if (values.size() == links.size()) {
                final List<LinkableItem> linkableItems = new ArrayList<>();
                for (int i = 0; i < links.size(); i++) {
                    linkableItems.add(new LinkableItem(dataField.getLabel(), values.get(i), links.get(i)));
                }
                return linkableItems;
            } else if (!links.isEmpty()) {
                throw new IllegalArgumentException("The json provided did not contain the correct field pairings.");
            }
        }
        return values
                   .parallelStream()
                   .map(value -> new LinkableItem(dataField.getFieldKey(), value))
                   .collect(Collectors.toList());
    }

    private void addContent(final AggregateMessageContent content) {
        for (final AggregateMessageContent existingContent : collectedContent) {
            if (existingContent.equals(content)) {
                // This object is already in the list
                return;
            }
        }
        collectedContent.add(content);
    }

    private <T> List<T> getTypedFields(final List<HierarchicalField> fields, final Class<T> targetClass) {
        return fields
                   .parallelStream()
                   .filter(field -> targetClass.isAssignableFrom(field.getClass()))
                   .map(targetClass::cast)
                   .collect(Collectors.toList());
    }

    private <T extends HierarchicalField> Optional<T> getRelatedUrlField(final List<T> categoryFields, final String label) {
        return categoryFields
                   .parallelStream()
                   .filter(field -> field.getLabel().equals(label + HierarchicalField.LABEL_URL_SUFFIX))
                   .findFirst();
    }

    private <T extends HierarchicalField> Optional<T> getFieldByLabel(final List<T> fields, final String label) {
        return fields
                   .parallelStream()
                   .filter(field -> label.equals(field.getLabel()))
                   .findFirst();
    }
}
