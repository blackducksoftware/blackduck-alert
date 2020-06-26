package com.synopsys.integration.issuetracker.jira.cloud;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.issuetracker.message.IssueContentModel;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.issuetracker.jira.JiraTestConfigHelper;
import com.synopsys.integration.issuetracker.jira.cloud.util.JiraCloudIssueHandler;
import com.synopsys.integration.issuetracker.jira.cloud.util.JiraCloudIssuePropertyHandler;
import com.synopsys.integration.issuetracker.jira.cloud.util.JiraCloudTransitionHandler;
import com.synopsys.integration.issuetracker.jira.common.JiraIssueSearchProperties;
import com.synopsys.integration.issuetracker.jira.common.util.JiraContentValidator;
import com.synopsys.integration.jira.common.cloud.service.IssueSearchService;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;

public class JiraCloudIssueHandlerTest {
    private Gson gson = new Gson();

    @Ignore
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
        TestJiraIssueHandler jiraCloudIssueHandler = new TestJiraIssueHandler(issueService, jiraTestConfigHelper.createJiraCloudProperties(), gson, jiraTransitionHandler, jiraIssuePropertyHandler, contentValidator);

        IssueConfig issueConfig = new IssueConfig();
        issueConfig.setProjectName(jiraTestConfigHelper.getTestProject());
        issueConfig.setIssueCreator(email);
        issueConfig.setIssueType(taskType);

        JiraIssueSearchProperties jiraIssueSearchProperties = new JiraIssueSearchProperties("Provider", "Provider Url", "Topic", "Topic Value",
            "Sub Topic", "Sub Topic Value", "Category", "Component Name", "Component Value", "Sub Component", "Sub Component Value", "");
        IssueContentModel issueContentModel = IssueContentModel.of("Test issue", "Attempting to create an issue to reproduce a bug", new ArrayList<>());
        IssueTrackerRequest request = new IssueTrackerRequest(IssueOperation.OPEN, jiraIssueSearchProperties, issueContentModel);

        Optional<IssueResponseModel> issueResponseModel = jiraCloudIssueHandler.testCreateIssue(issueConfig, request);
        assertTrue(issueResponseModel.isPresent());
        String issueId = issueResponseModel.map(IssueResponseModel::getId).orElse("");
        issueService.deleteIssue(issueId);

        logger.alwaysLog("Done");
    }

    private class TestJiraIssueHandler extends JiraCloudIssueHandler {

        public TestJiraIssueHandler(IssueService issueService, JiraCloudProperties jiraProperties, Gson gson, JiraCloudTransitionHandler jiraTransitionHandler,
            JiraCloudIssuePropertyHandler jiraIssuePropertyHandler, JiraContentValidator jiraContentValidator) {
            super(issueService, jiraProperties, gson, jiraTransitionHandler, jiraIssuePropertyHandler, jiraContentValidator);
        }

        public Optional<IssueResponseModel> testCreateIssue(IssueConfig issueConfig, IssueTrackerRequest request) throws IntegrationException {
            return createIssue(issueConfig, request);
        }

    }
}
