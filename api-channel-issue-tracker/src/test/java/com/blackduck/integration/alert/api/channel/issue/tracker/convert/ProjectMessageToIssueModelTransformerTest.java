/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueBomComponentDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueComponentUnknownVersionDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueEstimatedRiskModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssuePolicyDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueVulnerabilityDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.BomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcern;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernSeverity;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentPolicy;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentUpgradeGuidance;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentVulnerabilities;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

public class ProjectMessageToIssueModelTransformerTest {
    private static final LinkableItem PROVIDER = new LinkableItem("Black Duck", "server-name", "https://blackduck-server-url");
    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(0L, PROVIDER);

    private static final LinkableItem PROJECT = new LinkableItem("Project", "A Black Duck project");
    private static final LinkableItem PROJECT_VERSION = new LinkableItem("Project Version", "a-version");

    private static final LinkableItem COMPONENT = new LinkableItem("Component", "A BOM component");
    private static final LinkableItem COMPONENT_VERSION = new LinkableItem("Component Version", "0.8.7");
    private static final LinkableItem LICENSE = new LinkableItem("License", "A software license", "https://license-url");
    private static final String USAGE = "Some generic usage";
    private static final LinkableItem SHORT_TERM_GUIDANCE = new LinkableItem("Upgrade Guidance - Short Term", "1.0");
    private static final LinkableItem LONG_TERM_GUIDANCE = new LinkableItem("Upgrade Guidance - Long Term", "2.0");
    private static final ComponentUpgradeGuidance UPGRADE_GUIDANCE = new ComponentUpgradeGuidance(SHORT_TERM_GUIDANCE, LONG_TERM_GUIDANCE);
    private static final LinkableItem ADDITIONAL_ATTRIBUTE = new LinkableItem("A Label", "An attribute value");
    private static final String ISSUES_URL = "https://issues-url";

    private static final LinkableItem VULNERABILITY_1 = createVulnerabilityItem("CVE-123");
    private static final LinkableItem VULNERABILITY_2 = createVulnerabilityItem("CVE-456");
    private static final LinkableItem VULNERABILITY_3 = createVulnerabilityItem("CVE-789");
    private static final LinkableItem VULNERABILITY_4 = createVulnerabilityItem("CVE-135");
    private static final LinkableItem VULNERABILITY_5 = createVulnerabilityItem("CVE-246");
    private static final LinkableItem VULNERABILITY_6 = createVulnerabilityItem("CVE-369");
    private static final ComponentVulnerabilities COMPONENT_VULNERABILITIES = new ComponentVulnerabilities(
        List.of(),
        List.of(VULNERABILITY_1),
        List.of(VULNERABILITY_2, VULNERABILITY_3),
        List.of(VULNERABILITY_4, VULNERABILITY_5, VULNERABILITY_6)
    );

    private static final ComponentPolicy COMPONENT_POLICY_1 = new ComponentPolicy("First Policy", ComponentConcernSeverity.MINOR_MEDIUM, false, true, null, "Uncategorized");
    private static final ComponentPolicy COMPONENT_POLICY_2 = new ComponentPolicy("Second Policy", ComponentConcernSeverity.UNSPECIFIED_UNKNOWN, true, false, null, "Uncategorized");
    private static final List<ComponentPolicy> COMPONENT_POLICIES = List.of(
        COMPONENT_POLICY_1,
        COMPONENT_POLICY_2
    );

    @Test
    public void convertToIssueModelsForPolicyTest() {
        ComponentConcern policyConcern = ComponentConcern.policy(ItemOperation.ADD, COMPONENT_POLICY_1.getPolicyName(), "https://policy");
        BomComponentDetails bomComponentDetails = createBomComponentDetails(policyConcern);
        ProjectMessage projectMessage = ProjectMessage.componentConcern(
            PROVIDER_DETAILS,
            PROJECT,
            PROJECT_VERSION,
            List.of(bomComponentDetails)
        );

        ProjectMessageToIssueModelTransformer modelTransformer = new ProjectMessageToIssueModelTransformer();
        List<ProjectIssueModel> policyIssueModels = modelTransformer.convertToIssueModels(projectMessage);
        assertEquals(1, policyIssueModels.size());

        ProjectIssueModel policyIssueModel = policyIssueModels.get(0);
        assertRequiredDetails(policyIssueModel);

        Optional<IssuePolicyDetails> optionalPolicyDetails = policyIssueModel.getPolicyDetails();
        assertTrue(optionalPolicyDetails.isPresent(), "Expected policy details to be present");

        IssuePolicyDetails policyDetails = optionalPolicyDetails.get();
        assertEquals(policyConcern.getName(), policyDetails.getName());
        assertEquals(policyConcern.getOperation(), policyDetails.getOperation());
        assertEquals(policyConcern.getSeverity(), policyDetails.getSeverity());
    }

    @Test
    public void convertToIssueModelsForVulnerabilitiesTest() {
        LinkableItem vulnerabilityItem0 = createVulnerabilityItem("CVE-000");
        LinkableItem vulnerabilityItem7 = createVulnerabilityItem("CVE-007");
        ComponentConcern vulnConcern0 = ComponentConcern.vulnerability(ItemOperation.ADD, vulnerabilityItem0.getValue(), ComponentConcernSeverity.CRITICAL, vulnerabilityItem0.getUrl().orElse(null));
        ComponentConcern vulnConcern7 = ComponentConcern.vulnerability(ItemOperation.DELETE, vulnerabilityItem7.getValue(), ComponentConcernSeverity.MINOR_MEDIUM, vulnerabilityItem7.getUrl().orElse(null));
        BomComponentDetails bomComponentDetails = createBomComponentDetails(List.of(
            vulnConcern0,
            ComponentConcern.vulnerability(ItemOperation.UPDATE, VULNERABILITY_2.getValue(), ComponentConcernSeverity.MINOR_MEDIUM, VULNERABILITY_2.getUrl().orElse(null)),
            vulnConcern7
        ));
        ProjectMessage projectMessage = ProjectMessage.componentConcern(
            PROVIDER_DETAILS,
            PROJECT,
            PROJECT_VERSION,
            List.of(bomComponentDetails)
        );

        ProjectMessageToIssueModelTransformer modelTransformer = new ProjectMessageToIssueModelTransformer();
        List<ProjectIssueModel> vulnerabilityIssueModels = modelTransformer.convertToIssueModels(projectMessage);
        assertEquals(1, vulnerabilityIssueModels.size());

        ProjectIssueModel policyIssueModel = vulnerabilityIssueModels.get(0);
        assertRequiredDetails(policyIssueModel);

        Optional<IssueVulnerabilityDetails> optionalIssueVulnerabilityDetails = policyIssueModel.getVulnerabilityDetails();
        assertTrue(optionalIssueVulnerabilityDetails.isPresent(), "Expected vulnerability details to be present");

        IssueVulnerabilityDetails issueVulnerabilityDetails = optionalIssueVulnerabilityDetails.get();
        assertEquals(1, issueVulnerabilityDetails.getVulnerabilitiesAdded().size());
        assertEquals(1, issueVulnerabilityDetails.getVulnerabilitiesUpdated().size());
        assertEquals(1, issueVulnerabilityDetails.getVulnerabilitiesDeleted().size());
    }

    @Test
    public void convertToIssueModelsForComponentUnknownVersionTest() {
        ComponentConcern unknownComponentConcern = ComponentConcern.unknownComponentVersion(
            ItemOperation.ADD,
            "Component01",
            ComponentConcernSeverity.MAJOR_HIGH,
            2,
            "https://blackduck.com"
        );
        BomComponentDetails bomComponentDetails = createBomComponentDetails(unknownComponentConcern);
        ProjectMessage projectMessage = ProjectMessage.componentConcern(
            PROVIDER_DETAILS,
            PROJECT,
            PROJECT_VERSION,
            List.of(bomComponentDetails)
        );

        ProjectMessageToIssueModelTransformer modelTransformer = new ProjectMessageToIssueModelTransformer();
        List<ProjectIssueModel> policyIssueModels = modelTransformer.convertToIssueModels(projectMessage);
        assertEquals(1, policyIssueModels.size());

        ProjectIssueModel unknownVersionIssueModel = policyIssueModels.get(0);
        assertRequiredDetails(unknownVersionIssueModel);

        Optional<IssueComponentUnknownVersionDetails> optionalDetails = unknownVersionIssueModel.getComponentUnknownVersionDetails();
        assertTrue(optionalDetails.isPresent(), "Expected unknown component details to be present");

        IssueComponentUnknownVersionDetails details = optionalDetails.get();
        assertEquals(ItemOperation.ADD, details.getItemOperation());
        assertEquals(1, details.getEstimatedRiskModelList().size());
        IssueEstimatedRiskModel estimatedRiskModel = details.getEstimatedRiskModelList().get(0);
        assertEquals(ComponentConcernSeverity.MAJOR_HIGH, estimatedRiskModel.getSeverity());
        assertEquals("Component01", estimatedRiskModel.getName());
        assertEquals(2, estimatedRiskModel.getCount());
        assertTrue(estimatedRiskModel.getComponentVersionUrl().isPresent());
    }

    private static void assertRequiredDetails(ProjectIssueModel projectIssueModel) {
        assertEquals(PROVIDER_DETAILS, projectIssueModel.getProviderDetails());
        assertEquals(PROJECT, projectIssueModel.getProject());
        assertEquals(PROJECT_VERSION, projectIssueModel.getProjectVersion().orElse(null));

        IssueBomComponentDetails issueBomComponentDetails = projectIssueModel.getBomComponentDetails();
        assertEquals(COMPONENT, issueBomComponentDetails.getComponent());
        assertEquals(COMPONENT_VERSION, issueBomComponentDetails.getComponentVersion().orElse(null));
        assertEquals(COMPONENT_VULNERABILITIES, issueBomComponentDetails.getComponentVulnerabilities());
        assertEquals(COMPONENT_POLICIES, issueBomComponentDetails.getRelevantPolicies());
        assertEquals(LICENSE, issueBomComponentDetails.getLicense());
        assertEquals(USAGE, issueBomComponentDetails.getUsage());
        assertEquals(ISSUES_URL, issueBomComponentDetails.getBlackDuckIssuesUrl());
        assertEquals(UPGRADE_GUIDANCE, issueBomComponentDetails.getComponentUpgradeGuidance());
        assertTrue(
            issueBomComponentDetails.getAdditionalAttributes().contains(ADDITIONAL_ATTRIBUTE),
            "Expected issue BOM component details to contain an additional attribute"
        );
    }

    private static BomComponentDetails createBomComponentDetails(ComponentConcern componentConcern) {
        return createBomComponentDetails(List.of(componentConcern));
    }

    private static BomComponentDetails createBomComponentDetails(List<ComponentConcern> componentConcerns) {
        return new BomComponentDetails(
            COMPONENT,
            COMPONENT_VERSION,
            COMPONENT_VULNERABILITIES,
            COMPONENT_POLICIES,
            componentConcerns,
            LICENSE,
            USAGE,
            UPGRADE_GUIDANCE,
            List.of(ADDITIONAL_ATTRIBUTE),
            ISSUES_URL
        );
    }

    private static LinkableItem createVulnerabilityItem(String id) {
        return new LinkableItem("Vulnerability", id, "https://vuln-" + id);
    }

}
