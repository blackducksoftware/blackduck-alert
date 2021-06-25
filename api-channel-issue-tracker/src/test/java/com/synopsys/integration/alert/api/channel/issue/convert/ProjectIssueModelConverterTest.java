package com.synopsys.integration.alert.api.channel.issue.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.channel.issue.convert.mock.MockIssueTrackerMessageFormatter;
import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.AbstractBomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentPolicy;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;

public class ProjectIssueModelConverterTest {
    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(15L, new LinkableItem("Provider", "A provider", "https://provider-url"));
    private static final LinkableItem PROJECT_ITEM = new LinkableItem("Project", "A project", "https://project-url");
    private static final LinkableItem PROJECT_VERSION_ITEM = new LinkableItem("Project Version", "2.3.4-RC", "https://project-version-url");
    private static final LinkableItem COMPONENT_ITEM = new LinkableItem("Component", "A BOM component");
    private static final LinkableItem COMPONENT_VERSION_ITEM = new LinkableItem("Component Version", "1.0.0-SNAPSHOT");
    private static final AbstractBomComponentDetails BOM_COMPONENT_DETAILS = new AbstractBomComponentDetails(
        COMPONENT_ITEM,
        COMPONENT_VERSION_ITEM,
        new ComponentVulnerabilities(List.of(), List.of(), List.of(), List.of(new LinkableItem("Vulnerability", "CVE-007"))),
        List.of(new ComponentPolicy("A policy", ComponentConcernSeverity.UNSPECIFIED_UNKNOWN, false, false)),
        new LinkableItem("License", "A software license", "https://license-url"),
        "Example Usage",
        List.of(
            new LinkableItem("Attribute", "Example attribute")
        ),
        "https://issues-url"
    ) {};
    private static final IssueBomComponentDetails ISSUE_BOM_COMPONENT_DETAILS = IssueBomComponentDetails.fromBomComponentDetails(BOM_COMPONENT_DETAILS);
    private static final ExistingIssueDetails<String> EXISTING_ISSUE_DETAILS = new ExistingIssueDetails<>("issue-id", "issue-key", "a summary", "https://ui-link");

    @Test
    public void toIssueCreationModelTest() {
        ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, ISSUE_BOM_COMPONENT_DETAILS);

        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();
        ProjectIssueModelConverter converter = new ProjectIssueModelConverter(formatter);

        IssueCreationModel issueCreationModel = converter.toIssueCreationModel(projectIssueModel);
        assertEquals(projectIssueModel, issueCreationModel.getSource().orElse(null));

        String issueCreationModelTitle = issueCreationModel.getTitle();
        assertTrue(issueCreationModelTitle.contains(PROVIDER_DETAILS.getProvider().getValue()), "Expected provider value to be present in the title");
        assertTrue(issueCreationModelTitle.contains(PROJECT_ITEM.getValue()), "Expected project value to be present in the title");
        assertTrue(issueCreationModelTitle.contains(PROJECT_VERSION_ITEM.getValue()), "Expected project-version value to be present in the title");
        assertTrue(issueCreationModelTitle.contains(COMPONENT_ITEM.getValue()), "Expected component value to be present in the title");
        assertTrue(issueCreationModelTitle.contains(COMPONENT_VERSION_ITEM.getValue()), "Expected component-version value to be present in the title");
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

    private IssueTransitionModel<String> basicIssueTransitionModelTest(ItemOperation operation) {
        ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, ISSUE_BOM_COMPONENT_DETAILS);

        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();
        ProjectIssueModelConverter converter = new ProjectIssueModelConverter(formatter);

        IssueTransitionModel<String> issueTransitionModel = converter.toIssueTransitionModel(EXISTING_ISSUE_DETAILS, projectIssueModel, operation);
        assertEquals(EXISTING_ISSUE_DETAILS, issueTransitionModel.getExistingIssueDetails());
        assertEquals(projectIssueModel, issueTransitionModel.getSource());
        return issueTransitionModel;
    }

}
