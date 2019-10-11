/**
 * blackduck-alert
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
package com.synopsys.integration.alert.channel.msteams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.SetMap;
import com.synopsys.integration.alert.common.channel.FreemarkerDataModel;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

public class MsTeamsMessage implements FreemarkerDataModel {
    private Set<LinkableItem> providers = new HashSet<>();
    private List<MsTeamsSection> sections = new ArrayList<>();

    public void addAllContent(MsTeamsMessage other) {
        providers.addAll(other.providers);
        sections.addAll(other.sections);
    }

    public void addContent(ProviderMessageContent providerMessageContent) {
        providers.add(providerMessageContent.getProvider());

        MsTeamsSection msTeamsSection = new MsTeamsSection();
        msTeamsSection.setProvider(providerMessageContent.getProvider().getValue());
        msTeamsSection.setTopic(providerMessageContent.getTopic().getValue());
        providerMessageContent.getSubTopic()
            .map(LinkableItem::getValue)
            .ifPresent(msTeamsSection::setSubTopic);
        sections.add(msTeamsSection);

        SetMap<String, ComponentItem> groupedComponentItems = providerMessageContent.groupRelatedComponentItems();
        List<MsTeamsComponent> components = new ArrayList<>();
        for (Set<ComponentItem> componentItems : groupedComponentItems.values()) {
            SetMap<String, ComponentItem> groupingToItems = createGrouping(componentItems);
            for (Set<ComponentItem> groupedItems : groupingToItems.values()) {
                Optional<ComponentItem> optionalArbitraryItem = groupedItems
                                                                    .stream()
                                                                    .findAny();
                if (optionalArbitraryItem.isPresent()) {
                    ComponentItem arbitraryItem = optionalArbitraryItem.get();

                    MsTeamsComponent msTeamsComponent = new MsTeamsComponent();
                    msTeamsComponent.setCategory(arbitraryItem.getCategory());
                    msTeamsComponent.setOperation(arbitraryItem.getOperation().toString());

                    StringBuilder componentTextBuilder = new StringBuilder(arbitraryItem.getComponent().getValue());
                    arbitraryItem.getSubComponent().map(item -> "/" + item.getValue()).ifPresent(componentTextBuilder::append);
                    msTeamsComponent.setText(componentTextBuilder.toString());
                    msTeamsComponent.setCategoryItemText(createCategoryItemsString(arbitraryItem, groupedItems));

                    String allAttributeDetails = createDetails(arbitraryItem.getComponentAttributes());
                    msTeamsComponent.setAllAttributeDetails(allAttributeDetails);

                    components.add(msTeamsComponent);
                }
            }
        }
        msTeamsSection.setComponents(components);
    }

    private SetMap<String, ComponentItem> createGrouping(Set<ComponentItem> componentItems) {
        SetMap<String, ComponentItem> groupingToItems = SetMap.createLinked();
        for (ComponentItem item : componentItems) {
            String key = item.getCategoryGroupingAttribute()
                             .map(LinkableItem::getValue)
                             .orElse("DEFAULT_GROUPING_STRING");
            groupingToItems.add(key, item);
        }
        return groupingToItems;
    }

    private String createCategoryItemsString(ComponentItem arbitraryItem, Collection<ComponentItem> groupedItems) {
        StringBuilder categoryItemTextBuilder = new StringBuilder();
        arbitraryItem.getCategoryGroupingAttribute()
            .map(this::createLinkableItemString)
            .map(grouping -> grouping + ", ")
            .ifPresent(categoryItemTextBuilder::append);
        String categoryItemsString = groupedItems
                                         .stream()
                                         .map(ComponentItem::getCategoryItem)
                                         .map(this::createLinkableItemString)
                                         .collect(Collectors.joining(", "));
        categoryItemTextBuilder.append(categoryItemsString);
        return categoryItemTextBuilder.toString();
    }

    private String createDetails(Set<LinkableItem> componentAttributes) {
        return componentAttributes
                   .stream()
                   .map(this::createLinkableItemString)
                   .collect(Collectors.joining(", "));
    }

    public int getProviderCount() {
        return providers.size();
    }

    public List<MsTeamsSection> getSections() {
        return sections;
    }

    private String createLinkableItemString(LinkableItem item) {
        String valueString = item.getValue();
        if (item.getUrl().isPresent()) {
            valueString = String.format("[%s](%s)", valueString, item.getUrl().get());
        }
        return String.format("%s: %s", item.getName(), valueString);
    }

}
