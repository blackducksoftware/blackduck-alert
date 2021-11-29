package com.synopsys.integration.alert.api.channel.issue.convert;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.issue.convert.mock.MockIssueTrackerMessageFormatter;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;

public class ComponentVulnerabilitiesConverterTest {
    private static final ComponentVulnerabilities NO_VULNERABILITIES = new ComponentVulnerabilities(List.of(), List.of(), List.of(), List.of());
    private static final ComponentVulnerabilities SOME_VULNERABILITIES = new ComponentVulnerabilities(
        List.of(
            createVulnerabilityItem("CVE-867"),
            createVulnerabilityItem("CVE-5309")
        ),
        List.of(),
        List.of(
            // null URL test-case
            new LinkableItem("Vulnerability", "CVE-420")
        ),
        List.of(
            createVulnerabilityItem("BDSA-0000"),
            createVulnerabilityItem("BDSA-00000"),
            createVulnerabilityItem("BDSA-000000"),
            createVulnerabilityItem("BDSA-0000000"),
            createVulnerabilityItem("BDSA-00000000")
        )
    );

    @Test
    public void createComponentVulnerabilitiesSectionPiecesWithVulnerabilitiesTest() {
        callCreateComponentVulnerabilitiesSectionPieces(SOME_VULNERABILITIES);
    }

    @Disabled
    @Test
    public void previewFormattingWithVulnerabilities() {
        List<String> sectionPieces = callCreateComponentVulnerabilitiesSectionPieces(SOME_VULNERABILITIES);
        String joinedSectionPieces = StringUtils.join(sectionPieces, "");
        System.out.print(joinedSectionPieces);
    }

    @Test
    public void createComponentVulnerabilitiesSectionPiecesWithoutVulnerabilitiesTest() {
        callCreateComponentVulnerabilitiesSectionPieces(NO_VULNERABILITIES);
    }

    @Disabled
    @Test
    public void previewFormattingWithoutVulnerabilities() {
        List<String> sectionPieces = callCreateComponentVulnerabilitiesSectionPieces(NO_VULNERABILITIES);
        String joinedSectionPieces = StringUtils.join(sectionPieces, "");
        System.out.print(joinedSectionPieces);
    }

    private List<String> callCreateComponentVulnerabilitiesSectionPieces(ComponentVulnerabilities componentVulnerabilities) {
        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();
        ComponentVulnerabilitiesConverter converter = new ComponentVulnerabilitiesConverter(formatter);
        return converter.createComponentVulnerabilitiesSectionPieces(componentVulnerabilities);
    }

    private static LinkableItem createVulnerabilityItem(String id) {
        return new LinkableItem("Vulnerability", id, "https://vuln-url");
    }

}
