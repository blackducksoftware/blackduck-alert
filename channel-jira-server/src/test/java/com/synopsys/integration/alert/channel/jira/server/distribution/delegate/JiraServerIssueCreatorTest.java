package com.synopsys.integration.alert.channel.jira.server.distribution.delegate;

import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraErrorMessageUtility;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraIssueCreationRequestCreator;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.MessageReplacementValues;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraSearcherResponseModel;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.jira.server.distribution.JiraServerQueryExecutor;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.builder.IssueRequestModelFieldsBuilder;
import com.synopsys.integration.jira.common.model.components.ProjectComponent;
import com.synopsys.integration.jira.common.server.model.IssueCreationRequestModel;
import com.synopsys.integration.jira.common.server.service.IssueService;
import com.synopsys.integration.jira.common.server.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(SpringExtension.class)
public class JiraServerIssueCreatorTest {
    final String TEST_ISSUE_CREATOR_NAME = "user_name01";
    final String TEST_PROJECT_NAME_OR_KEY = "JIRA-Y";
    final String TEST_ISSUE_TYPE = "other";
    final String TEST_ISSUE_SUMMARY = "issueSummary";
    final String TEST_TITLE = "title";
    final JiraServerJobDetailsModel distributionDetails = new JiraServerJobDetailsModel(
        UUID.randomUUID(),
        true,
        TEST_ISSUE_CREATOR_NAME,
        TEST_PROJECT_NAME_OR_KEY,
        TEST_ISSUE_TYPE,
        "finished",
        "unfinished",
        List.of(),
        TEST_ISSUE_SUMMARY
    );
    final IssueCreationModel simpleIssueCreationModel = IssueCreationModel.simple(TEST_TITLE, "description", List.of(), new LinkableItem("provider", "test-provider"));


    @Mock IssueRequestModelFieldsBuilder mockFieldsBuilder;
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
            true,
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
