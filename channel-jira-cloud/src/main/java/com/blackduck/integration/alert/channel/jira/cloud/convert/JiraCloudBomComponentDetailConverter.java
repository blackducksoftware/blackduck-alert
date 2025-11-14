package com.blackduck.integration.alert.channel.jira.cloud.convert;

import java.util.List;

import com.blackduck.integration.alert.api.channel.convert.ChannelMessageFormatter;
import com.blackduck.integration.alert.api.processor.extract.model.project.AbstractBomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentUpgradeGuidance;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

public class JiraCloudBomComponentDetailConverter {
    private final ChannelMessageFormatter formatter;

    public JiraCloudBomComponentDetailConverter(ChannelMessageFormatter formatter) {
        this.formatter = formatter;
    }

    public void gatherAbstractBomComponentSectionPieces(AbstractBomComponentDetails bomComponent, AtlassianDocumentBuilder documentBuilder) {
        documentBuilder.addTextNode(bomComponent.getComponent(), true)
            .addTextNode(formatter.getLineSeparator());

        bomComponent.getComponentVersion().ifPresent(version -> documentBuilder.addParagraphNode()
            .addTextNode(version, true)
            .addParagraphNode());

        gatherAttributeStrings(bomComponent, documentBuilder);
    }

    public void gatherAttributeStrings(AbstractBomComponentDetails bomComponent, AtlassianDocumentBuilder documentBuilder) {
        LinkableItem licenseItem = bomComponent.getLicense();
        String usageText = bomComponent.getUsage();
        ComponentUpgradeGuidance componentUpgradeGuidance = bomComponent.getComponentUpgradeGuidance();
        List<LinkableItem> additionalAttributes = bomComponent.getAdditionalAttributes();

        String licenseString = formatAttributeLabel(licenseItem);
        documentBuilder.startBulletList()
            .addListItem()
            .addTextNode(licenseString)
            .addTextNode(formatter.encode(licenseItem.getValue()), licenseItem.getUrl().map(formatter::encode).orElse(null))
            .addTextNode(formatter.getLineSeparator());

        LinkableItem usageItem = new LinkableItem("Usage", usageText);
        String usageString = formatAttributeLabel(usageItem);
        documentBuilder
            .addListItem()
            .addTextNode(usageString)
            .addTextNode(formatter.encode(usageItem.getValue()))
            .addTextNode(formatter.getLineSeparator());

        componentUpgradeGuidance.getShortTermUpgradeGuidance()
            .ifPresent(attr -> documentBuilder
                .addListItem()
                .addTextNode(formatAttributeLabel(attr))
                .addTextNode(formatter.encode(attr.getValue()), attr.getUrl().map(formatter::encode).orElse(null))
                .addTextNode(formatter.getLineSeparator()));
        componentUpgradeGuidance.getLongTermUpgradeGuidance()
            .ifPresent(attr -> documentBuilder
                .addListItem()
                .addTextNode(formatAttributeLabel(attr))
                .addTextNode(formatter.encode(attr.getValue()), attr.getUrl().map(formatter::encode).orElse(null))
                .addTextNode(formatter.getLineSeparator()));

        additionalAttributes
            .forEach(attr -> documentBuilder
                .addListItem()
                .addTextNode(formatAttributeLabel(attr))
                .addTextNode(formatter.encode(attr.getValue()), attr.getUrl().map(formatter::encode).orElse(null))
                .addTextNode(formatter.getLineSeparator()));

        documentBuilder.finishBulletList();
    }

    private String formatAttributeLabel(LinkableItem linkableItem) {
        String label = formatter.encode(linkableItem.getLabel());
        String formattedValue = String.format("%s:%s", label, formatter.getNonBreakingSpace());
        return String.format("%s%s", formatter.getNonBreakingSpace(), formattedValue);
    }
}
