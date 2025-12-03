/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.distribution.delegate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.List;
import java.util.UUID;

import com.blackduck.integration.jira.common.model.request.builder.IssueRequestModelFieldsMapBuilder;
import com.blackduck.integration.jira.common.server.builder.IssueRequestModelFieldsBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.blackduck.integration.alert.api.channel.issue.tracker.callback.IssueTrackerCallbackInfoCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.jira.distribution.JiraErrorMessageUtility;
import com.blackduck.integration.alert.api.channel.jira.distribution.JiraIssueCreationRequestCreator;
import com.blackduck.integration.alert.api.channel.jira.distribution.custom.MessageReplacementValues;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraSearcherResponseModel;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.channel.jira.server.distribution.JiraServerQueryExecutor;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.components.ProjectComponent;
import com.blackduck.integration.jira.common.server.model.IssueCreationRequestModel;
import com.blackduck.integration.jira.common.server.service.IssueService;
import com.blackduck.integration.jira.common.server.service.ProjectService;

@ExtendWith(SpringExtension.class)
public class JiraServerIssueCreatorTest {
    final String TEST_ISSUE_CREATOR_NAME = "user_name01";
    final String TEST_PROJECT_NAME_OR_KEY = "JIRA-Y";
    final String TEST_ISSUE_TYPE = "other";
    final String TEST_ISSUE_SUMMARY = "issueSummary";
    final String TEST_TITLE = "title";
    final JiraServerJobDetailsModel distributionDetails = new JiraServerJobDetailsModel(
        UUID.randomUUID(),
        TEST_ISSUE_CREATOR_NAME,
        TEST_PROJECT_NAME_OR_KEY,
        TEST_ISSUE_TYPE,
        "finished",
        "unfinished",
        List.of(),
        TEST_ISSUE_SUMMARY
    );
    final IssueCreationModel simpleIssueCreationModel = IssueCreationModel.simple(TEST_TITLE, "description", List.of(), new LinkableItem("provider", "test-provider"));


    @Mock
    IssueRequestModelFieldsMapBuilder<IssueRequestModelFieldsBuilder> mockFieldsBuilder;
    @Mock IssueService mockIssueService;
    @Mock ProjectService mockProjectService;
    @Mock JiraIssueCreationRequestCreator mockJiraIssueCreationRequestCreator;
    @Mock JiraServerQueryExecutor mockJiraServerQueryExecutor;
    @Mock IssueCategoryRetriever mockIssueCategoryRetriever;
    @Mock ProjectIssueModel mockProjectIssueModel;

    JiraServerIssueCreator testJiraServerIssueCreator;

    ArgumentCaptor<String> summaryArg; // Captures the summary string passed to JiraIssueCreationRequestCreator.createIssueRequestModel() called within createIssueCreationRequest()
    MessageReplacementValues messageReplacementValues;

    @BeforeEach
    void initEach() throws IntegrationException {
        testJiraServerIssueCreator = new JiraServerIssueCreator(
            ChannelKeys.JIRA_SERVER,
            Mockito.mock(JiraServerIssueCommenter.class),
            Mockito.mock(IssueTrackerCallbackInfoCreator.class),
            distributionDetails,
            mockIssueService,
            mockProjectService,
            mockJiraIssueCreationRequestCreator,
            Mockito.mock(JiraIssueAlertPropertiesManager.class),
            Mockito.mock(JiraErrorMessageUtility.class),
            mockIssueCategoryRetriever,
            mockJiraServerQueryExecutor
        );

        messageReplacementValues = new MessageReplacementValues.Builder("providerName", "projectName").build();
        summaryArg = ArgumentCaptor.forClass(String.class);

        Mockito.when(mockProjectService.getProjectsByName(TEST_PROJECT_NAME_OR_KEY)).thenReturn(List.of(new ProjectComponent("", "JP", "", "jiraProject", null, null, true, "")));
        Mockito.when(mockJiraIssueCreationRequestCreator.createIssueRequestModel(anyString(), anyString(), anyString(), anyString(), any(MessageReplacementValues.class), anyCollection())).thenReturn(mockFieldsBuilder);
    }

    @Test
    void createRequestxUsingIssueSummaryAsTitle() throws AlertException {
        IssueCreationRequestModel testIssueCreationModel = testJiraServerIssueCreator.createIssueCreationRequest(simpleIssueCreationModel, messageReplacementValues);

        Mockito.verify(mockJiraIssueCreationRequestCreator).createIssueRequestModel(summaryArg.capture(), anyString(), anyString(), anyString(), any(MessageReplacementValues.class), anyCollection());
        assertEquals(TEST_ISSUE_SUMMARY, summaryArg.getValue());

        assertAll("Constructed IssueCreationModel matches creator attributes",
            () -> assertEquals(TEST_ISSUE_CREATOR_NAME, testIssueCreationModel.getReporterUsername()),
            () -> assertEquals(TEST_ISSUE_TYPE, testIssueCreationModel.getIssueTypeName()),
            () -> assertEquals(TEST_PROJECT_NAME_OR_KEY, testIssueCreationModel.getProjectName()),
            () -> assertEquals(mockFieldsBuilder, testIssueCreationModel.getFieldsBuilder())
        );
    }

    @Test
    void createRequestReturnsCorrectModelUsingTitle() throws AlertException {
        JiraServerJobDetailsModel distDetailsEmptyIssueSummary = new JiraServerJobDetailsModel(
            UUID.randomUUID(),
            TEST_ISSUE_CREATOR_NAME,
            TEST_PROJECT_NAME_OR_KEY,
            TEST_ISSUE_TYPE,
            "finished",
            "unfinished",
            List.of(),
            ""
        );
        JiraServerIssueCreator testJiraServerIssueCreatorEmptyIssueSummary = new JiraServerIssueCreator(
            ChannelKeys.JIRA_SERVER,
            Mockito.mock(JiraServerIssueCommenter.class),
            Mockito.mock(IssueTrackerCallbackInfoCreator.class),
            distDetailsEmptyIssueSummary,
            mockIssueService,
            mockProjectService,
            mockJiraIssueCreationRequestCreator,
            Mockito.mock(JiraIssueAlertPropertiesManager.class),
            Mockito.mock(JiraErrorMessageUtility.class),
            mockIssueCategoryRetriever,
            mockJiraServerQueryExecutor
        );
        IssueCreationRequestModel testIssueCreationModelEmptyIssueSummary = testJiraServerIssueCreatorEmptyIssueSummary.createIssueCreationRequest(simpleIssueCreationModel, messageReplacementValues);

        Mockito.verify(mockJiraIssueCreationRequestCreator).createIssueRequestModel(summaryArg.capture(), anyString(), anyString(), anyString(), any(MessageReplacementValues.class), anyCollection());
        assertEquals(TEST_TITLE, summaryArg.getValue());

        assertAll("Constructed IssueCreationModel matches creator attributes",
            () -> assertEquals(TEST_ISSUE_CREATOR_NAME, testIssueCreationModelEmptyIssueSummary.getReporterUsername()),
            () -> assertEquals(TEST_ISSUE_TYPE, testIssueCreationModelEmptyIssueSummary.getIssueTypeName()),
            () -> assertEquals(TEST_PROJECT_NAME_OR_KEY, testIssueCreationModelEmptyIssueSummary.getProjectName()),
            () -> assertEquals(mockFieldsBuilder, testIssueCreationModelEmptyIssueSummary.getFieldsBuilder())
        );
    }

    @Test
    void searchForIssueReturnsNonEmptyResponse() throws AlertException {
        List<JiraSearcherResponseModel> expectedResponse = List.of(new JiraSearcherResponseModel("", "", "", ""));
        Mockito.when(mockJiraServerQueryExecutor.executeQuery(anyString())).thenReturn(expectedResponse);

        IssueCreationModel issueCreationModel = IssueCreationModel.project("Test title", null, List.of("example comment"), mockProjectIssueModel, "Query");
        List<JiraSearcherResponseModel> actualResponse = testJiraServerIssueCreator.searchForIssue(issueCreationModel);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void searchForIssueReturnsEmptyResponse() {
        List<JiraSearcherResponseModel> expectedResponse = List.of();

        IssueCreationModel issueCreationModel = IssueCreationModel.project("Test title", null, List.of("example comment"), mockProjectIssueModel, null);
        List<JiraSearcherResponseModel> actualResponse = testJiraServerIssueCreator.searchForIssue(issueCreationModel);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test()
    void searchForIssueReturnsEmptyResponseOnError() throws AlertException {
        List<JiraSearcherResponseModel> expectedResponse = List.of();
        Mockito.when(mockJiraServerQueryExecutor.executeQuery(anyString())).thenThrow(AlertException.class);

        IssueCreationModel issueCreationModel = IssueCreationModel.project("Test title", null, List.of("example comment"), mockProjectIssueModel, "Query");
        List<JiraSearcherResponseModel> actualResponse = testJiraServerIssueCreator.searchForIssue(issueCreationModel);

        // No straightforward way to verify logger.errors were called in the catch block so just verify executeQuery (mocked to throw an error in this test case) is called
        Mockito.verify(mockJiraServerQueryExecutor, Mockito.times(1)).executeQuery(anyString());
        assertEquals(expectedResponse, actualResponse);
    }
}
