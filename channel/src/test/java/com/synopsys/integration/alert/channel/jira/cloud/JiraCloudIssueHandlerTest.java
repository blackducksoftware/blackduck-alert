package com.synopsys.integration.alert.channel.jira.cloud;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.cloud.util.JiraCloudIssueHandler;
import com.synopsys.integration.alert.channel.jira.cloud.util.JiraCloudIssuePropertyHandler;
import com.synopsys.integration.alert.channel.jira.cloud.util.JiraCloudTransitionHandler;
import com.synopsys.integration.alert.channel.jira.common.JiraIssueSearchProperties;
import com.synopsys.integration.alert.channel.jira.common.JiraTestConfigHelper;
import com.synopsys.integration.alert.channel.jira.common.model.JiraIssueConfig;
import com.synopsys.integration.alert.channel.jira.common.util.JiraContentValidator;
import com.synopsys.integration.alert.channel.jira.common.util.JiraErrorMessageUtility;
import com.synopsys.integration.alert.channel.jira2.common.JiraCustomFieldResolver;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueContentModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.IssueSearchService;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;

public class JiraCloudIssueHandlerTest {
    private final Gson gson = new Gson();

    @Disabled
    @Test
    public void testCreateIssueIT() throws Exception {
        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.TRACE);
        JiraTestConfigHelper jiraTestConfigHelper = new JiraTestConfigHelper();
        jiraTestConfigHelper.validateConfiguration();

        String taskType = "Task";
        String email = jiraTestConfigHelper.getEnvUserEmail();

        JiraCloudServiceFactory serviceFactory = jiraTestConfigHelper.createServiceFactory();
        IssueService issueService = serviceFactory.createIssueService();
        IssueSearchService issueSearchService = serviceFactory.createIssueSearchService();
        IssuePropertyService issuePropertyService = serviceFactory.createIssuePropertyService();

        JiraContentValidator contentValidator = new JiraContentValidator();
        JiraCloudTransitionHandler jiraTransitionHandler = new JiraCloudTransitionHandler(issueService);
        JiraCloudIssuePropertyHandler jiraIssuePropertyHandler = new JiraCloudIssuePropertyHandler(issueSearchService, issuePropertyService);
        JiraCustomFieldResolver customFieldResolver = Mockito.mock(JiraCustomFieldResolver.class);
        JiraErrorMessageUtility jiraErrorMessageUtility = new JiraErrorMessageUtility(gson);
        MockJiraIssueHandler jiraCloudIssueHandler = new MockJiraIssueHandler(issueService, jiraTestConfigHelper.createJiraCloudProperties(), jiraErrorMessageUtility, jiraTransitionHandler, jiraIssuePropertyHandler, contentValidator,
            customFieldResolver);

        JiraIssueConfig issueConfig = new JiraIssueConfig(
            jiraTestConfigHelper.getTestProject(),
            null,
            null,
            email,
            taskType,
            true,
            null,
            null,
            List.of()
        );

        JiraIssueSearchProperties jiraIssueSearchProperties = new JiraIssueSearchProperties("Provider", "Provider URL", "Topic", "Topic Value",
            "Sub Topic", "Sub Topic Value", "Category", "Component Name", "Component Value", "Sub Component", "Sub Component Value", "");
        IssueContentModel issueContentModel = IssueContentModel.of("Test issue", "Attempting to create an issue to reproduce a bug", new ArrayList<>());
        AlertIssueOrigin alertIssueOrigin = new AlertIssueOrigin(null, null);
        IssueTrackerRequest request = new IssueTrackerRequest(IssueOperation.OPEN, jiraIssueSearchProperties, issueContentModel, alertIssueOrigin);

        Optional<IssueResponseModel> issueResponseModel = jiraCloudIssueHandler.testCreateIssue(issueConfig, request);
        assertTrue(issueResponseModel.isPresent());
        String issueId = issueResponseModel.map(IssueResponseModel::getId).orElse("");
        issueService.deleteIssue(issueId);

        logger.alwaysLog("Done");
    }

    private static class MockJiraIssueHandler extends JiraCloudIssueHandler {
        public MockJiraIssueHandler(IssueService issueService, JiraCloudProperties jiraProperties, JiraErrorMessageUtility jiraErrorMessageUtility, JiraCloudTransitionHandler jiraTransitionHandler,
            JiraCloudIssuePropertyHandler jiraIssuePropertyHandler, JiraContentValidator jiraContentValidator, JiraCustomFieldResolver jiraCustomFieldResolver) {
            super(issueService, jiraProperties, jiraErrorMessageUtility, jiraTransitionHandler, jiraIssuePropertyHandler, jiraContentValidator, jiraCustomFieldResolver);
        }

        public Optional<IssueResponseModel> testCreateIssue(IssueConfig issueConfig, IssueTrackerRequest request) throws IntegrationException {
            return createIssue(issueConfig, request);
        }

    }

}
