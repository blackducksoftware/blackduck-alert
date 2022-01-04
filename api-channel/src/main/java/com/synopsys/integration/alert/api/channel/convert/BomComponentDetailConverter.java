/*
 * api-channel
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.convert;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.AbstractBomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;

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
        return gatherAttributeStrings(bomComponent.getLicense(), bomComponent.getUsage(), bomComponent.getComponentUpgradeGuidance(), bomComponent.getAdditionalAttributes());
    }

    private List<String> gatherAttributeStrings(LinkableItem licenseItem, String usageText, ComponentUpgradeGuidance componentUpgradeGuidance, List<LinkableItem> additionalAttributes) {
        List<String> componentAttributeStrings = new ArrayList<>(additionalAttributes.size() + 2);

        String licenseString = linkableItemConverter.convertToString(licenseItem, false);
        componentAttributeStrings.add(licenseString);

        LinkableItem usageItem = new LinkableItem("Usage", usageText);
        String usageString = linkableItemConverter.convertToString(usageItem, false);
        componentAttributeStrings.add(usageString);

        componentUpgradeGuidance.getShortTermUpgradeGuidance()
            .stream()
            .map(attr -> linkableItemConverter.convertToString(attr, false))
            .forEach(componentAttributeStrings::add);
        componentUpgradeGuidance.getLongTermUpgradeGuidance()
            .stream()
            .map(attr -> linkableItemConverter.convertToString(attr, false))
            .forEach(componentAttributeStrings::add);

        additionalAttributes
            .stream()
            .map(attr -> linkableItemConverter.convertToString(attr, false))
            .forEach(componentAttributeStrings::add);
        return componentAttributeStrings;
    }
}
