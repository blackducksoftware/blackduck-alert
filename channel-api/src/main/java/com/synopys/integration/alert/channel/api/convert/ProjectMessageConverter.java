/**
 * channel-api
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
package com.synopys.integration.alert.channel.api.convert;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.synopsys.integration.alert.common.channel.message.ChunkedStringBuilder;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;
import com.synopsys.integration.alert.processor.api.extract.model.project.MessageReason;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public class ProjectMessageConverter extends ProviderMessageConverter<ProjectMessage> {
    private final ChannelMessageFormatter messageFormatter;

    public ProjectMessageConverter(ChannelMessageFormatter messageFormatter) {
        super(messageFormatter);
        this.messageFormatter = messageFormatter;
    }

    @Override
    public List<String> convertToFormattedMessageChunks(ProjectMessage projectMessage) {
        ChunkedStringBuilder chunkedStringBuilder = new ChunkedStringBuilder(messageFormatter.getMaxMessageLength());

        String projectString = createLinkableItemString(projectMessage.getProject(), true);
        chunkedStringBuilder.append(projectString);
        chunkedStringBuilder.append(messageFormatter.getLineSeparator());

        projectMessage.getProjectVersion()
            .map(projectVersion -> createLinkableItemString(projectVersion, true))
            .ifPresent(projectVersionString -> {
                chunkedStringBuilder.append(projectVersionString);
                chunkedStringBuilder.append(messageFormatter.getLineSeparator());
            });

        String nonBreakingSpace = messageFormatter.getNonBreakingSpace();

        MessageReason messageReason = projectMessage.getMessageReason();
        if (MessageReason.PROJECT_STATUS.equals(messageReason) || MessageReason.PROJECT_VERSION_STATUS.equals(messageReason)) {
            projectMessage.getOperation()
                .map(operation -> String.format("Project%sAction:%s%s", nonBreakingSpace, nonBreakingSpace, operation.name()))
                .map(messageFormatter::encode)
                .ifPresent(chunkedStringBuilder::append);
            return chunkedStringBuilder.collectCurrentChunks();
        }

        List<BomComponentDetails> bomComponents = projectMessage.getBomComponents();
        if (!bomComponents.isEmpty()) {
            chunkedStringBuilder.append(messageFormatter.getSectionSeparator());
            chunkedStringBuilder.append(messageFormatter.getLineSeparator());
        }

        for (BomComponentDetails bomComponentDetails : bomComponents) {
            List<String> bomComponentMessagePieces = gatherBomComponentPieces(bomComponentDetails);
            bomComponentMessagePieces.forEach(chunkedStringBuilder::append);
            chunkedStringBuilder.append(messageFormatter.getSectionSeparator());
            chunkedStringBuilder.append(messageFormatter.getLineSeparator());
        }

        return chunkedStringBuilder.collectCurrentChunks();
    }

    private List<String> gatherBomComponentPieces(BomComponentDetails bomComponent) {
        List<String> bomComponentSectionPieces = new LinkedList<>();

        String componentString = createLinkableItemString(bomComponent.getComponent(), true);
        bomComponentSectionPieces.add(componentString);
        bomComponentSectionPieces.add(messageFormatter.getLineSeparator());

        bomComponent.getComponentVersion()
            .map(componentVersion -> createLinkableItemString(componentVersion, true))
            .ifPresent(componentVersionString -> {
                bomComponentSectionPieces.add(componentVersionString);
                bomComponentSectionPieces.add(messageFormatter.getLineSeparator());
            });

        String nonBreakingSpace = messageFormatter.getNonBreakingSpace();

        List<String> componentAttributeStrings = gatherAttributeStrings(bomComponent);
        for (String attributeString : componentAttributeStrings) {
            bomComponentSectionPieces.add(String.format("%s-%s%s", nonBreakingSpace, nonBreakingSpace, attributeString));
            bomComponentSectionPieces.add(messageFormatter.getLineSeparator());
        }

        Set<ComponentConcern> sortedConcerns = new TreeSet<>(bomComponent.getComponentConcerns());
        ComponentConcernType currentType = null;
        ItemOperation currentOperation = null;
        ComponentConcernSeverity currentSeverity = null;
        for (ComponentConcern componentConcern : sortedConcerns) {
            if (!componentConcern.getType().equals(currentType)) {
                currentType = componentConcern.getType();
                currentOperation = null;

                bomComponentSectionPieces.add(messageFormatter.getLineSeparator());
                bomComponentSectionPieces.add(messageFormatter.encode(currentType.name()));
            }

            if (!componentConcern.getOperation().equals(currentOperation)) {
                currentOperation = componentConcern.getOperation();
                currentSeverity = null;

                bomComponentSectionPieces.add(messageFormatter.getLineSeparator());
                bomComponentSectionPieces.add(nonBreakingSpace + nonBreakingSpace);
                bomComponentSectionPieces.add(messageFormatter.encode(currentOperation.name()));
            }

            if (!componentConcern.getSeverity().equals(currentSeverity)) {
                currentSeverity = componentConcern.getSeverity();

                bomComponentSectionPieces.add(messageFormatter.getLineSeparator());
                bomComponentSectionPieces.add(nonBreakingSpace + nonBreakingSpace + nonBreakingSpace + nonBreakingSpace);
                bomComponentSectionPieces.add(messageFormatter.encode(currentSeverity.name()));
                bomComponentSectionPieces.add(messageFormatter.getLineSeparator());
            }

            String concernString;
            String encodedName = messageFormatter.encode(componentConcern.getName());
            Optional<String> concernUrl = componentConcern.getUrl();
            if (concernUrl.isPresent()) {
                String encodedUrl = messageFormatter.encode(concernUrl.get());
                concernString = String.format("[%s]", messageFormatter.createLink(encodedName, encodedUrl));
            } else {
                concernString = String.format("%s-%s%s%s", nonBreakingSpace, nonBreakingSpace, encodedName, messageFormatter.getLineSeparator());
            }
            bomComponentSectionPieces.add(concernString);
        }

        return bomComponentSectionPieces;
    }

    private List<String> gatherAttributeStrings(BomComponentDetails bomComponent) {
        List<LinkableItem> additionalAttributes = bomComponent.getAdditionalAttributes();
        List<String> componentAttributeStrings = new ArrayList<>(additionalAttributes.size() + 2);

        String licenseString = createLinkableItemString(bomComponent.getLicense(), false);
        componentAttributeStrings.add(licenseString);

        String usageString = createLinkableItemString(bomComponent.getLicense(), false);
        componentAttributeStrings.add(usageString);

        additionalAttributes
            .stream()
            .map(attr -> createLinkableItemString(attr, false))
            .forEach(componentAttributeStrings::add);
        return componentAttributeStrings;
    }

}
