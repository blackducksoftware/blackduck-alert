/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.channel.issuetracker.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.message.ChannelMessageParser;
import com.synopsys.integration.alert.common.channel.message.MessageSplitter;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public abstract class IssueTrackerMessageParser extends ChannelMessageParser {
    private final int titleSizeLimit;
    private final int messageSizeLimit;

    // TODO: What if description length differs from comment length? Should description length be accepted here?
    protected IssueTrackerMessageParser(int titleSizeLimit, int messageSizeLimit) {
        this.titleSizeLimit = titleSizeLimit;
        this.messageSizeLimit = messageSizeLimit;
    }

    public final IssueContentModel createIssueContentModel(
        String providerName,
        IssueOperation issueOperation,
        LinkableItem topic,
        @Nullable LinkableItem subTopic,
        Set<ComponentItem> componentItems,
        ComponentItem arbitraryItem
    ) {
        String title = createTitle(providerName, topic, subTopic, arbitraryItem);

        StringBuilder description = new StringBuilder();
        description.append(emphasize("Provider: " + providerName));
        description.append(getLineSeparator());
        description.append(createLinkableItemString(topic, true));
        description.append(getLineSeparator());
        if (null != subTopic) {
            description.append(createLinkableItemString(subTopic, true));
            description.append(getLineSeparator());
        }

        LinkedList<String> descriptionComments = new LinkedList<>();
        String finalDescription = createDescriptionOrAddToAdditionalComments(description.toString(), componentItems, descriptionComments);
        List<String> operationComments = createOperationComment(providerName, topic.getLabel(), issueOperation, componentItems);
        return IssueContentModel.of(title, finalDescription, descriptionComments, operationComments);
    }

    private List<String> createOperationComment(String provider, String category, IssueOperation operation, Set<ComponentItem> componentItems) {
        List<String> categoryItemMessagePieces = createCategoryMessagePieces(componentItems);
        List<String> attributesPieces = createComponentAttributeMessagePieces(componentItems);
        Collection<String> text = new ArrayList<>();
        String description = String.format("The %s operation was performed for this %s in %s.", operation.name(), category, provider);
        text.add(description);
        if (!attributesPieces.isEmpty()) {
            text.add(getLineSeparator());
            text.add("----------");
            text.add(getLineSeparator());
            String categoryItemString = String.join("", categoryItemMessagePieces);
            text.add(categoryItemString);
            text.add(getLineSeparator());
            text.add("----------");
            text.add(getLineSeparator());
            String attributesString = String.join("", attributesPieces);
            text.add(attributesString);
        }
        MessageSplitter splitter = new MessageSplitter(messageSizeLimit, getLineSeparator());
        return splitter.splitMessages(text, true);
    }

    private String createTitle(String provider, LinkableItem topic, @Nullable LinkableItem subTopic, @Nullable ComponentItem arbitraryItem) {
        StringBuilder title = new StringBuilder();
        title.append("Alert - Provider: ");
        title.append(provider);
        title.append(createTitlePartStringPrefixedWithComma(topic));

        if (null != subTopic) {
            title.append(createTitlePartStringPrefixedWithComma(subTopic));
        }

        if (null != arbitraryItem) {
            title.append(createTitlePartStringPrefixedWithComma(arbitraryItem.getComponent()));
            arbitraryItem
                .getComponentVersion()
                .ifPresent(linkableItem -> title.append(createTitlePartStringPrefixedWithComma(linkableItem)));

            if (arbitraryItem.collapseOnCategory()) {
                title.append(", ");
                title.append(arbitraryItem.getCategory());
            } else {
                title.append(createTitlePartStringPrefixedWithComma(arbitraryItem.getCategoryItem()));
            }
        }

        return StringUtils.abbreviate(title.toString(), titleSizeLimit);
    }

    private String createDescriptionOrAddToAdditionalComments(String initialDescription, Set<ComponentItem> componentItems, Collection<String> additionalComments) {
        StringBuilder description = new StringBuilder();

        List<String> tempAdditionalComments = new LinkedList<>();
        splitAndAppendMessages(initialDescription, tempAdditionalComments);
        description.append(tempAdditionalComments.remove(0));
        boolean descriptionFull = CollectionUtils.isNotEmpty(tempAdditionalComments);
        int currentLength = description.length();

        List<String> componentItemMessagePieces = createComponentAndCategoryMessagePieces(componentItems);
        for (String descriptionItem : componentItemMessagePieces) {
            int itemLength = descriptionItem.length();
            if (descriptionFull || itemLength + currentLength >= messageSizeLimit) {
                descriptionFull = true;
                splitAndAppendMessages(descriptionItem, tempAdditionalComments);
            } else {
                description.append(descriptionItem);
                currentLength = description.length();
            }
        }

        additionalComments.addAll(tempAdditionalComments);

        return description.toString();
    }

    private void splitAndAppendMessages(String message, List<String> messages) {
        MessageSplitter splitter = new MessageSplitter(messageSizeLimit, getLineSeparator());
        List<String> splitMessages = splitter.splitMessages(List.of(message), true);
        messages.addAll(splitMessages);
    }

    private String createTitlePartStringPrefixedWithComma(LinkableItem linkableItem) {
        return String.format(", %s: %s", linkableItem.getLabel(), linkableItem.getValue());
    }

}
