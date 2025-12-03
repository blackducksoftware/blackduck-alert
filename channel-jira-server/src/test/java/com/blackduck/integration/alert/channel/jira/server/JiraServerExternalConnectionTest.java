/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.callback.IssueTrackerCallbackInfoCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.convert.ProjectMessageToIssueModelTransformer;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.jira.distribution.JiraMessageFormatter;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderMessageHolder;
import com.blackduck.integration.alert.api.processor.extract.model.SimpleMessage;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.jira.server.distribution.JiraServerMessageSenderFactory;
import com.blackduck.integration.alert.channel.jira.server.distribution.JiraServerProcessorFactory;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.common.persistence.accessor.JobAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModelBuilder;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.test.common.TestPropertyKey;
import com.blackduck.integration.alert.test.common.TestTags;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;

@Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
class JiraServerExternalConnectionTest {
    private final TestProperties testProperties = new TestProperties();

    //This test is @Disabled since it requires a running Jira Server instance. In order to run this test, you must deploy a Jira Server and
    // add the Jira Server environment values into test.properties
    @Test
    @Disabled
    void sendJiraServerMessageTest() throws AlertException {
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        JiraMessageFormatter jiraMessageFormatter = new JiraMessageFormatter();

        JiraServerChannelKey jiraServerChannelKey = new JiraServerChannelKey();
        EventManager eventManager = Mockito.mock(EventManager.class);
        ExecutingJobManager executingJobManager = Mockito.mock(ExecutingJobManager.class);
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.of(createJiraServerConfigModel()));
        ProxyManager proxyManager = Mockito.mock(ProxyManager.class);
        Mockito.when(proxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(null);
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(createDistributionJobModel()));
        JiraServerPropertiesFactory jiraServerPropertiesFactory = new JiraServerPropertiesFactory(proxyManager, jiraServerGlobalConfigAccessor, jobAccessor, null);

        IssueTrackerCallbackInfoCreator issueTrackerCallbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        JiraServerMessageSenderFactory jiraServerMessageSenderFactory = new JiraServerMessageSenderFactory(
            gson,
            jiraServerChannelKey,
            jiraServerPropertiesFactory,
            issueTrackerCallbackInfoCreator,
            issueCategoryRetriever,
            eventManager,
            executingJobManager
        );

        ProjectMessageToIssueModelTransformer modelTransformer = new ProjectMessageToIssueModelTransformer();
        JiraServerProcessorFactory jiraServerProcessorFactory = new JiraServerProcessorFactory(
            gson,
            jiraMessageFormatter,
            jiraServerPropertiesFactory,
            jiraServerMessageSenderFactory,
            modelTransformer,
            issueCategoryRetriever
        );
        IssueTrackerProcessor<String> processor = jiraServerProcessorFactory.createProcessor(createDistributionDetails(), UUID.randomUUID(), Set.of());

        IssueTrackerResponse<String> response = processor.processMessages(createMessage(), "jobName");

        assertEquals("Success", response.getStatusMessage());
    }

    private ProviderMessageHolder createMessage() {
        ProviderDetails providerDetails = new ProviderDetails(1L, new LinkableItem("ProviderLabel", "ProviderName"));
        SimpleMessage simpleMessage = SimpleMessage.original(providerDetails, "Test Summary", "Test Description", List.of());

        return new ProviderMessageHolder(List.of(), List.of(simpleMessage));
    }

    private JiraServerGlobalConfigModel createJiraServerConfigModel() {
        // TODO: Implement access token and AuthorizationMethod
        JiraServerGlobalConfigModel configModel = new JiraServerGlobalConfigModel(
            UUID.randomUUID().toString(),
            "name",
            testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_URL),
            Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_TIMEOUT)),
            JiraServerAuthorizationMethod.BASIC
        );
        configModel.setUserName(testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_USERNAME));
        configModel.setPassword(testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_PASSWORD));
        return configModel;
    }

    private DistributionJobModel createDistributionJobModel() {
        return new DistributionJobModelBuilder()
            .channelGlobalConfigId(UUID.randomUUID())
            .name("name")
            .distributionFrequency(FrequencyType.REAL_TIME)
            .processingType(ProcessingType.DEFAULT)
            .channelDescriptorName("channelName")
            .createdAt(OffsetDateTime.now())
            .blackDuckGlobalConfigId(1L)
            .notificationTypes(List.of("POLICY_OVERRIDE"))
            .build();
    }

    private JiraServerJobDetailsModel createDistributionDetails() {
        UUID uuid = UUID.randomUUID();
        List<JiraJobCustomFieldModel> customFields = new ArrayList<>();
        //This test requires that the JIRA server has 2 components associated with the project: "component1" and "component2"
        customFields.add(new JiraJobCustomFieldModel("Component/s", "component1 component2"));

        return new JiraServerJobDetailsModel(
            uuid,
            testProperties.getOptionalProperty(TestPropertyKey.TEST_JIRA_SERVER_ISSUE_CREATOR).orElse(null),
            testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_PROJECT_NAME),
            testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_ISSUE_TYPE),
            testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_RESOLVE_TRANSITION),
            testProperties.getProperty(TestPropertyKey.TEST_JIRA_SERVER_REOPEN_TRANSITION),
            customFields,
            ""
        );
    }

}
