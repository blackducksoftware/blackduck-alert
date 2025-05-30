/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.channel.issue.tracker.convert.mock.MockIssueTrackerMessageFormatter;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueBomComponentDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssuePolicyDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ActionableIssueSearchResult;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueTrackerSearcher;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueCategory;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueStatus;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.extract.model.SimpleMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.AbstractBomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.BomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernSeverity;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentUpgradeGuidance;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentVulnerabilities;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

class IssueTrackerModelExtractorTest {
    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(0L, new LinkableItem("Provider", "Provider Config Name", "https://provider-url"));

    private static final LinkableItem PROJECT = new LinkableItem("Project", "A Black Duck project");
    private static final LinkableItem PROJECT_VERSION = new LinkableItem("Project Version", "a-version");

    private static final LinkableItem COMPONENT = new LinkableItem("Component", "A BOM component");
    private static final LinkableItem COMPONENT_VERSION = new LinkableItem("Component Version", "0.8.7");
    private static final LinkableItem LICENSE = new LinkableItem("License", "A software license", "https://license-url");
    private static final String USAGE = "Some generic usage";
    private static final String ISSUES_URL = "https://issues-url";

    private static final AbstractBomComponentDetails BOM_COMPONENT_DETAILS = new BomComponentDetails(
        COMPONENT,
        COMPONENT_VERSION,
        ComponentVulnerabilities.none(),
        List.of(),
        List.of(),
        LICENSE,
        USAGE,
        ComponentUpgradeGuidance.none(),
        List.of(),
        ISSUES_URL
    );
    private static final IssueBomComponentDetails ISSUE_BOM_COMPONENT_DETAILS = IssueBomComponentDetails.fromBomComponentDetails(BOM_COMPONENT_DETAILS);

    @Test
    void extractSimpleMessageIssueModelsTest() {
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
        assertTrue(
            issueCreationModel.getDescription().contains(additionalDetail.getValue()),
            "Expected the issue description to contain the simple message's additional detail(s)"
        );
    }

    @Test
    void extractProjectMessageIssueModelsCreateTest() throws AlertException {
        ProjectMessage projectMessage = Mockito.mock(ProjectMessage.class);
        IssuePolicyDetails policyDetails = new IssuePolicyDetails("A policy", ItemOperation.ADD, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN);
        ProjectIssueModel projectIssueModel = ProjectIssueModel.policy(PROVIDER_DETAILS, PROJECT, PROJECT_VERSION, ISSUE_BOM_COMPONENT_DETAILS, policyDetails);
        ActionableIssueSearchResult<String> searchResult = new ActionableIssueSearchResult<>(null, projectIssueModel, "", ItemOperation.ADD);

        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();

        IssueTrackerSearcher<String> searcher = Mockito.mock(IssueTrackerSearcher.class);
        Mockito.when(searcher.findIssues(projectMessage)).thenReturn(List.of(searchResult));

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
    void extractProjectMessageIssueModelsCommentTest() throws AlertException {
        ProjectMessage projectMessage = Mockito.mock(ProjectMessage.class);
        ExistingIssueDetails<String> existingIssueDetails = new ExistingIssueDetails<>(
            "issue-id",
            "issue-key",
            "issue summary",
            "https://ui-link",
            IssueStatus.UNKNOWN,
            IssueCategory.POLICY
        );
        IssuePolicyDetails policyDetails = new IssuePolicyDetails("A policy", ItemOperation.UPDATE, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN);
        ProjectIssueModel projectIssueModel = ProjectIssueModel.policy(PROVIDER_DETAILS, PROJECT, PROJECT_VERSION, ISSUE_BOM_COMPONENT_DETAILS, policyDetails);
        ActionableIssueSearchResult<String> searchResult = new ActionableIssueSearchResult<>(existingIssueDetails, projectIssueModel, "", ItemOperation.UPDATE);

        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();

        IssueTrackerSearcher<String> searcher = Mockito.mock(IssueTrackerSearcher.class);
        Mockito.when(searcher.findIssues(projectMessage)).thenReturn(List.of(searchResult));

        IssueTrackerModelExtractor<String> extractor = new IssueTrackerModelExtractor<>(formatter, searcher);

        IssueTrackerModelHolder<String> modelHolder = extractor.extractProjectMessageIssueModels(projectMessage, "jobName");
        assertEquals(0, modelHolder.getIssueCreationModels().size());
        assertEquals(0, modelHolder.getIssueTransitionModels().size());
        assertEquals(1, modelHolder.getIssueCommentModels().size());
    }

    @Test
    void extractProjectMessageIssueModelsTransitionTest() throws AlertException {
        ItemOperation itemOperation = ItemOperation.DELETE;
        IssueOperation issueOperation = IssueOperation.RESOLVE;
        ProjectMessage projectMessage = Mockito.mock(ProjectMessage.class);
        ExistingIssueDetails<String> existingIssueDetails = new ExistingIssueDetails<>(
            "issue-id",
            "issue-key",
            "issue summary",
            "https://ui-link",
            IssueStatus.UNKNOWN,
            IssueCategory.POLICY
        );
        IssuePolicyDetails policyDetails = new IssuePolicyDetails("A policy", itemOperation, ComponentConcernSeverity.UNSPECIFIED_UNKNOWN);
        ProjectIssueModel projectIssueModel = ProjectIssueModel.policy(PROVIDER_DETAILS, PROJECT, PROJECT_VERSION, ISSUE_BOM_COMPONENT_DETAILS, policyDetails);
        ActionableIssueSearchResult<String> searchResult = new ActionableIssueSearchResult<>(existingIssueDetails, projectIssueModel, "", itemOperation);

        MockIssueTrackerMessageFormatter formatter = MockIssueTrackerMessageFormatter.withIntegerMaxValueLength();

        IssueTrackerSearcher<String> searcher = Mockito.mock(IssueTrackerSearcher.class);
        Mockito.when(searcher.findIssues(projectMessage)).thenReturn(List.of(searchResult));

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
