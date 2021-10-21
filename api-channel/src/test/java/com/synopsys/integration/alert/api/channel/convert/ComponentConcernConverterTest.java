package com.synopsys.integration.alert.api.channel.convert;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.convert.mock.MockChannelMessageFormatter;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;

public class ComponentConcernConverterTest {
    @Test
    public void gatherComponentConcernSectionPiecesTest() {
        callGatherComponentConcernSectionPieces();
    }

    @Disabled
    @Test
    public void previewFormatting() {
        List<String> sectionPieces = callGatherComponentConcernSectionPieces();
        String joinedSectionPieces = StringUtils.join(sectionPieces, "");
        System.out.print(joinedSectionPieces);
    }

    private List<String> callGatherComponentConcernSectionPieces() {
        ChannelMessageFormatter channelMessageFormatter = new MockChannelMessageFormatter(Integer.MAX_VALUE);
        ComponentConcernConverter componentConcernConverter = new ComponentConcernConverter(channelMessageFormatter);

        List<ComponentConcern> componentConcerns = createALotOfComponentConcerns();

        return componentConcernConverter.gatherComponentConcernSectionPieces(componentConcerns);
    }

    private List<ComponentConcern> createALotOfComponentConcerns() {
        return List.of(
            ComponentConcern.policy(ItemOperation.ADD, "Added Policy", "https://policy"),
            ComponentConcern.vulnerability(ItemOperation.ADD, "Added-Vuln01", ComponentConcernSeverity.CRITICAL, "https://synopsys.com"),
            ComponentConcern.policy(ItemOperation.ADD, "Added Another Policy", "https://policy"),
            ComponentConcern.vulnerability(ItemOperation.ADD, "Added-Vuln02", ComponentConcernSeverity.CRITICAL, "https://synopsys.com"),
            ComponentConcern.severePolicy(ItemOperation.ADD, "Added Severe Policy", ComponentConcernSeverity.TRIVIAL_LOW, "https://policy"),
            ComponentConcern.vulnerability(ItemOperation.ADD, "Added-Vuln03", ComponentConcernSeverity.CRITICAL, "https://synopsys.com"),
            ComponentConcern.vulnerability(ItemOperation.UPDATE, "Updated-Vuln01", ComponentConcernSeverity.MAJOR_HIGH, "https://synopsys.com"),
            ComponentConcern.severePolicy(ItemOperation.DELETE, "Removed Policy", ComponentConcernSeverity.MAJOR_HIGH, "https://policy"),
            ComponentConcern.vulnerability(ItemOperation.UPDATE, "Updated-Vuln02", ComponentConcernSeverity.MAJOR_HIGH, "https://synopsys.com"),
            ComponentConcern.severePolicy(ItemOperation.DELETE, "Removed Another Policy", ComponentConcernSeverity.MAJOR_HIGH, "https://policy"),
            ComponentConcern.policy(ItemOperation.DELETE, "Removed Severe Policy", "https://policy"),
            ComponentConcern.vulnerability(ItemOperation.DELETE, "Removed-Vuln01", ComponentConcernSeverity.MINOR_MEDIUM, "https://synopsys.com"),
            ComponentConcern.vulnerability(ItemOperation.DELETE, "Removed-Vuln02", ComponentConcernSeverity.MINOR_MEDIUM, "https://synopsys.com"),
            ComponentConcern.unknownComponentVersion(ItemOperation.ADD, "Added-Component01", ComponentConcernSeverity.MAJOR_HIGH, 5, "https://synopsys.com"),
            ComponentConcern.unknownComponentVersion(ItemOperation.DELETE, "Removed-Component01", ComponentConcernSeverity.MAJOR_HIGH, 0, "https://synopsys.com"),
            ComponentConcern.unknownComponentVersion(ItemOperation.ADD, "Added-Component02", ComponentConcernSeverity.MINOR_MEDIUM, 3, "https://synopsys.com"),
            ComponentConcern.unknownComponentVersion(ItemOperation.DELETE, "Removed-Component02", ComponentConcernSeverity.MINOR_MEDIUM, 0, "https://synopsys.com")
        );
    }

}
