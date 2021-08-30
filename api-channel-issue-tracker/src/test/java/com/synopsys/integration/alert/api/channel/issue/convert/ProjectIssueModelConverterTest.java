package com.synopsys.integration.alert.api.channel.issue.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.issue.convert.mock.MockIssueTrackerMessageFormatter;
import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueVulnerabilityDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueVulnerabilityModel;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.AbstractBomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentPolicy;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;

public class ProjectIssueModelConverterTest {
    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(15L, new LinkableItem("Provider", "A provider", "https://provider-url"));
    private static final LinkableItem PROJECT_ITEM = new LinkableItem("Project", "A project", "https://project-url");
    private static final LinkableItem PROJECT_VERSION_ITEM = new LinkableItem("Project Version", "2.3.4-RC", "https://project-version-url");
    private static final LinkableItem COMPONENT_ITEM = new LinkableItem("Component", "A BOM component");
    private static final LinkableItem COMPONENT_VERSION_ITEM = new LinkableItem("Component Version", "1.0.0-SNAPSHOT");
    private static final ComponentPolicy COMPONENT_POLICY = new ComponentPolicy("A policy", ComponentConcernSeverity.UNSPECIFIED_UNKNOWN, false, false, null, "Uncategorized");
    private static final ComponentVulnerabilities COMPONENT_VULNERABILITIES = new ComponentVulnerabilities(List.of(), List.of(), List.of(), List.of(new LinkableItem("Vulnerability", "CVE-007")));
    private static final AbstractBomComponentDetails BOM_COMPONENT_DETAILS = createBomComponentDetailsWithComponentVulnerabilities(COMPONENT_VULNERABILITIES);
    private static final IssueBomComponentDetails ISSUE_BOM_COMPONENT_DETAILS = IssueBomComponentDetails.fromBomComponentDetails(BOM_COMPONENT_DETAILS);
    private static final ExistingIssueDetails<String> EXISTING_ISSUE_DETAILS = new ExistingIssueDetails<>("issue-id", "issue-key", "a summary", "https://ui-link", IssueStatus.UNKNOWN, IssueCategory.BOM);

    @Test
    public void toIssueCreationModelBomTest() {
        ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, ISSUE_BOM_COMPONENT_DETAILS);
        basicIssueCreationModelTest(projectIssueModel);
    }

    @Test
    public void toIssueCreationModelPolicyTest() {
        IssuePolicyDetails testPolicy = new IssuePolicyDetails("Test Policy", ItemOperation.ADD, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN);
        ProjectIssueModel projectIssueModel = ProjectIssueModel.policy(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, ISSUE_BOM_COMPONENT_DETAILS, testPolicy);
        IssueCreationModel issueCreationModel = basicIssueCreationModelTest(projectIssueModel);
        assertTrue(issueCreationModel.getTitle().contains(testPolicy.getName()), "Expected policy name to be present in the title");
        assertTrue(issueCreationModel.getDescription().contains(testPolicy.getName()), "Expected policy name to be present in the description");
    }

    @Test
    public void toIssueCreationModelVulnerabilityTest() {
        IssueVulnerabilityModel vuln1 = createIssueVulnerability(ComponentConcernSeverity.MAJOR_HIGH, "CVE-13579", "https://a-url");
        IssueVulnerabilityModel vuln2 = createIssueVulnerability(ComponentConcernSeverity.MAJOR_HIGH, "CVE-24680", "https://a-url");
        IssueVulnerabilityModel vuln3 = createIssueVulnerability(ComponentConcernSeverity.MAJOR_HIGH, "CVE-235711", "https://a-url");
        IssueVulnerabilityModel vuln4 = createIssueVulnerability(ComponentConcernSeverity.MAJOR_HIGH, "CVE-112358", "https://a-url");
        IssueVulnerabilityDetails vulnerabilityDetails = new IssueVulnerabilityDetails(
            false,
            List.of(),
            List.of(vuln1),
            List.of(vuln2, vuln3, vuln4)
        );
        ProjectIssueModel projectIssueModel = ProjectIssueModel.vulnerability(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, ISSUE_BOM_COMPONENT_DETAILS, vulnerabilityDetails);
        IssueCreationModel issueCreationModel = basicIssueCreationModelTest(projectIssueModel);
        assertTrue(issueCreationModel.getTitle().contains(vuln1.getVulnerability().getLabel()), "Expected vulnerability label to be present in the title");
    }

    @Test
    public void toIssueCreationModelWithVulnerabilitySeverityTest() {
        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();
        ProjectIssueModelConverter converter = new ProjectIssueModelConverter(formatter);

        IssueVulnerabilityModel vuln = createIssueVulnerability(ComponentConcernSeverity.MAJOR_HIGH, "CVE-13579", "https://a-url");
        IssueVulnerabilityDetails vulnerabilityDetails = new IssueVulnerabilityDetails(false, List.of(), List.of(vuln), List.of());
        ComponentVulnerabilities componentVulnerabilities = new ComponentVulnerabilities(
            List.of(new LinkableItem("VulnerabilityCritical", "CVE-004")),
            List.of(new LinkableItem("VulnerabilityHigh", "CVE-005")),
            List.of(new LinkableItem("VulnerabilityMedium", "CVE-006")),
            List.of(new LinkableItem("VulnerabilityLow", "CVE-007")));
        AbstractBomComponentDetails vulnerableBomComponentDetails = createBomComponentDetailsWithComponentVulnerabilities(componentVulnerabilities);
        IssueBomComponentDetails issueBomComponentDetails = IssueBomComponentDetails.fromBomComponentDetails(vulnerableBomComponentDetails);

        ProjectIssueModel projectIssueModel = ProjectIssueModel.vulnerability(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, issueBomComponentDetails, vulnerabilityDetails);
        IssueCreationModel issueCreationModel = converter.toIssueCreationModel(projectIssueModel, "jobName");

        assertTrue(issueCreationModel.getDescription().contains(ComponentConcernSeverity.CRITICAL.getVulnerabilityLabel()), "Expected highest vulnerability severity in the description to be CRITICAL");
    }

    @Test
    public void toIssueTransitionModelOpenTest() {
        IssueTransitionModel<String> issueTransitionModel = basicIssueTransitionModelTest(ItemOperation.ADD);
        assertEquals(IssueOperation.OPEN, issueTransitionModel.getIssueOperation());
    }

    @Test
    public void toIssueTransitionModelResolveTest() {
        IssueTransitionModel<String> issueTransitionModel = basicIssueTransitionModelTest(ItemOperation.DELETE);
        assertEquals(IssueOperation.RESOLVE, issueTransitionModel.getIssueOperation());
    }

    @Test
    public void toIssueCommentModelTest() {
        ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, ISSUE_BOM_COMPONENT_DETAILS);

        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();
        ProjectIssueModelConverter converter = new ProjectIssueModelConverter(formatter);

        IssueCommentModel<String> issueCommentModel = converter.toIssueCommentModel(EXISTING_ISSUE_DETAILS, projectIssueModel);
        assertEquals(EXISTING_ISSUE_DETAILS, issueCommentModel.getExistingIssueDetails());
        assertEquals(projectIssueModel, issueCommentModel.getSource().orElse(null));
        assertTrue(issueCommentModel.getComments().size() > 0, "Expected non-zero number of comments");
    }

    @Test
    public void toIssueCommentModelWithVulnerabilitySeverityTest() {
        IssueVulnerabilityModel vuln1 = createIssueVulnerability(ComponentConcernSeverity.MAJOR_HIGH, "CVE-13579", "https://a-url");
        IssueVulnerabilityDetails vulnerabilityDetails = new IssueVulnerabilityDetails(false, List.of(), List.of(vuln1), List.of());
        ComponentVulnerabilities componentVulnerabilities = new ComponentVulnerabilities(
            List.of(new LinkableItem("VulnerabilityCritical", "CVE-004")),
            List.of(new LinkableItem("VulnerabilityHigh", "CVE-005")),
            List.of(new LinkableItem("VulnerabilityMedium", "CVE-006")),
            List.of(new LinkableItem("VulnerabilityLow", "CVE-007")));
        AbstractBomComponentDetails vulnerableBomComponentDetails = createBomComponentDetailsWithComponentVulnerabilities(componentVulnerabilities);
        IssueBomComponentDetails issueBomComponentDetails = IssueBomComponentDetails.fromBomComponentDetails(vulnerableBomComponentDetails);

        ProjectIssueModel projectIssueModel = ProjectIssueModel.vulnerability(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, issueBomComponentDetails, vulnerabilityDetails);

        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();
        ProjectIssueModelConverter converter = new ProjectIssueModelConverter(formatter);

        IssueCommentModel<String> issueCommentModel = converter.toIssueCommentModel(EXISTING_ISSUE_DETAILS, projectIssueModel);
        assertEquals(1, issueCommentModel.getComments().size());
        String comments = issueCommentModel.getComments().get(0);
        assertTrue(comments.contains(ComponentConcernSeverity.CRITICAL.getVulnerabilityLabel()), "Expected highest vulnerability severity in the comment to be CRITICAL");
    }

    @Test
    public void toIssueCommentModelWithEmptyVulnerabilitySeverityTest() {
        IssueVulnerabilityModel vuln1 = createIssueVulnerability(ComponentConcernSeverity.MAJOR_HIGH, "CVE-13579", "https://a-url");
        IssueVulnerabilityDetails vulnerabilityDetails = new IssueVulnerabilityDetails(false, List.of(), List.of(vuln1), List.of());
        ComponentVulnerabilities componentVulnerabilities = new ComponentVulnerabilities(List.of(), List.of(), List.of(), List.of());
        AbstractBomComponentDetails vulnerableBomComponentDetails = createBomComponentDetailsWithComponentVulnerabilities(componentVulnerabilities);
        IssueBomComponentDetails issueBomComponentDetails = IssueBomComponentDetails.fromBomComponentDetails(vulnerableBomComponentDetails);

        ProjectIssueModel projectIssueModel = ProjectIssueModel.vulnerability(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, issueBomComponentDetails, vulnerabilityDetails);

        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();
        ProjectIssueModelConverter converter = new ProjectIssueModelConverter(formatter);

        IssueCommentModel<String> issueCommentModel = converter.toIssueCommentModel(EXISTING_ISSUE_DETAILS, projectIssueModel);
        assertEquals(1, issueCommentModel.getComments().size());
        String comments = issueCommentModel.getComments().get(0);
        assertTrue(comments.contains("None"), "Expected missing vulnerability severity to return Severity Status: None");
    }

    private IssueCreationModel basicIssueCreationModelTest(ProjectIssueModel projectIssueModel) {
        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();
        ProjectIssueModelConverter converter = new ProjectIssueModelConverter(formatter);

        IssueCreationModel issueCreationModel = converter.toIssueCreationModel(projectIssueModel, "jobName");
        assertEquals(projectIssueModel, issueCreationModel.getSource().orElse(null));

        String issueCreationModelTitle = issueCreationModel.getTitle();
        assertTrue(issueCreationModelTitle.contains(PROVIDER_DETAILS.getProvider().getValue()), "Expected provider value to be present in the title");
        assertTrue(issueCreationModelTitle.contains(PROJECT_ITEM.getValue()), "Expected project value to be present in the title");
        assertTrue(issueCreationModelTitle.contains(PROJECT_VERSION_ITEM.getValue()), "Expected project-version value to be present in the title");
        assertTrue(issueCreationModelTitle.contains(COMPONENT_ITEM.getValue()), "Expected component value to be present in the title");
        assertTrue(issueCreationModelTitle.contains(COMPONENT_VERSION_ITEM.getValue()), "Expected component-version value to be present in the title");
        return issueCreationModel;
    }

    private IssueTransitionModel<String> basicIssueTransitionModelTest(ItemOperation operation) {
        ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, ISSUE_BOM_COMPONENT_DETAILS);

        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();
        ProjectIssueModelConverter converter = new ProjectIssueModelConverter(formatter);

        IssueTransitionModel<String> issueTransitionModel = converter.toIssueTransitionModel(EXISTING_ISSUE_DETAILS, projectIssueModel, operation);
        assertEquals(EXISTING_ISSUE_DETAILS, issueTransitionModel.getExistingIssueDetails());
        assertEquals(projectIssueModel, issueTransitionModel.getSource());
        return issueTransitionModel;
    }

    private IssueVulnerabilityModel createIssueVulnerability(ComponentConcernSeverity severity, String id, @Nullable String url) {
        LinkableItem vulnerability = new LinkableItem("Vulnerability", id, url);
        return new IssueVulnerabilityModel(severity, vulnerability);
    }

    private static AbstractBomComponentDetails createBomComponentDetailsWithComponentVulnerabilities(ComponentVulnerabilities componentVulnerabilities) {
        return new AbstractBomComponentDetails(
            COMPONENT_ITEM,
            COMPONENT_VERSION_ITEM,
            componentVulnerabilities,
            List.of(COMPONENT_POLICY),
            new LinkableItem("License", "A software license", "https://license-url"),
            "Example Usage",
            ComponentUpgradeGuidance.none(),
            List.of(
                new LinkableItem("Attribute", "Example attribute")
            ),
            "https://issues-url"
        ) {};
    }
}
