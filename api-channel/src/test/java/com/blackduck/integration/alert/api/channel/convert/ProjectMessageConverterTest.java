/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.convert;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.channel.convert.mock.MockChannelMessageFormatter;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.BomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcern;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernSeverity;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentPolicy;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentUpgradeGuidance;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentVulnerabilities;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectOperation;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

public class ProjectMessageConverterTest {
    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(0L, new LinkableItem("Provider", "The provider"));
    private static final LinkableItem PROJECT_ITEM = new LinkableItem("Project", "The project", "https://a-url");
    private static final ProjectMessage PROJECT_MESSAGE_PROJECT = createProjectMessageForProject();
    private static final ProjectMessage PROJECT_MESSAGE_COMPONENT = createProjectMessageForComponent();

    @Test
    public void convertToFormattedMessageChunksProjectTest() {
        callConvertToFormattedMessageChunks(PROJECT_MESSAGE_PROJECT);
    }

    @Test
    @Disabled
    public void previewConvertToFormattedMessageChunksProjectFormatting() {
        List<String> messageChunks = callConvertToFormattedMessageChunks(PROJECT_MESSAGE_PROJECT);
        printMessageChunks(messageChunks);
    }

    @Test
    public void convertToFormattedMessageChunksComponentTest() {
        callConvertToFormattedMessageChunks(PROJECT_MESSAGE_COMPONENT);
    }

    @Test
    @Disabled
    public void previewConvertToFormattedMessageChunksComponentFormatting() {
        List<String> messageChunks = callConvertToFormattedMessageChunks(PROJECT_MESSAGE_COMPONENT);
        printMessageChunks(messageChunks);
    }

    private List<String> callConvertToFormattedMessageChunks(ProjectMessage projectMessage) {
        MockChannelMessageFormatter formatter = new MockChannelMessageFormatter(Integer.MAX_VALUE);
        ProjectMessageConverter projectMessageConverter = new ProjectMessageConverter(formatter);

        return projectMessageConverter.convertToFormattedMessageChunks(projectMessage, "jobName");
    }

    private void printMessageChunks(List<String> messageChunks) {
        String joinedSectionPieces = StringUtils.join(messageChunks, "");
        System.out.print(joinedSectionPieces);
    }

    private static ProjectMessage createProjectMessageForProject() {
        return ProjectMessage.projectStatusInfo(PROVIDER_DETAILS, PROJECT_ITEM, ProjectOperation.CREATE);
    }

    private static ProjectMessage createProjectMessageForComponent() {
        BomComponentDetails bomComponent = createBomComponentDetails();
        LinkableItem projectVersion = new LinkableItem("Project Version", "The project version", "https://a-url");
        return ProjectMessage.componentConcern(PROVIDER_DETAILS, PROJECT_ITEM, projectVersion, List.of(bomComponent));
    }

    private static BomComponentDetails createBomComponentDetails() {
        ComponentPolicy componentPolicy1 = new ComponentPolicy("A component policy", ComponentConcernSeverity.UNSPECIFIED_UNKNOWN, true, false, null, "Uncategorized");
        ComponentPolicy componentPolicy2 = new ComponentPolicy("A different policy", ComponentConcernSeverity.MAJOR_HIGH, false, true, null, "Uncategorized");

        ComponentConcern policyConcern1 = ComponentConcern.policy(ItemOperation.DELETE, "A non-severe policy", "https://policy");
        ComponentConcern policyConcern2 = ComponentConcern.severePolicy(ItemOperation.ADD, "A severe policy", ComponentConcernSeverity.TRIVIAL_LOW, "https://severe-policy");

        ComponentConcern vulnerabilityConcern1 = createVulnerabilityConcern(ItemOperation.ADD, "CVE-123", ComponentConcernSeverity.CRITICAL);
        ComponentConcern vulnerabilityConcern2 = createVulnerabilityConcern(ItemOperation.UPDATE, "CVE-135", ComponentConcernSeverity.TRIVIAL_LOW);
        ComponentConcern vulnerabilityConcern3 = createVulnerabilityConcern(ItemOperation.DELETE, "CVE-246", ComponentConcernSeverity.MINOR_MEDIUM);

        LinkableItem shortTermUpgradeGuidance = new LinkableItem("Upgrade Guidance - Short Term", "1.0");
        LinkableItem longTermUpgradeGuidance = new LinkableItem("Upgrade Guidance - Long Term", "2.0");
        ComponentUpgradeGuidance componentUpgradeGuidance = new ComponentUpgradeGuidance(shortTermUpgradeGuidance, longTermUpgradeGuidance);

        ComponentConcern unknownVersionConcern1 = ComponentConcern.unknownComponentVersion(
            ItemOperation.ADD,
            "Component-Unknown-Version-01",
            ComponentConcernSeverity.CRITICAL,
            0,
            "https://blackduck.com"
        );
        ComponentConcern unknownVersionConcern2 = ComponentConcern.unknownComponentVersion(
            ItemOperation.ADD,
            "Component-Unknown-Version-01",
            ComponentConcernSeverity.MAJOR_HIGH,
            1,
            "https://blackduck.com"
        );
        ComponentConcern unknownVersionConcern3 = ComponentConcern.unknownComponentVersion(
            ItemOperation.ADD,
            "Component-Unknown-Version-01",
            ComponentConcernSeverity.MINOR_MEDIUM,
            2,
            "https://blackduck.com"
        );
        ComponentConcern unknownVersionConcern4 = ComponentConcern.unknownComponentVersion(
            ItemOperation.ADD,
            "Component-Unknown-Version-01",
            ComponentConcernSeverity.TRIVIAL_LOW,
            3,
            "https://blackduck.com"
        );

        LinkableItem attribute1 = new LinkableItem("Attribute", "The first attribute");
        LinkableItem attribute2 = new LinkableItem("Attribute Prime", "The second attribute");

        return new BomComponentDetails(
            new LinkableItem("Component", "The component"),
            new LinkableItem("Component Version", "The component version"),
            createComponentVulnerabilities(),
            List.of(componentPolicy1, componentPolicy2),
            List.of(
                policyConcern1,
                policyConcern2,
                vulnerabilityConcern1,
                vulnerabilityConcern2,
                vulnerabilityConcern3,
                unknownVersionConcern1,
                unknownVersionConcern2,
                unknownVersionConcern3,
                unknownVersionConcern4
            ),
            new LinkableItem("License", "The software license name", "https://license-url"),
            "The usage of the component",
            componentUpgradeGuidance,
            List.of(attribute1, attribute2),
            "https://blackduck-issues-url"
        );
    }

    private static ComponentVulnerabilities createComponentVulnerabilities() {
        LinkableItem vuln1 = createComponentVulnerability("CVE-123");
        LinkableItem vuln2 = createComponentVulnerability("CVE-135");
        LinkableItem vuln3 = createComponentVulnerability("CVE-007");
        LinkableItem vuln4 = createComponentVulnerability("CVE-099");
        LinkableItem vuln5 = createComponentVulnerability("CVE-230");
        return new ComponentVulnerabilities(
            List.of(vuln1),
            List.of(vuln3, vuln4),
            List.of(),
            List.of(vuln2, vuln5)
        );
    }

    private static ComponentConcern createVulnerabilityConcern(ItemOperation op, String vulnId, ComponentConcernSeverity severity) {
        return ComponentConcern.vulnerability(op, vulnId, severity, "https://vuln-url");
    }

    private static LinkableItem createComponentVulnerability(String vulnId) {
        return new LinkableItem("Vulnerability", vulnId, "https://vuln-url");
    }

}
