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

        String licenseString = formatAttribute(licenseItem);
        documentBuilder.addTextNode(licenseString, licenseItem.getUrl().map(formatter::encode).orElse(null))
            .addTextNode(formatter.getLineSeparator());

        LinkableItem usageItem = new LinkableItem("Usage", usageText);
        String usageString = formatAttribute(usageItem);
        documentBuilder.addTextNode(usageString)
            .addTextNode(formatter.getLineSeparator());

        componentUpgradeGuidance.getShortTermUpgradeGuidance()
            .ifPresent(attr -> documentBuilder.addTextNode(formatAttribute(attr), attr.getUrl().map(formatter::encode).orElse(null))
                .addTextNode(formatter.getLineSeparator()));
        componentUpgradeGuidance.getLongTermUpgradeGuidance()
            .ifPresent(attr -> documentBuilder.addTextNode(formatAttribute(attr), attr.getUrl().map(formatter::encode).orElse(null))
                .addTextNode(formatter.getLineSeparator()));

        additionalAttributes
            .forEach(attr -> documentBuilder.addTextNode(formatAttribute(attr), attr.getUrl().map(formatter::encode).orElse(null))
                .addTextNode(formatter.getLineSeparator()));
    }

    private String formatAttribute(LinkableItem linkableItem) {
        String label = formatter.encode(linkableItem.getLabel());
        String value = formatter.encode(linkableItem.getValue());
        String formattedValue = String.format("%s:%s%s", label, formatter.getNonBreakingSpace(), value);
        return String.format("%s-%s%s", formatter.getNonBreakingSpace(), formatter.getNonBreakingSpace(), formattedValue);
    }
}
