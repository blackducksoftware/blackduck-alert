package com.synopsys.integration.alert.api.channel.convert;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.convert.mock.MockChannelMessageFormatter;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.AbstractBomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentPolicy;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;

public class BomComponentDetailConverterTest {
    private static final AbstractBomComponentDetails BOM_COMPONENT_DETAILS = createBomComponentDetails();

    @Test
    public void gatherAbstractBomComponentSectionPiecesTest() {
        callGatherAbstractBomComponentSectionPieces();
    }

    @Disabled
    @Test
    public void previewAbstractBomComponentSectionPiecesFormatting() {
        List<String> sectionPieces = callGatherAbstractBomComponentSectionPieces();
        printSectionPieces(sectionPieces);
    }

    @Test
    public void gatherAttributeStringsTest() {
        callGatherAttributeStrings();
    }

    @Disabled
    @Test
    public void previewAttributeStringsFormatting() {
        List<String> sectionPieces = callGatherAttributeStrings();
        printSectionPieces(sectionPieces);
    }

    private List<String> callGatherAbstractBomComponentSectionPieces() {
        MockChannelMessageFormatter mockChannelMessageFormatter = new MockChannelMessageFormatter(Integer.MAX_VALUE);
        BomComponentDetailConverter bomComponentDetailConverter = new BomComponentDetailConverter(mockChannelMessageFormatter);

        return bomComponentDetailConverter.gatherAbstractBomComponentSectionPieces(BOM_COMPONENT_DETAILS);
    }

    public List<String> callGatherAttributeStrings() {
        MockChannelMessageFormatter mockChannelMessageFormatter = new MockChannelMessageFormatter(Integer.MAX_VALUE);
        BomComponentDetailConverter bomComponentDetailConverter = new BomComponentDetailConverter(mockChannelMessageFormatter);

        return bomComponentDetailConverter.gatherAttributeStrings(BOM_COMPONENT_DETAILS);
    }

    private void printSectionPieces(List<String> sectionPieces) {
        String joinedSectionPieces = StringUtils.join(sectionPieces, "");
        System.out.print(joinedSectionPieces);
    }

    private static AbstractBomComponentDetails createBomComponentDetails() {
        ComponentPolicy componentPolicy1 = new ComponentPolicy("A Black Duck Policy", ComponentConcernSeverity.MAJOR_HIGH, true, false, null, "Uncategorized");
        ComponentPolicy componentPolicy2 = new ComponentPolicy("A Different Black Duck Policy", ComponentConcernSeverity.UNSPECIFIED_UNKNOWN, false, true, null, "Uncategorized");

        LinkableItem shortTermUpgradeGuidance = new LinkableItem("Upgrade Guidance - Short Term", "1.0");
        LinkableItem longTermUpgradeGuidance = new LinkableItem("Upgrade Guidance - Long Term", "2.0");
        ComponentUpgradeGuidance componentUpgradeGuidance = new ComponentUpgradeGuidance(shortTermUpgradeGuidance, longTermUpgradeGuidance);

        LinkableItem attribute1 = new LinkableItem("Attribute", "Number 1");
        LinkableItem attribute2 = new LinkableItem("Attribute", "Number 2");

        return new AbstractBomComponentDetails(
            new LinkableItem("Component", "A BOM Component"),
            new LinkableItem("Component Version", "A BOM Component Version"),
            createComponentVulnerabilities(),
            List.of(componentPolicy1, componentPolicy2),
            new LinkableItem("License", "A Software License"),
            "Example Usage",
            componentUpgradeGuidance,
            List.of(attribute1, attribute2),
            "https://a-blackduck-url"
        ) {};
    }

    private static ComponentVulnerabilities createComponentVulnerabilities() {
        LinkableItem vulnerability1 = creatVulnerabilityLinkableItem("CVE-001");
        LinkableItem vulnerability2 = creatVulnerabilityLinkableItem("CVE-002");
        LinkableItem vulnerability3 = creatVulnerabilityLinkableItem("CVE-003");
        LinkableItem vulnerability4 = creatVulnerabilityLinkableItem("CVE-004");
        LinkableItem vulnerability5 = creatVulnerabilityLinkableItem("CVE-005");
        LinkableItem vulnerability6 = creatVulnerabilityLinkableItem("CVE-006");
        return new ComponentVulnerabilities(
            List.of(),
            List.of(vulnerability1),
            List.of(vulnerability2, vulnerability3),
            List.of(vulnerability4, vulnerability5, vulnerability6)
        );
    }

    private static LinkableItem creatVulnerabilityLinkableItem(String value) {
        return new LinkableItem("Vulnerability", value, "https://a-blackduck-url");
    }

}
