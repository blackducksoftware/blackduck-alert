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
package com.synopsys.integration.alert.common.email;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

@Component
public class MessageContentGroupCsvCreator {
    private static final String UNDEFINED_VALUE = "N/A";
    private static final String MULTI_VALUE_COLUMN_NAME_DELIMITER = " / ";
    private static final String MULTI_VALUE_CELL_DELIMITER = " | ";

    public String createCsvString(MessageContentGroup messageContentGroup) {
        LinkableItem commonProvider = messageContentGroup.getCommonProvider();
        LinkableItem commonTopic = messageContentGroup.getCommonTopic();
        List<ProviderMessageContent> contents = messageContentGroup.getSubContent();

        StringBuilder csvBuilder = new StringBuilder();
        List<String> columnNames = createColumnNames(commonProvider, commonTopic, contents);
        appendLine(csvBuilder, columnNames);

        List<List<String>> rowValues = createRowValues(commonProvider, commonTopic, contents);
        for (List<String> row : rowValues) {
            appendLine(csvBuilder, row);
        }

        return csvBuilder.toString();
    }

    // Provider | Topic Name | Sub Topic Name | Component Name | Sub Component Name | Component URL | Operation | Category | Category Name | Category Grouping Attribute Name | Additional Attributes | Item URL
    private List<String> createColumnNames(LinkableItem commonProvider, LinkableItem commonTopic, List<ProviderMessageContent> contents) {
        List<String> columnNames = new ArrayList<>();
        columnNames.add(commonProvider.getName());
        columnNames.add(commonTopic.getName());

        String subTopicNamesCombined = createOptionalColumnNameString(contents, ProviderMessageContent::getSubTopic);
        columnNames.add(subTopicNamesCombined);

        List<ComponentItem> allComponentItems = contents
                                                    .stream()
                                                    .map(ProviderMessageContent::getComponentItems)
                                                    .flatMap(Set::stream)
                                                    .collect(Collectors.toList());

        String componentNamesCombined = createColumnNameString(allComponentItems, ComponentItem::getComponent);
        columnNames.add(componentNamesCombined);

        String subComponentNamesCombined = createOptionalColumnNameString(allComponentItems, ComponentItem::getSubComponent);
        columnNames.add(subComponentNamesCombined);

        columnNames.add("Component URL");
        columnNames.add("Operation");
        columnNames.add("Category");

        Set<String> categoryNames = allComponentItems
                                        .stream()
                                        .map(ComponentItem::getCategory)
                                        .collect(Collectors.toSet());
        String categoryNamesCombined = createColumnNameString(categoryNames);
        columnNames.add(categoryNamesCombined);

        String categoryGroupingAttributeNamesCombined = createOptionalColumnNameString(allComponentItems, ComponentItem::getCategoryGroupingAttribute);
        columnNames.add(categoryGroupingAttributeNamesCombined);

        columnNames.add("Additional Attributes");
        columnNames.add("Item URL");

        return columnNames;
    }

    private List<List<String>> createRowValues(LinkableItem commonProvider, LinkableItem commonTopic, List<ProviderMessageContent> contents) {
        List<List<String>> rows = new ArrayList<>();
        for (ProviderMessageContent message : contents) {
            String subTopicValue = createOptionalValueString(message.getSubTopic(), LinkableItem::getValue);
            for (ComponentItem componentItem : message.getComponentItems()) {
                List<String> columnValues = createColumnValues(commonProvider.getValue(), commonTopic.getValue(), subTopicValue, componentItem);
                rows.add(columnValues);
            }
        }
        return rows;
    }

    private List<String> createColumnValues(String providerValue, String topicValue, String subTopicValue, ComponentItem componentItem) {
        List<String> columnValues = new ArrayList<>();
        columnValues.add(providerValue);
        columnValues.add(topicValue);
        columnValues.add(subTopicValue);

        columnValues.add(componentItem.getComponent().getValue());
        String subComponentString = createOptionalValueString(componentItem.getSubComponent(), LinkableItem::getValue);
        columnValues.add(subComponentString);
        String componentUrl;
        if (UNDEFINED_VALUE.equals(subComponentString)) {
            componentUrl = componentItem.getComponent().getUrl().orElse(null);
        } else {
            componentUrl = createOptionalValueString(componentItem.getSubComponent(), item -> item.getUrl().orElse(null));
        }
        columnValues.add(componentUrl);
        columnValues.add(componentItem.getOperation().name());
        columnValues.add(componentItem.getCategory());

        columnValues.add(componentItem.getCategoryItem().getValue());
        String categoryGroupingAttribute = createOptionalValueString(componentItem.getCategoryGroupingAttribute(), LinkableItem::getValue);
        columnValues.add(categoryGroupingAttribute);

        String additionalAttributes = createFlattenedItemsString(componentItem.getComponentAttributes());
        columnValues.add(additionalAttributes);

        String categoryItemUrl = componentItem.getCategoryItem().getUrl().orElse(UNDEFINED_VALUE);
        columnValues.add(categoryItemUrl);
        return columnValues;
    }

    private <T> String createOptionalColumnNameString(List<T> objects, Function<T, Optional<LinkableItem>> linkableItemMapper) {
        Set<String> columnNameCandidates = objects
                                               .stream()
                                               .map(linkableItemMapper)
                                               .flatMap(Optional::stream)
                                               .map(LinkableItem::getName)
                                               .collect(Collectors.toSet());
        return createColumnNameString(columnNameCandidates);
    }

    private <T> String createColumnNameString(List<T> objects, Function<T, LinkableItem> linkableItemMapper) {
        Set<String> columnNameCandidates = objects
                                               .stream()
                                               .map(linkableItemMapper)
                                               .map(LinkableItem::getName)
                                               .collect(Collectors.toSet());
        return createColumnNameString(columnNameCandidates);
    }

    private String createColumnNameString(Set<String> columnNameCandidates) {
        if (columnNameCandidates.isEmpty()) {
            return UNDEFINED_VALUE;
        }
        return String.join(MULTI_VALUE_COLUMN_NAME_DELIMITER, columnNameCandidates);
    }

    private String createOptionalValueString(Optional<LinkableItem> item, Function<LinkableItem, String> attributeMapper) {
        return item
                   .map(attributeMapper)
                   .filter(StringUtils::isNotBlank)
                   .orElse(UNDEFINED_VALUE);
    }

    private String createFlattenedItemsString(Collection<LinkableItem> items) {
        String flattenedString = items
                                     .stream()
                                     .map(item -> String.format("%s: %s", item.getName(), item.getValue()))
                                     .collect(Collectors.joining(MULTI_VALUE_CELL_DELIMITER));
        if (StringUtils.isNotBlank(flattenedString)) {
            return flattenedString;
        }
        return UNDEFINED_VALUE;
    }

    private void appendLine(StringBuilder csvBuilder, List<String> values) {
        String csvString = String.join(",", values);
        csvBuilder.append(csvString);
        csvBuilder.append("\r\n");
    }

}
