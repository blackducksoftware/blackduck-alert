package com.synopsys.integration.alert.api.channel.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.channel.issue.convert.mock.MockIssueTrackerMessageFormatter;
import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerModelHolder;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.ActionableIssueSearchResult;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.IssueTrackerSearcher;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.SimpleMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.AbstractBomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

public class IssueTrackerModelExtractorTest {
    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(0L, new LinkableItem("Provider", "Provider Config Name", "https://provider-url"));

    private static final LinkableItem PROJECT = new LinkableItem("Project", "A Black Duck project");
    private static final LinkableItem PROJECT_VERSION = new LinkableItem("Project Version", "a-version");

    private static final LinkableItem COMPONENT = new LinkableItem("Component", "A BOM component");
    private static final LinkableItem COMPONENT_VERSION = new LinkableItem("Component Version", "0.8.7");
    private static final LinkableItem LICENSE = new LinkableItem("License", "A software license", "https://license-url");
    private static final String USAGE = "Some generic usage";
    private static final String ISSUES_URL = "https://issues-url";

    private static final AbstractBomComponentDetails BOM_COMPONENT_DETAILS = new BomComponentDetails(COMPONENT, COMPONENT_VERSION, ComponentVulnerabilities.none(), List.of(), List.of(), LICENSE, USAGE, ComponentUpgradeGuidance.none(),
        List.of(), ISSUES_URL);
    private static final IssueBomComponentDetails ISSUE_BOM_COMPONENT_DETAILS = IssueBomComponentDetails.fromBomComponentDetails(BOM_COMPONENT_DETAILS);

    @Test
    public void extractSimpleMessageIssueModelsTest() {
        String testSummary = "A test summary";
        String testDescription = "A description for the test";
        LinkableItem additionalDetail = new LinkableItem("A label", "A value");

        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();
        IssueTrackerModelExtractor<String> extractor = new IssueTrackerModelExtractor<>(formatter, null);

        SimpleMessage simpleMessage = SimpleMessage.original(PROVIDER_DETAILS, testSummary, testDescription, List.of(additionalDetail));
        IssueTrackerModelHolder<String> modelHolder = extractor.extractSimpleMessageIssueModels(List.of(simpleMessage), "jobName");
        List<IssueCreationModel> issueCreationModels = modelHolder.getIssueCreationModels();
        assertEquals(1, issueCreationModels.size());
        assertEquals(0, modelHolder.getIssueTransitionModels().size());
        assertEquals(0, modelHolder.getIssueCommentModels().size());

        IssueCreationModel issueCreationModel = issueCreationModels.get(0);
        assertEquals(PROVIDER_DETAILS.getProvider(), issueCreationModel.getProvider());
        assertTrue(issueCreationModel.getTitle().contains(testSummary), "Expected the issue title to contain the simple message's summary");
        assertTrue(issueCreationModel.getDescription().contains(testDescription), "Expected the issue description to contain the simple message's description");
        assertTrue(issueCreationModel.getDescription().contains(additionalDetail.getValue()), "Expected the issue description to contain the simple message's additional detail(s)");
    }

    @Test
    public void extractProjectMessageIssueModelsCreateTest() throws AlertException {
        ProjectMessage projectMessage = Mockito.mock(ProjectMessage.class);
        IssuePolicyDetails policyDetails = new IssuePolicyDetails("A policy", ItemOperation.ADD, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN);
        ProjectIssueModel projectIssueModel = ProjectIssueModel.policy(PROVIDER_DETAILS, PROJECT, PROJECT_VERSION, ISSUE_BOM_COMPONENT_DETAILS, policyDetails);
        ActionableIssueSearchResult<String> searchResult = new ActionableIssueSearchResult<>(null, projectIssueModel, ItemOperation.ADD);

        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();

        IssueTrackerSearcher<String> searcher = Mockito.mock(IssueTrackerSearcher.class);
        Mockito.when(searcher.findIssues(Mockito.eq(projectMessage))).thenReturn(List.of(searchResult));

        IssueTrackerModelExtractor<String> extractor = new IssueTrackerModelExtractor<>(formatter, searcher);

        IssueTrackerModelHolder<String> modelHolder = extractor.extractProjectMessageIssueModels(projectMessage, "jobName");
        List<IssueCreationModel> issueCreationModels = modelHolder.getIssueCreationModels();
        assertEquals(1, issueCreationModels.size());
        assertEquals(0, modelHolder.getIssueTransitionModels().size());
        assertEquals(0, modelHolder.getIssueCommentModels().size());

        IssueCreationModel issueCreationModel = issueCreationModels.get(0);
        assertEquals(projectIssueModel, issueCreationModel.getSource().orElse(null));
    }

    @Test
    public void extractProjectMessageIssueModelsCommentTest() throws AlertException {
        ProjectMessage projectMessage = Mockito.mock(ProjectMessage.class);
        ExistingIssueDetails<String> existingIssueDetails = new ExistingIssueDetails<>("issue-id", "issue-key", "issue summary", "https://ui-link", IssueStatus.UNKNOWN, IssueCategory.POLICY);
        IssuePolicyDetails policyDetails = new IssuePolicyDetails("A policy", ItemOperation.UPDATE, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN);
        ProjectIssueModel projectIssueModel = ProjectIssueModel.policy(PROVIDER_DETAILS, PROJECT, PROJECT_VERSION, ISSUE_BOM_COMPONENT_DETAILS, policyDetails);
        ActionableIssueSearchResult<String> searchResult = new ActionableIssueSearchResult<>(existingIssueDetails, projectIssueModel, ItemOperation.UPDATE);

        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();

        IssueTrackerSearcher<String> searcher = Mockito.mock(IssueTrackerSearcher.class);
        Mockito.when(searcher.findIssues(Mockito.eq(projectMessage))).thenReturn(List.of(searchResult));

        IssueTrackerModelExtractor<String> extractor = new IssueTrackerModelExtractor<>(formatter, searcher);

        IssueTrackerModelHolder<String> modelHolder = extractor.extractProjectMessageIssueModels(projectMessage, "jobName");
        assertEquals(0, modelHolder.getIssueCreationModels().size());
        assertEquals(0, modelHolder.getIssueTransitionModels().size());
        assertEquals(1, modelHolder.getIssueCommentModels().size());
    }

    @Test
    public void extractProjectMessageIssueModelsTransitionTest() throws AlertException {
        ItemOperation itemOperation = ItemOperation.DELETE;
        IssueOperation issueOperation = IssueOperation.RESOLVE;
        ProjectMessage projectMessage = Mockito.mock(ProjectMessage.class);
        ExistingIssueDetails<String> existingIssueDetails = new ExistingIssueDetails<>("issue-id", "issue-key", "issue summary", "https://ui-link", IssueStatus.UNKNOWN, IssueCategory.POLICY);
        IssuePolicyDetails policyDetails = new IssuePolicyDetails("A policy", itemOperation, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN);
        ProjectIssueModel projectIssueModel = ProjectIssueModel.policy(PROVIDER_DETAILS, PROJECT, PROJECT_VERSION, ISSUE_BOM_COMPONENT_DETAILS, policyDetails);
        ActionableIssueSearchResult<String> searchResult = new ActionableIssueSearchResult<>(existingIssueDetails, projectIssueModel, itemOperation);

        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();

        IssueTrackerSearcher<String> searcher = Mockito.mock(IssueTrackerSearcher.class);
        Mockito.when(searcher.findIssues(Mockito.eq(projectMessage))).thenReturn(List.of(searchResult));

        IssueTrackerModelExtractor<String> extractor = new IssueTrackerModelExtractor<>(formatter, searcher);

        IssueTrackerModelHolder<String> modelHolder = extractor.extractProjectMessageIssueModels(projectMessage, "jobName");
        List<IssueTransitionModel<String>> issueTransitionModels = modelHolder.getIssueTransitionModels();
        assertEquals(0, modelHolder.getIssueCreationModels().size());
        assertEquals(1, issueTransitionModels.size());
        assertEquals(0, modelHolder.getIssueCommentModels().size());

        IssueTransitionModel<String> transitionModel = issueTransitionModels.get(0);
        assertEquals(issueOperation, transitionModel.getIssueOperation());
    }

}
