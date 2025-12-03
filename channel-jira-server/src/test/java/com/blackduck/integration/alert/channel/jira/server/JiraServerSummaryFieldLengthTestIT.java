/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

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
import com.blackduck.integration.alert.channel.jira.server.distribution.JiraServerMessageSenderFactory;
import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
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
public class JiraServerSummaryFieldLengthTestIT {
    private static final LinkableItem PROVIDER = new LinkableItem("Black Duck", "a hub server", "https://a-hub-server");
    private static final String TEST_NAME = JiraServerSummaryFieldLengthTestIT.class.getSimpleName();
    private static final Integer TEST_JIRA_TIMEOUT_SECONDS = 300;

    private static IssueTrackerMessageSender<String> jiraServerMessageSender;

    @BeforeAll
    public static void init() throws AlertException {
        jiraServerMessageSender = createJiraServerMessageSender();
    }

    @Test
    void summaryLength254SucceedsTest() {
        IssueCreationModel issueCreationModel = createIssueCreationModel(254);
        IssueTrackerModelHolder<String> messages = new IssueTrackerModelHolder<>(List.of(issueCreationModel), List.of(), List.of());
        assertDoesNotThrow(() -> jiraServerMessageSender.sendMessages(messages));
    }

    @Test
    void summaryLength256FailsTest() {
        IssueCreationModel issueCreationModel = createIssueCreationModel(256);
        IssueTrackerModelHolder<String> messages = new IssueTrackerModelHolder<>(List.of(issueCreationModel), List.of(), List.of());
        Throwable exception = assertThrows(AlertException.class, () -> jiraServerMessageSender.sendMessages(messages));
        assertTrue(exception.getMessage().contains("Summary must be less than 255 characters"));
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

    private static IssueTrackerMessageSender<String> createJiraServerMessageSender() throws AlertException {
        TestProperties testProperties = new TestProperties();
        Gson gson = new GsonBuilder().create();
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        JiraServerPropertiesFactory jiraServerPropertiesFactory = createJiraServerPropertiesFactory(testProperties);
        EventManager eventManager = Mockito.mock(EventManager.class);
        ExecutingJobManager executingJobManager = Mockito.mock(ExecutingJobManager.class);

        JiraServerMessageSenderFactory jiraServerMessageSenderFactory = new JiraServerMessageSenderFactory(
            gson,
            ChannelKeys.JIRA_SERVER,
            jiraServerPropertiesFactory,
            new IssueTrackerCallbackInfoCreator(),
            issueCategoryRetriever,
            eventManager,
            executingJobManager
        );

        JiraServerJobDetailsModel jiraServerJobDetails = createJiraServerJobDetails(testProperties);
        return jiraServerMessageSenderFactory.createMessageSender(jiraServerJobDetails, UUID.randomUUID());
    }

    private static JiraServerPropertiesFactory createJiraServerPropertiesFactory(TestProperties testProperties) throws AlertConfigurationException {
        String url = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_URL);
        String username = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_USERNAME);
        String password = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_PASSWORD);

        JiraServerPropertiesFactory jiraServerPropertiesFactory = Mockito.mock(JiraServerPropertiesFactory.class);
        JiraServerProperties jiraServerProperties = new JiraServerProperties(
            url,
            TEST_JIRA_TIMEOUT_SECONDS,
            JiraServerAuthorizationMethod.BASIC,
            password,
            username,
            null,
            true,
            ProxyInfo.NO_PROXY_INFO,
            null
        );
        Mockito.when(jiraServerPropertiesFactory.createJiraProperties(Mockito.any(UUID.class))).thenReturn(jiraServerProperties);

        return jiraServerPropertiesFactory;
    }

    private static JiraServerJobDetailsModel createJiraServerJobDetails(TestProperties testProperties) {
        String issueCreator = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_ISSUE_CREATOR).orElse(null);
        String projectName = testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_PROJECT_NAME);
        String issueType = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_ISSUE_TYPE).orElse("Task");
        String resolveTransition = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_RESOLVE_TRANSITION).orElse(null);
        String reopenTransition = testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_REOPEN_TRANSITION).orElse(null);

        return new JiraServerJobDetailsModel(
            UUID.randomUUID(),
            issueCreator,
            projectName,
            issueType,
            resolveTransition,
            reopenTransition,
            List.of(),
            ""
        );
    }

}
