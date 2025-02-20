/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.jira.distribution.JiraIssueCreationRequestCreator;
import com.blackduck.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldResolver;
import com.blackduck.integration.alert.api.channel.jira.distribution.custom.MessageReplacementValues;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.JiraCloudChannelKey;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.channel.jira.cloud.distribution.delegate.JiraCloudIssueCreator;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.cloud.builder.IssueRequestModelFieldsBuilder;
import com.blackduck.integration.jira.common.cloud.model.IssueCreationRequestModel;
import com.blackduck.integration.jira.common.cloud.service.ProjectService;
import com.blackduck.integration.jira.common.model.components.ProjectComponent;
import com.blackduck.integration.jira.common.model.response.PageOfProjectsResponseModel;

public class JiraCloudIssueCreatorTest {

    @Test
    public void verifyIssueSummaryReplacement() throws IntegrationException {
        String testSummary = "testSummary";
        String projectNameOrKey = "FakeProject";
        JiraCloudJobDetailsModel jiraCloudJobDetailsModel = new JiraCloudJobDetailsModel(
            UUID.randomUUID(),
            false,
            "my@email.com",
            projectNameOrKey,
            "Task",
            null,
            null,
            List.of(),
            testSummary
        );

        TestJiraCloudIssueCreator jiraCloudIssueCreator = createTestJiraCloudIssueCreator(projectNameOrKey, jiraCloudJobDetailsModel);
        IssueCreationModel issueCreationModel = createIssueCreationModel();
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder("providerName", "projectName")
                                                                .projectVersionName("projectVersionName")
                                                                .componentName("componentName")
                                                                .componentVersionName("componentVersionName")
                                                                .severity("severity")
                                                                .policyCategory("UNCATEGORIZED")
                                                                .build();
        IssueCreationRequestModel issueCreationRequest = jiraCloudIssueCreator.createIssueCreationRequest(issueCreationModel, messageReplacementValues);
        String summaryText = getSummary(issueCreationRequest);

        assertEquals(testSummary, summaryText);
    }

    @Test
    public void verifySummaryReplacingValues() throws IntegrationException {
        String projectNameOrKey = "FakeProject";
        JiraCloudJobDetailsModel jiraCloudJobDetailsModel = new JiraCloudJobDetailsModel(
            UUID.randomUUID(),
            false,
            "my@email.com",
            projectNameOrKey,
            "Task",
            null,
            null,
            List.of(),
            "testSummary {{providerName}}"
        );
        TestJiraCloudIssueCreator jiraCloudIssueCreator = createTestJiraCloudIssueCreator(projectNameOrKey, jiraCloudJobDetailsModel);

        IssueCreationModel issueCreationModel = createIssueCreationModel();
        String testProviderName = "providerName";
        MessageReplacementValues messageReplacementValues = new MessageReplacementValues.Builder(testProviderName, "projectName")
                                                                .projectVersionName("projectVersionName")
                                                                .componentName("componentName")
                                                                .componentVersionName("componentVersionName")
                                                                .severity("severity")
                                                                .policyCategory("UNCATEGORIZED")
                                                                .build();
        IssueCreationRequestModel issueCreationRequest = jiraCloudIssueCreator.createIssueCreationRequest(issueCreationModel, messageReplacementValues);
        String summaryText = getSummary(issueCreationRequest);

        assertEquals("testSummary " + testProviderName, summaryText);
    }

    private TestJiraCloudIssueCreator createTestJiraCloudIssueCreator(String projectNameOrKey, JiraCloudJobDetailsModel jiraCloudJobDetailsModel) throws IntegrationException {
        ProjectService projectService = Mockito.mock(ProjectService.class);
        Mockito.when(projectService.getProjectsByName(Mockito.anyString())).thenReturn(new PageOfProjectsResponseModel(
            List.of(new ProjectComponent(
                "self",
                "id",
                "key",
                projectNameOrKey,
                null,
                null,
                null,
                null
            ))
        ));
        JiraCustomFieldResolver jiraCustomFieldResolver = new JiraCustomFieldResolver(() -> List.of());
        JiraIssueCreationRequestCreator jiraIssueCreationRequestCreator = new JiraIssueCreationRequestCreator(jiraCustomFieldResolver);
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();

        return new TestJiraCloudIssueCreator(jiraCloudJobDetailsModel, projectService, jiraIssueCreationRequestCreator, issueCategoryRetriever);
    }

    private IssueCreationModel createIssueCreationModel() {
        ProviderDetails providerDetails = new ProviderDetails(1L, null);
        ProjectIssueModel policy = ProjectIssueModel.policy(providerDetails, null, null, null, null);
        return IssueCreationModel.project("title", "description", List.of(), policy, null);
    }

    private String getSummary(IssueCreationRequestModel issueCreationRequest) {
        Map<String, Object> issueFieldMapping = issueCreationRequest.getFieldsBuilder().build();
        Object summary = issueFieldMapping.get(IssueRequestModelFieldsBuilder.SUMMARY);

        return String.valueOf(summary);
    }

    class TestJiraCloudIssueCreator extends JiraCloudIssueCreator {

        public TestJiraCloudIssueCreator(JiraCloudJobDetailsModel jiraCloudJobDetailsModel, ProjectService projectService, JiraIssueCreationRequestCreator jiraIssueCreationRequestCreator, IssueCategoryRetriever issueCategoryRetriever) {
            super(
                new JiraCloudChannelKey(),
                null,
                null,
                jiraCloudJobDetailsModel,
                null,
                projectService,
                jiraIssueCreationRequestCreator,
                null,
                null,
                issueCategoryRetriever,
                null
            );
        }

        @Override
        public IssueCreationRequestModel createIssueCreationRequest(IssueCreationModel issueCreationModel, MessageReplacementValues messageReplacementValues) throws AlertException {
            return super.createIssueCreationRequest(issueCreationModel, messageReplacementValues);
        }
    }
}
