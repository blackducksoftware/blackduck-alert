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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.TypeRef;
import com.synopsys.integration.alert.common.enumeration.FieldContentIdentifier;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.field.JsonField;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.CategoryItem;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.common.model.MessageContentKey;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.workflow.filter.field.JsonFieldAccessor;

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
        this.collectedContent = new Vector<>();
    }

    public Set<String> getSupportedNotificationTypes() {
        return supportedNotificationTypes;
    }

    public void insert(final NotificationContent notification) {
        try {
            final List<JsonField<?>> notificationFields = getFieldsForNotificationType(notification.getNotificationType());
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

    protected abstract void addCategoryItems(final List<CategoryItem> categoryItems, final JsonFieldAccessor jsonFieldAccessor, final List<JsonField<?>> notificationFields, final NotificationContent notificationContent);

    protected final List<AggregateMessageContent> getCopyOfCollectedContent() {
        return Collections.unmodifiableList(collectedContent);
    }

    protected final List<JsonField<String>> getStringFields(final List<JsonField<?>> fields) {
        return getTypedFields(fields, new TypeRef<String>() {});
    }

    protected final List<JsonField<Long>> getLongFields(final List<JsonField<?>> fields) {
        return getTypedFields(fields, new TypeRef<Long>() {});
    }

    protected final <T> List<JsonField<T>> getFieldsOfType(final List<JsonField<?>> fields, final TypeRef<?> typeRef) {
        return getTypedFields(fields, typeRef);
    }

    protected void addItem(final List<CategoryItem> categoryItems, final CategoryItem newItem) {
        final Optional<CategoryItem> foundItem = categoryItems
                                                     .stream()
                                                     .filter(item -> item.getCategoryKey().equals(newItem.getCategoryKey()))
                                                     .findFirst();
        if (foundItem.isPresent()) {
            final CategoryItem categoryItem = foundItem.get();
            if (categoryItem.getOperation().equals(newItem.getOperation())) {
                categoryItem.getItems().addAll(newItem.getItems());
            } else {
                // operation is different treat like another category item
                categoryItems.add(newItem);
            }
        } else {
            categoryItems.add(newItem);
        }
    }

    protected final List<LinkableItem> getLinkableItemsByLabel(final JsonFieldAccessor accessor, final List<JsonField<String>> fields, final String label) {
        final Optional<JsonField<String>> foundField = getFieldByLabel(fields, label);
        if (foundField.isPresent()) {
            final JsonField<String> valueField = foundField.get();
            final Optional<JsonField<String>> foundUrlField = getRelatedUrlField(fields, label);
            return createLinkableItemsFromFields(accessor, valueField, foundUrlField.orElse(null));
        }
        return Collections.emptyList();
    }

    protected final <T> List<T> getFieldValueObjectsByLabel(final JsonFieldAccessor accessor, final List<JsonField<T>> fields, final String label) throws AlertException {
        final Optional<JsonField<T>> field = getFieldByLabel(fields, label);
        if (field.isPresent()) {
            return accessor.get(field.get());
        }
        throw new IllegalStateException(String.format("The list provided did not contain the required field: %s", label));
    }

    protected final SortedSet<LinkableItem> createLinkableItemSet(final LinkableItem... items) {
        final SortedSet<LinkableItem> list = new TreeSet<>();
        if (null != items) {
            for (final LinkableItem item : items) {
                list.add(item);
            }
        }
        return list;
    }

    private List<JsonField<?>> getFieldsForNotificationType(final String notificationType) {
        for (final ProviderContentType providerContentType : contentTypes) {
            if (providerContentType.getNotificationType().equals(notificationType)) {
                return providerContentType.getNotificationFields();
            }
        }
        throw new IllegalArgumentException(String.format("No such notification type '%s' supported; accepted values are: %s", notificationType, String.join(",", getSupportedNotificationTypes())));
    }

    private JsonFieldAccessor createJsonAccessor(final List<JsonField<?>> notificationFields, final String notificationJson) {
        return jsonExtractor.createJsonFieldAccessor(notificationFields, notificationJson);
    }

    private List<AggregateMessageContent> getContentsOrCreateIfDoesNotExist(final JsonFieldAccessor accessor, final List<JsonField<?>> notificationFields) {
        final List<AggregateMessageContent> aggregateMessageContentsForNotifications = new ArrayList<>();

        final List<LinkableItem> topicItems = getTopicItems(accessor, notificationFields);
        if (topicItems.isEmpty()) {
            return Collections.emptyList();
        }
        final List<LinkableItem> subTopicItems = getSubTopicItems(accessor, notificationFields);
        // for the number of topics assume there is an equal number of sub topics and the order is the same.this seems fragile at the moment.
        final int count = topicItems.size();
        for (int index = 0; index < count; index++) {
            final LinkableItem topicItem = topicItems.get(index);

            final LinkableItem subTopic;
            if (!subTopicItems.isEmpty()) {
                subTopic = subTopicItems.get(index);
            } else {
                subTopic = new LinkableItem(null, null);
            }

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

    private List<LinkableItem> getTopicItems(final JsonFieldAccessor accessor, final List<JsonField<?>> fields) {
        return getLinkableItems(accessor, fields, FieldContentIdentifier.TOPIC, FieldContentIdentifier.TOPIC_URL, true);
    }

    private List<LinkableItem> getSubTopicItems(final JsonFieldAccessor accessor, final List<JsonField<?>> fields) {
        return getLinkableItems(accessor, fields, FieldContentIdentifier.SUB_TOPIC, FieldContentIdentifier.SUB_TOPIC_URL, false);
    }

    private List<LinkableItem> getLinkableItems(final JsonFieldAccessor accessor, final List<JsonField<?>> fields, final FieldContentIdentifier fieldContentIdentifier, final FieldContentIdentifier urlFieldContentIdentifier,
        final boolean required) {
        final Optional<JsonField<?>> optionalField = getFieldForContentIdentifier(fields, fieldContentIdentifier);
        if (!optionalField.isPresent()) {
            if (required) {
                throw new IllegalStateException(String.format("The list provided did not contain the required field: %s", fieldContentIdentifier));
            }
            return Collections.emptyList();
        }
        final Optional<JsonField<?>> optionalUrlField = getFieldForContentIdentifier(fields, urlFieldContentIdentifier);

        // These will always be String fields
        final JsonField<String> valueField = (JsonField<String>) optionalField.get();
        final JsonField<String> urlField = (JsonField<String>) optionalUrlField.orElse(null);

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

    private Optional<JsonField<?>> getFieldForContentIdentifier(final List<JsonField<?>> fields, final FieldContentIdentifier contentIdentifier) {
        return fields
                   .parallelStream()
                   .filter(field -> contentIdentifier.equals(field.getContentIdentifier()))
                   .findFirst();
    }

    private List<LinkableItem> createLinkableItemsFromFields(final JsonFieldAccessor jsonFieldAccessor, final JsonField<String> dataField, final JsonField<String> linkField) {
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
                   .map(value -> new LinkableItem(dataField.getLabel(), value))
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

    private <T> List<JsonField<T>> getTypedFields(final List<JsonField<?>> fields, final TypeRef<?> typeRef) {
        return fields
                   .parallelStream()
                   .filter(field -> field.isOfType(typeRef.getType()))
                   .map(field -> (JsonField<T>) field)
                   .collect(Collectors.toList());
    }

    private <T extends JsonField> Optional<T> getRelatedUrlField(final List<T> categoryFields, final String label) {
        return categoryFields
                   .parallelStream()
                   .filter(field -> field.getLabel().equals(label + JsonField.LABEL_URL_SUFFIX))
                   .findFirst();
    }

    private <T extends JsonField> Optional<T> getFieldByLabel(final List<T> fields, final String label) {
        return fields
                   .parallelStream()
                   .filter(field -> label.equals(field.getLabel()))
                   .findFirst();
    }
}
