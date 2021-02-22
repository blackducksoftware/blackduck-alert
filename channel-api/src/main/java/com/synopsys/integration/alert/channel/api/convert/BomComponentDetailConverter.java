/*
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
package com.synopsys.integration.alert.channel.api.convert;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;

public class BomComponentDetailConverter {
    private final ChannelMessageFormatter formatter;
    private final LinkableItemConverter linkableItemConverter;

    public BomComponentDetailConverter(ChannelMessageFormatter formatter) {
        this.formatter = formatter;
        this.linkableItemConverter = new LinkableItemConverter(formatter);
    }

    public List<String> gatherBomComponentPieces(BomComponentDetails bomComponent) {
        List<String> bomComponentSectionPieces = new LinkedList<>();

        String componentString = linkableItemConverter.convertToString(bomComponent.getComponent(), true);
        bomComponentSectionPieces.add(componentString);
        bomComponentSectionPieces.add(formatter.getLineSeparator());

        bomComponent.getComponentVersion()
            .map(componentVersion -> linkableItemConverter.convertToString(componentVersion, true))
            .ifPresent(componentVersionString -> {
                bomComponentSectionPieces.add(componentVersionString);
                bomComponentSectionPieces.add(formatter.getLineSeparator());
            });

        List<String> componentAttributeStrings = gatherAttributeStrings(bomComponent);
        for (String attributeString : componentAttributeStrings) {
            bomComponentSectionPieces.add(String.format("%s-%s%s", formatter.getNonBreakingSpace(), formatter.getNonBreakingSpace(), attributeString));
            bomComponentSectionPieces.add(formatter.getLineSeparator());
        }

        List<String> componentConcernSectionPieces = createComponentConcernSectionPieces(bomComponent);
        bomComponentSectionPieces.addAll(componentConcernSectionPieces);

        return bomComponentSectionPieces;
    }

    public List<String> createComponentConcernSectionPieces(BomComponentDetails bomComponent) {
        List<String> componentConcernSectionPieces = new LinkedList<>();

        String nonBreakingSpace = formatter.getNonBreakingSpace();
        String indent = nonBreakingSpace + nonBreakingSpace;
        String doubleIndent = indent + indent;

        Set<ComponentConcern> sortedConcerns = new TreeSet<>(bomComponent.getComponentConcerns());
        ComponentConcernType currentType = null;
        ItemOperation currentOperation = null;
        ComponentConcernSeverity currentSeverity = null;
        for (ComponentConcern componentConcern : sortedConcerns) {
            if (!componentConcern.getType().equals(currentType)) {
                currentType = componentConcern.getType();
                currentOperation = null;

                componentConcernSectionPieces.add(formatter.getLineSeparator());
                componentConcernSectionPieces.add(formatter.encode(currentType.name()));
            }

            if (!componentConcern.getOperation().equals(currentOperation)) {
                currentOperation = componentConcern.getOperation();
                currentSeverity = null;

                componentConcernSectionPieces.add(formatter.getLineSeparator());
                componentConcernSectionPieces.add(indent);
                componentConcernSectionPieces.add(formatter.encode(currentOperation.name()));
            }

            if (!componentConcern.getSeverity().equals(currentSeverity)) {
                currentSeverity = componentConcern.getSeverity();

                componentConcernSectionPieces.add(formatter.getLineSeparator());
                componentConcernSectionPieces.add(doubleIndent);
                componentConcernSectionPieces.add(formatter.encode(currentSeverity.name()));
                componentConcernSectionPieces.add(formatter.getLineSeparator());
            }

            String concernString = createComponentConcernString(componentConcern);
            componentConcernSectionPieces.add(concernString);
        }
        return componentConcernSectionPieces;
    }

    public List<String> gatherAttributeStrings(BomComponentDetails bomComponent) {
        return gatherAttributeStrings(bomComponent.getLicense(), bomComponent.getUsage(), bomComponent.getAdditionalAttributes());
    }

    private List<String> gatherAttributeStrings(LinkableItem licenseItem, String usageText, List<LinkableItem> additionalAttributes) {
        List<String> componentAttributeStrings = new ArrayList<>(additionalAttributes.size() + 2);

        String licenseString = linkableItemConverter.convertToString(licenseItem, false);
        componentAttributeStrings.add(licenseString);

        LinkableItem usageItem = new LinkableItem("Usage", usageText);
        String usageString = linkableItemConverter.convertToString(usageItem, false);
        componentAttributeStrings.add(usageString);

        additionalAttributes
            .stream()
            .map(attr -> linkableItemConverter.convertToString(attr, false))
            .forEach(componentAttributeStrings::add);
        return componentAttributeStrings;
    }

    private String createComponentConcernString(ComponentConcern componentConcern) {
        String encodedName = formatter.encode(componentConcern.getName());
        Optional<String> concernUrl = componentConcern.getUrl();
        if (concernUrl.isPresent()) {
            String encodedUrl = formatter.encode(concernUrl.get());
            return String.format("[%s]", formatter.createLink(encodedName, encodedUrl));
        } else {
            return String.format("%s-%s%s%s", formatter.getNonBreakingSpace(), formatter.getNonBreakingSpace(), encodedName, formatter.getLineSeparator());
        }
    }

}
