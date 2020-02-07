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
package com.synopsys.integration.alert.common.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.TypeRef;
import com.synopsys.integration.alert.common.enumeration.FieldContentIdentifier;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonFieldAccessor;

public abstract class MessageContentCollector {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final JsonExtractor jsonExtractor;
    private final Collection<ProviderContentType> contentTypes;
    private final Set<String> supportedNotificationTypes;
    private final Map<String, ProviderMessageContent.Builder> messageBuilderMap;

    public MessageContentCollector(final JsonExtractor jsonExtractor, final Collection<ProviderContentType> contentTypes) {
        this.jsonExtractor = jsonExtractor;
        this.contentTypes = contentTypes;
        this.supportedNotificationTypes = contentTypes.stream().map(ProviderContentType::getNotificationType).collect(Collectors.toSet());
        this.messageBuilderMap = new HashMap<>();
    }

    public Set<String> getSupportedNotificationTypes() {
        return supportedNotificationTypes;
    }

    public void insert(final AlertNotificationWrapper notification) {
        try {
            final List<JsonField<?>> notificationFields = getFieldsForNotificationType(notification.getNotificationType());
            final JsonFieldAccessor jsonFieldAccessor = createJsonAccessor(notificationFields, notification.getContent());
            final List<ProviderMessageContent.Builder> providerContents = getMessageBuildersOrCreateIfTheyDoNotExist(jsonFieldAccessor, notificationFields, notification.getProviderCreationTime());

            for (final ProviderMessageContent.Builder builder : providerContents) {
                final Collection<ComponentItem> componentItems = getComponentItems(jsonFieldAccessor, notificationFields, notification);
                builder.applyAllComponentItems(componentItems);
                String builderKey = builder.getCurrentContentKey().getValue();
                if (!messageBuilderMap.containsKey(builderKey)) {
                    messageBuilderMap.put(builderKey, builder);
                }
            }
        } catch (final IllegalArgumentException ex) {
            final String message = String.format("Error inserting notification into collector: %s", notification);
            logger.error(message, ex);
        }
    }

    public List<ProviderMessageContent> getCollectedContent() {
        return messageBuilderMap.values().stream()
                   .map(this::buildMessageContent)
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .collect(Collectors.toList());
    }

    private Optional<ProviderMessageContent> buildMessageContent(ProviderMessageContent.Builder builder) {
        try {
            return Optional.of(builder.build());
        } catch (AlertException ex) {
            logger.error("Error building message content continuing on", ex);
            return Optional.empty();
        }
    }

    protected abstract Collection<ComponentItem> getComponentItems(JsonFieldAccessor jsonFieldAccessor, List<JsonField<?>> notificationFields, AlertNotificationWrapper notificationContent);

    protected final List<JsonField<String>> getStringFields(final List<JsonField<?>> fields) {
        return getTypedFields(fields, new TypeRef<String>() {});
    }

    protected final List<JsonField<Long>> getLongFields(final List<JsonField<?>> fields) {
        return getTypedFields(fields, new TypeRef<Long>() {});
    }

    protected final List<JsonField<Integer>> getIntegerFields(final List<JsonField<?>> fields) {
        return getTypedFields(fields, new TypeRef<Integer>() {});
    }

    protected final <T> List<JsonField<T>> getFieldsOfType(final List<JsonField<?>> fields, final TypeRef<?> typeRef) {
        return getTypedFields(fields, typeRef);
    }

    protected final List<LinkableItem> getLinkableItemsByLabel(final JsonFieldAccessor accessor, final List<JsonField<String>> fields, final String label) {
        final Optional<JsonField<String>> foundField = getFieldByLabel(fields, label);
        if (foundField.isPresent()) {
            final JsonField<String> valueField = foundField.get();
            final Optional<JsonField<String>> foundUrlField = getRelatedUrlField(fields, label);
            return createLinkableItemsFromFields(accessor, valueField, foundUrlField.orElse(null));
        }
        return List.of();
    }

    protected final List<LinkableItem> getItemsByLabel(final JsonFieldAccessor accessor, final List<JsonField<String>> fields, final String label) {
        final Optional<JsonField<String>> foundField = getFieldByLabel(fields, label);
        if (foundField.isPresent()) {
            final JsonField<String> valueField = foundField.get();
            return createLinkableItemsFromFields(accessor, valueField, null);
        }
        return Collections.emptyList();
    }

    protected final <T> List<T> getFieldValueObjectsByLabel(final JsonFieldAccessor accessor, final List<JsonField<T>> fields, final String label) {
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

    protected abstract LinkableItem getProviderItem();

    protected List<LinkableItem> getTopicItems(final JsonFieldAccessor accessor, final List<JsonField<?>> fields) {
        return getLinkableItems(accessor, fields, FieldContentIdentifier.TOPIC, FieldContentIdentifier.TOPIC_URL, true);
    }

    protected List<LinkableItem> getSubTopicItems(final JsonFieldAccessor accessor, final List<JsonField<?>> fields) {
        return getLinkableItems(accessor, fields, FieldContentIdentifier.SUB_TOPIC, FieldContentIdentifier.SUB_TOPIC_URL, false);
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

    private List<ProviderMessageContent.Builder> getMessageBuildersOrCreateIfTheyDoNotExist(JsonFieldAccessor accessor, List<JsonField<?>> notificationFields, Date providerCreationTime) {
        final List<ProviderMessageContent.Builder> buildersForNotifications = new ArrayList<>();

        final List<LinkableItem> topicItems = getTopicItems(accessor, notificationFields);
        if (topicItems.isEmpty()) {
            return List.of();
        }
        final List<LinkableItem> subTopicItems = getSubTopicItems(accessor, notificationFields);
        // For the number of topics assume there is an equal number of sub topics and the order is the same. This seems fragile at the moment.
        final int count = topicItems.size();
        for (int index = 0; index < count; index++) {
            final LinkableItem topicItem = topicItems.get(index);

            LinkableItem subTopic = null;
            if (!subTopicItems.isEmpty()) {
                subTopic = subTopicItems.get(index);
            }

            final LinkableItem providerItem = getProviderItem();
            final ProviderMessageContent.Builder foundContent = findContentBuilder(providerItem.getValue(), topicItem, subTopic);

            if (null != foundContent) {
                foundContent.applyEarliestProviderCreationTime(providerCreationTime);
                buildersForNotifications.add(foundContent);
            } else {
                ProviderMessageContent.Builder builder = new ProviderMessageContent.Builder();
                builder
                    .applyProvider(providerItem.getValue(), providerItem.getUrl().orElse(null))
                    .applyProviderCreationTime(providerCreationTime)
                    .applyTopic(topicItem.getName(), topicItem.getValue(), topicItem.getUrl().orElse(null));
                if (null != subTopic) {
                    builder.applySubTopic(subTopic.getName(), subTopic.getValue(), subTopic.getUrl().orElse(null));
                }
                buildersForNotifications.add(builder);
            }
        }
        return buildersForNotifications;
    }

    private List<LinkableItem> getLinkableItems(final JsonFieldAccessor accessor, final List<JsonField<?>> fields, final FieldContentIdentifier fieldContentIdentifier, final FieldContentIdentifier urlFieldContentIdentifier,
        final boolean required) {
        final Optional<JsonField<?>> optionalField = getFieldForContentIdentifier(fields, fieldContentIdentifier);
        if (!optionalField.isPresent()) {
            if (required) {
                throw new IllegalStateException(String.format("The list provided did not contain the required field: %s", fieldContentIdentifier));
            }
            return List.of();
        }
        final Optional<JsonField<?>> optionalUrlField = getFieldForContentIdentifier(fields, urlFieldContentIdentifier);

        // These will always be String fields
        final JsonField<String> valueField = (JsonField<String>) optionalField.get();
        final JsonField<String> urlField = (JsonField<String>) optionalUrlField.orElse(null);

        return createLinkableItemsFromFields(accessor, valueField, urlField);
    }

    private ProviderMessageContent.Builder findContentBuilder(String providerName, LinkableItem topicItem, final LinkableItem subTopicItem) {
        String subTopicName = null;
        String subTopicValue = null;
        if (null != subTopicItem) {
            subTopicName = subTopicItem.getName();
            subTopicValue = subTopicItem.getValue();
        }

        String key = ContentKey.of(providerName, topicItem.getName(), topicItem.getValue(), subTopicName, subTopicValue).getValue();
        return messageBuilderMap.get(key);
    }

    protected final Optional<JsonField<?>> getFieldForContentIdentifier(final List<JsonField<?>> fields, final FieldContentIdentifier contentIdentifier) {
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
