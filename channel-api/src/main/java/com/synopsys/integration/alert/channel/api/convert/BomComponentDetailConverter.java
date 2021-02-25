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

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.AbstractBomComponentDetails;

public class BomComponentDetailConverter {
    private final ChannelMessageFormatter formatter;
    private final LinkableItemConverter linkableItemConverter;

    public BomComponentDetailConverter(ChannelMessageFormatter formatter) {
        this.formatter = formatter;
        this.linkableItemConverter = new LinkableItemConverter(formatter);
    }

    public List<String> gatherAbstractBomComponentSectionPieces(AbstractBomComponentDetails bomComponent) {
        List<String> preConcernSectionPieces = new LinkedList<>();

        String componentString = linkableItemConverter.convertToString(bomComponent.getComponent(), true);
        preConcernSectionPieces.add(componentString);
        preConcernSectionPieces.add(formatter.getLineSeparator());

        bomComponent.getComponentVersion()
            .map(componentVersion -> linkableItemConverter.convertToString(componentVersion, true))
            .ifPresent(componentVersionString -> {
                preConcernSectionPieces.add(componentVersionString);
                preConcernSectionPieces.add(formatter.getLineSeparator());
            });

        List<String> componentAttributeStrings = gatherAttributeStrings(bomComponent);
        for (String attributeString : componentAttributeStrings) {
            preConcernSectionPieces.add(String.format("%s-%s%s", formatter.getNonBreakingSpace(), formatter.getNonBreakingSpace(), attributeString));
            preConcernSectionPieces.add(formatter.getLineSeparator());
        }
        return preConcernSectionPieces;
    }

    public List<String> gatherAttributeStrings(AbstractBomComponentDetails bomComponent) {
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

}
