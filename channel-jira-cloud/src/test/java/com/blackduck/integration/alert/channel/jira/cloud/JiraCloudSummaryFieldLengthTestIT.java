/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import com.blackduck.integration.jira.common.cloud.configuration.JiraCloudRestConfigBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.channel.issue.tracker.callback.IssueTrackerCallbackInfoCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerMessageSender;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.channel.jira.cloud.distribution.JiraCloudMessageSenderFactory;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.test.common.TestPropertyKey;
import com.blackduck.integration.alert.test.common.TestTags;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Tags(value = {
    @Tag(TestTags.DEFAULT_INTEGRATION),
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
})
public class JiraCloudSummaryFieldLengthTestIT {
    private static final LinkableItem PROVIDER = new LinkableItem("Black Duck", "a hub server", "https://a-hub-server");
    private static final String TEST_NAME = JiraCloudSummaryFieldLengthTestIT.class.getSimpleName();

    private static IssueTrackerMessageSender<String> jiraCloudMessageSender;

    @BeforeAll
    public static void init() throws AlertException {
        jiraCloudMessageSender = createJiraCloudMessageSender();
    }

    @Test
    void summaryLength254SucceedsTest() {
        IssueCreationModel issueCreationModel = createIssueCreationModel(254);
        IssueTrackerModelHolder<String> messages = new IssueTrackerModelHolder<>(List.of(issueCreationModel), List.of(), List.of());
        assertDoesNotThrow(() -> jiraCloudMessageSender.sendMessages(messages));
    }

    @Test
    void summaryLength256FailsTest() {
        IssueCreationModel issueCreationModel = createIssueCreationModel(256);
        IssueTrackerModelHolder<String> messages = new IssueTrackerModelHolder<>(List.of(issueCreationModel), List.of(), List.of());
        Throwable exception = assertThrows(AlertException.class, () -> jiraCloudMessageSender.sendMessages(messages));
        assertTrue(exception.getMessage().contains("Summary can't exceed 255 characters"));
    }

    private IssueCreationModel createIssueCreationModel(int summaryLength) {
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < summaryLength; i++) {
            if (i % 2 == 0) {
                summary.append("0");
            } else {
                summary.append("1");
            }
        }
        return IssueCreationModel.simple(summary.toString(), TEST_NAME, List.of(), PROVIDER);
    }

    private static IssueTrackerMessageSender<String> createJiraCloudMessageSender() throws AlertException {
        TestProperties testProperties = new TestProperties();
        Gson gson = new GsonBuilder().create();
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        JiraCloudPropertiesFactory jiraCloudPropertiesFactory = createJiraCloudPropertiesFactory(testProperties);
        EventManager eventManager = Mockito.mock(EventManager.class);
        ExecutingJobManager executingJobManager = Mockito.mock(ExecutingJobManager.class);

        JiraCloudMessageSenderFactory jiraCloudMessageSenderFactory = new JiraCloudMessageSenderFactory(
            gson,
            ChannelKeys.JIRA_CLOUD,
            jiraCloudPropertiesFactory,
            new IssueTrackerCallbackInfoCreator(),
            issueCategoryRetriever,
            eventManager,
            executingJobManager
        );

        JiraCloudJobDetailsModel jiraCloudJobDetails = createJiraCloudJobDetails(testProperties);
        return jiraCloudMessageSenderFactory.createMessageSender(jiraCloudJobDetails, null);
    }

    private static JiraCloudPropertiesFactory createJiraCloudPropertiesFactory(TestProperties testProperties) throws AlertConfigurationException {
        String url = testProperties.getProperty(TestPropertyKey.TEST_JIRA_CLOUD_URL);
        String apiToken = testProperties.getProperty(TestPropertyKey.TEST_JIRA_CLOUD_API_TOKEN);
        String userEmail = testProperties.getProperty(TestPropertyKey.TEST_JIRA_CLOUD_USER_EMAIL);

        JiraCloudPropertiesFactory jiraCloudPropertiesFactory = Mockito.mock(JiraCloudPropertiesFactory.class);
        JiraCloudProperties jiraCloudProperties = new JiraCloudProperties(url, apiToken, userEmail, true, ProxyInfo.NO_PROXY_INFO, null,  JiraCloudRestConfigBuilder.DEFAULT_TIMEOUT_SECONDS);
        Mockito.when(jiraCloudPropertiesFactory.createJiraProperties()).thenReturn(jiraCloudProperties);

        return jiraCloudPropertiesFactory;
    }

    private static JiraCloudJobDetailsModel createJiraCloudJobDetails(TestProperties testProperties) {
        String issueCreator = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_CLOUD_ISSUE_CREATOR).orElse(null);
        String projectName = testProperties.getProperty(TestPropertyKey.TEST_JIRA_CLOUD_PROJECT_NAME);
        String issueType = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_CLOUD_ISSUE_TYPE).orElse("Task");
        String resolveTransition = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_CLOUD_RESOLVE_TRANSITION).orElse(null);
        String reopenTransition = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_CLOUD_REOPEN_TRANSITION).orElse(null);

        return new JiraCloudJobDetailsModel(
            UUID.randomUUID(),
            issueCreator,
            projectName,
            issueType,
            resolveTransition,
            reopenTransition,
            List.of(),
            null
        );
    }

}
