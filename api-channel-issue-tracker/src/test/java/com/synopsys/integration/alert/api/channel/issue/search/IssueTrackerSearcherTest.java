package com.synopsys.integration.alert.api.channel.issue.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.channel.issue.convert.ProjectMessageToIssueModelTransformer;
import com.synopsys.integration.alert.api.channel.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueVulnerabilityDetails;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;

public class IssueTrackerSearcherTest {
    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(0L, new LinkableItem("Black Duck", "a-black-duck-config", "https://a-server"));
    private static final LinkableItem PROJECT_ITEM = new LinkableItem("Project", "A Project", "https://a-project-url");
    private static final LinkableItem PROJECT_VERSION_ITEM = new LinkableItem("Project Version", "A Version", "https://a-project-version-url");
    private static final ExistingIssueDetails<String> EXISTING_ISSUE_DETAILS = new ExistingIssueDetails<>("issue-id", "issue-key", "issue summary", "https://issue-link", IssueStatus.RESOLVABLE, IssueCategory.BOM);

    private final ProjectMessageToIssueModelTransformer modelTransformer = new ProjectMessageToIssueModelTransformer();

    @Test
    public void findIssuesProject() throws AlertException {
        ProjectMessage projectMessage = ProjectMessage.projectStatusInfo(PROVIDER_DETAILS, PROJECT_ITEM, ProjectOperation.CREATE);
        IssueTrackerSearcher<String> searcher = new IssueTrackerSearcher<>(null, null, null, null, modelTransformer);
        List<ActionableIssueSearchResult<String>> foundIssues = searcher.findIssues(projectMessage);
        assertEquals(0, foundIssues.size());
    }

    @Test
    public void findIssuesProjectBomDeleted() throws AlertException {
        ProjectMessage projectMessage = ProjectMessage.projectStatusInfo(PROVIDER_DETAILS, PROJECT_ITEM, ProjectOperation.DELETE);
        ProjectIssueModel projectIssueModel = Mockito.mock(ProjectIssueModel.class);
        ProjectIssueSearchResult<String> projectIssueSearchResult = new ProjectIssueSearchResult<>(EXISTING_ISSUE_DETAILS, projectIssueModel);

        ProjectIssueFinder<String> projectIssueFinder = Mockito.mock(ProjectIssueFinder.class);
        Mockito.when(projectIssueFinder.findProjectIssues(Mockito.eq(PROVIDER_DETAILS), Mockito.eq(PROJECT_ITEM))).thenReturn(List.of(projectIssueSearchResult));

        IssueTrackerSearcher<String> searcher = new IssueTrackerSearcher<>(projectIssueFinder, null, null, null, modelTransformer);
        List<ActionableIssueSearchResult<String>> foundIssues = searcher.findIssues(projectMessage);
        assertEquals(1, foundIssues.size());
        assertSearchResult(foundIssues.get(0), projectIssueModel, ItemOperation.DELETE);
    }

    @Test
    public void findIssuesProjectVersion() throws AlertException {
        ProjectMessage projectMessage = ProjectMessage.projectVersionStatusInfo(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, ProjectOperation.DELETE);
        ProjectIssueModel projectIssueModel = Mockito.mock(ProjectIssueModel.class);
        ProjectIssueSearchResult<String> projectIssueSearchResult = new ProjectIssueSearchResult<>(EXISTING_ISSUE_DETAILS, projectIssueModel);

        ProjectVersionIssueFinder<String> projectVersionIssueFinder = Mockito.mock(ProjectVersionIssueFinder.class);
        Mockito.when(projectVersionIssueFinder.findProjectVersionIssues(Mockito.eq(PROVIDER_DETAILS), Mockito.eq(PROJECT_ITEM), Mockito.eq(PROJECT_VERSION_ITEM))).thenReturn(List.of(projectIssueSearchResult));

        IssueTrackerSearcher<String> searcher = new IssueTrackerSearcher<>(null, projectVersionIssueFinder, null, null, modelTransformer);
        List<ActionableIssueSearchResult<String>> foundIssues = searcher.findIssues(projectMessage);
        assertEquals(1, foundIssues.size());
        assertSearchResult(foundIssues.get(0), projectIssueModel, ItemOperation.DELETE);
    }

    @Test
    public void findIssuesComponentUpdate() throws AlertException {
        BomComponentDetails bomComponentDetails = Mockito.mock(BomComponentDetails.class);
        ProjectMessage projectMessage = ProjectMessage.componentUpdate(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, List.of(bomComponentDetails));
        ProjectIssueModel projectIssueModel = Mockito.mock(ProjectIssueModel.class);
        ProjectIssueSearchResult<String> projectIssueSearchResult = new ProjectIssueSearchResult<>(EXISTING_ISSUE_DETAILS, projectIssueModel);

        ProjectVersionComponentIssueFinder<String> componentIssueFinder = Mockito.mock(ProjectVersionComponentIssueFinder.class);
        Mockito.when(componentIssueFinder.findIssuesByComponent(Mockito.eq(PROVIDER_DETAILS), Mockito.eq(PROJECT_ITEM), Mockito.eq(PROJECT_VERSION_ITEM), Mockito.eq(bomComponentDetails))).thenReturn(List.of(projectIssueSearchResult));

        IssueTrackerSearcher<String> searcher = new IssueTrackerSearcher<>(null, null, componentIssueFinder, null, modelTransformer);
        List<ActionableIssueSearchResult<String>> foundIssues = searcher.findIssues(projectMessage);
        assertEquals(1, foundIssues.size());
        assertSearchResult(foundIssues.get(0), projectIssueModel, ItemOperation.UPDATE);
    }

    @Test
    public void findIssuesProjectIssueModel() throws AlertException {
        BomComponentDetails bomComponentDetails = Mockito.mock(BomComponentDetails.class);
        ProjectMessage projectMessage = ProjectMessage.componentConcern(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, List.of(bomComponentDetails));

        ProjectIssueModel projectIssueModel1 = Mockito.mock(ProjectIssueModel.class);
        ExistingIssueDetails<String> issue1 = new ExistingIssueDetails<>("issue-1", "issue-1", "issue 1", "https://issue-1", IssueStatus.RESOLVABLE, IssueCategory.POLICY);

        IssuePolicyDetails policyDetails = new IssuePolicyDetails("A policy", ItemOperation.DELETE, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN);
        ProjectIssueModel projectIssueModel2 = Mockito.mock(ProjectIssueModel.class);
        Mockito.when(projectIssueModel2.getPolicyDetails()).thenReturn(Optional.of(policyDetails));

        IssueVulnerabilityDetails vulnerabilityDetails = new IssueVulnerabilityDetails(true, List.of(), List.of(), List.of());
        ProjectIssueModel projectIssueModel3 = Mockito.mock(ProjectIssueModel.class);
        Mockito.when(projectIssueModel3.getVulnerabilityDetails()).thenReturn(Optional.of(vulnerabilityDetails));

        ExactIssueFinder<String> exactIssueFinder = Mockito.mock(ExactIssueFinder.class);
        Mockito.when(exactIssueFinder.findExistingIssuesByProjectIssueModel(projectIssueModel1)).thenReturn(List.of(issue1));
        Mockito.when(exactIssueFinder.findExistingIssuesByProjectIssueModel(projectIssueModel2)).thenReturn(List.of());

        ProjectMessageToIssueModelTransformer mockModelTransformer = Mockito.mock(ProjectMessageToIssueModelTransformer.class);
        Mockito.when(mockModelTransformer.convertToIssueModels(Mockito.eq(projectMessage))).thenReturn(List.of(projectIssueModel1, projectIssueModel2, projectIssueModel3));

        IssueTrackerSearcher<String> searcher = new IssueTrackerSearcher<>(null, null, null, exactIssueFinder, mockModelTransformer);
        List<ActionableIssueSearchResult<String>> foundIssues = searcher.findIssues(projectMessage);
        assertEquals(1, foundIssues.size());
        ActionableIssueSearchResult<String> foundIssue = foundIssues.get(0);
        assertEquals(issue1, foundIssue.getExistingIssueDetails().orElse(null));
    }

    private static void assertSearchResult(ActionableIssueSearchResult<String> issueSearchResult, ProjectIssueModel expectedProjectIssue, ItemOperation expectedOp) {
        assertEquals(expectedProjectIssue, issueSearchResult.getProjectIssueModel());
        assertEquals(EXISTING_ISSUE_DETAILS, issueSearchResult.getExistingIssueDetails().orElse(null));
        assertEquals(expectedOp, issueSearchResult.getRequiredOperation());
    }

}
