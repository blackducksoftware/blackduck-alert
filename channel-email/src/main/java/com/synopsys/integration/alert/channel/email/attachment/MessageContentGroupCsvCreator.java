/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.attachment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.email.attachment.compatibility.ComponentItem;
import com.synopsys.integration.alert.channel.email.attachment.compatibility.MessageContentGroup;
import com.synopsys.integration.alert.channel.email.attachment.compatibility.ProviderMessageContent;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

@Component
public class MessageContentGroupCsvCreator {
    private static final String UNDEFINED_VALUE = "N/A";
    private static final String MULTI_VALUE_COLUMN_NAME_DELIMITER = " / ";
    private static final String MULTI_VALUE_CELL_DELIMITER = " | ";

    public String createCsvString(MessageContentGroup messageContentGroup) {
        LinkableItem commonProvider = messageContentGroup.getCommonProvider();
        LinkableItem commonProject = messageContentGroup.getCommonTopic();
        List<ProviderMessageContent> contents = messageContentGroup.getSubContent();

        StringBuilder csvBuilder = new StringBuilder();
        List<String> columnNames = createColumnNames(commonProject, contents);
        appendLine(csvBuilder, columnNames);

        List<List<String>> rowValues = createRowValues(commonProvider, commonProject, contents);
        for (List<String> row : rowValues) {
            appendLine(csvBuilder, row);
        }

        return csvBuilder.toString();
    }

    // TODO to change these columns would be a breaking change to our API
    // Provider | Provider Config Name | Topic Name | Sub Topic Name | Component Name | Sub Component Name | Component URL | Operation | Category | Category Name | Category Grouping Attribute Name | Additional Attributes | Item URL
    private List<String> createColumnNames(LinkableItem commonProject, List<ProviderMessageContent> contents) {
        List<String> columnNames = new ArrayList<>();
        columnNames.add("Provider");
        columnNames.add("Provider Config");
        columnNames.add(commonProject.getLabel());

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
            Set<ComponentItem> componentItems = extractComponentItemsOrDefault(message);
            for (ComponentItem componentItem : componentItems) {
                List<String> columnValues = createColumnValues(commonProvider.getLabel(), commonProvider.getValue(), commonTopic.getValue(), subTopicValue, componentItem);
                rows.add(columnValues);
            }
        }
        return rows;
    }

    private Set<ComponentItem> extractComponentItemsOrDefault(ProviderMessageContent message) {
        Set<ComponentItem> componentItems = message.getComponentItems();
        if (!componentItems.isEmpty()) {
            return componentItems;
        } else {
            try {
                ComponentItem naComponentItem = new ComponentItem.Builder()
                    .applyCategory(UNDEFINED_VALUE)
                    .applyOperation(message.getAction().orElse(ItemOperation.INFO))
                    .applyComponentData(UNDEFINED_VALUE, UNDEFINED_VALUE)
                    .applyCategoryItem(UNDEFINED_VALUE, UNDEFINED_VALUE)
                    .build();
                return Set.of(naComponentItem);
            } catch (AlertException e) {
                return Set.of();
            }
        }
    }

    private List<String> createColumnValues(String providerName, String providerValue, String topicValue, String subTopicValue, ComponentItem componentItem) {
        List<String> columnValues = new ArrayList<>();
        columnValues.add(providerName);
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
            .map(LinkableItem::getLabel)
            .collect(Collectors.toSet());
        return createColumnNameString(columnNameCandidates);
    }

    private <T> String createColumnNameString(List<T> objects, Function<T, LinkableItem> linkableItemMapper) {
        Set<String> columnNameCandidates = objects
            .stream()
            .map(linkableItemMapper)
            .map(LinkableItem::getLabel)
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
            .map(item -> String.format("%s: %s", item.getLabel(), item.getValue()))
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
