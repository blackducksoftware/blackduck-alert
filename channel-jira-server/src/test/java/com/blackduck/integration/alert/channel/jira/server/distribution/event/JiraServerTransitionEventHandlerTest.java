/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.distribution.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerResponsePostProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.callback.IssueTrackerCallbackInfoCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.callback.ProviderCallbackIssueTrackerResponsePostProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerTransitionIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTransitionModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.ExistingIssueDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueCategory;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueStatus;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.channel.jira.server.JiraServerProperties;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.DefaultJiraServerJobDetailsAccessor;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.mock.MockJiraServerJobCustomFieldRepository;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.mock.MockJiraServerJobDetailsRepository;
import com.blackduck.integration.alert.channel.jira.server.distribution.JiraServerMessageSenderFactory;
import com.blackduck.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.components.StatusCategory;
import com.blackduck.integration.jira.common.model.components.StatusDetailsComponent;
import com.blackduck.integration.jira.common.model.components.TransitionComponent;
import com.blackduck.integration.jira.common.model.request.IssueRequestModel;
import com.blackduck.integration.jira.common.model.response.TransitionsResponseModel;
import com.blackduck.integration.jira.common.server.service.FieldService;
import com.blackduck.integration.jira.common.server.service.IssueSearchService;
import com.blackduck.integration.jira.common.server.service.IssueService;
import com.blackduck.integration.jira.common.server.service.JiraServerServiceFactory;
import com.blackduck.integration.jira.common.server.service.ProjectService;
import com.google.gson.Gson;

class JiraServerTransitionEventHandlerTest {

    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private AtomicInteger issueCounter;
    private EventManager eventManager;
    private IssueTrackerResponsePostProcessor responsePostProcessor;
    private DefaultJiraServerJobDetailsAccessor jobDetailsAccessor;
    private ExecutingJobManager executingJobManager;

    @BeforeEach
    public void init() {
        issueCounter = new AtomicInteger(0);
        eventManager = Mockito.mock(EventManager.class);
        responsePostProcessor = new ProviderCallbackIssueTrackerResponsePostProcessor(eventManager);

        MockJiraServerJobDetailsRepository jiraServerJobDetailsRepository = new MockJiraServerJobDetailsRepository();
        MockJiraServerJobCustomFieldRepository jiraServerJobCustomFieldRepository = new MockJiraServerJobCustomFieldRepository();
        jobDetailsAccessor = new DefaultJiraServerJobDetailsAccessor(
            ChannelKeys.JIRA_SERVER,
            jiraServerJobDetailsRepository,
            jiraServerJobCustomFieldRepository
        );

        executingJobManager = Mockito.mock(ExecutingJobManager.class);
    }

    @Test
    void handleUnknownJobTest() {
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);

        JiraServerPropertiesFactory propertiesFactory = Mockito.mock(JiraServerPropertiesFactory.class);
        IssueTrackerCallbackInfoCreator callbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        JiraServerMessageSenderFactory messageSenderFactory = new JiraServerMessageSenderFactory(
            gson,
            ChannelKeys.JIRA_SERVER,
            propertiesFactory,
            callbackInfoCreator,
            issueCategoryRetriever,
            eventManager,
            executingJobManager
        );

        JiraServerTransitionEventHandler handler = new JiraServerTransitionEventHandler(
            eventManager,
            gson,
            propertiesFactory,
            messageSenderFactory,
            jobDetailsAccessor,
            responsePostProcessor,
            executingJobManager
        );
        ExistingIssueDetails<String> existingIssueDetails = new ExistingIssueDetails<>("id", "key", "summary", "link", IssueStatus.UNKNOWN, IssueCategory.BOM);
        IssueTransitionModel<String> model = new IssueTransitionModel<>(existingIssueDetails, IssueOperation.RESOLVE, List.of(), null);
        JiraServerTransitionEvent event = new JiraServerTransitionEvent(
            IssueTrackerTransitionIssueEvent.createDefaultEventDestination(ChannelKeys.JIRA_SERVER),
            jobExecutionId,
            jobId,
            notificationIds,
            model
        );
        handler.handle(event);
        assertEquals(0, issueCounter.get());
    }

    @Test
    void handleTransitionTest() throws IntegrationException {
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);

        JiraServerJobDetailsModel jobDetailsModel = createJobDetails(jobId);

        JiraServerPropertiesFactory propertiesFactory = Mockito.mock(JiraServerPropertiesFactory.class);
        JiraServerProperties jiraProperties = Mockito.mock(JiraServerProperties.class);
        JiraServerServiceFactory jiraServiceFactory = Mockito.mock(JiraServerServiceFactory.class);
        IssueService issueService = Mockito.mock(IssueService.class);
        IssueSearchService issueSearchService = Mockito.mock(IssueSearchService.class);
        FieldService fieldService = Mockito.mock(FieldService.class);
        ProjectService projectService = Mockito.mock(ProjectService.class);

        Mockito.doAnswer(invocation -> {
            issueCounter.incrementAndGet();
            return null;
        }).when(issueService).transitionIssue(Mockito.any(IssueRequestModel.class));
        Mockito.when(issueService.getStatus(Mockito.anyString())).thenReturn(createStatusDetailsComponent());
        Mockito.when(issueService.getTransitions(Mockito.anyString())).thenReturn(new TransitionsResponseModel("", List.of(createTransition())));

        Mockito.when(jiraServiceFactory.createFieldService()).thenReturn(fieldService);
        Mockito.when(jiraServiceFactory.createIssueService()).thenReturn(issueService);
        Mockito.when(jiraServiceFactory.createIssueSearchService()).thenReturn(issueSearchService);
        Mockito.when(jiraServiceFactory.createProjectService()).thenReturn(projectService);
        Mockito.when(jiraProperties.createJiraServicesServerFactory(Mockito.any(), Mockito.any())).thenReturn(jiraServiceFactory);
        Mockito.when(propertiesFactory.createJiraPropertiesWithJobId(jobId)).thenReturn(jiraProperties);

        IssueTrackerCallbackInfoCreator callbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        JiraServerMessageSenderFactory messageSenderFactory = new JiraServerMessageSenderFactory(
            gson,
            ChannelKeys.JIRA_SERVER,
            propertiesFactory,
            callbackInfoCreator,
            issueCategoryRetriever,
            eventManager,
            executingJobManager
        );

        jobDetailsAccessor.saveConcreteJobDetails(jobId, jobDetailsModel);
        JiraServerTransitionEventHandler handler = new JiraServerTransitionEventHandler(
            eventManager,
            gson,
            propertiesFactory,
            messageSenderFactory,
            jobDetailsAccessor,
            responsePostProcessor,
            executingJobManager
        );
        ExistingIssueDetails<String> existingIssueDetails = new ExistingIssueDetails<>("id", "key", "summary", "link", IssueStatus.UNKNOWN, IssueCategory.BOM);
        IssueTransitionModel<String> model = new IssueTransitionModel<>(existingIssueDetails, IssueOperation.RESOLVE, List.of(), null);
        JiraServerTransitionEvent event = new JiraServerTransitionEvent(
            IssueTrackerTransitionIssueEvent.createDefaultEventDestination(ChannelKeys.JIRA_SERVER),
            jobExecutionId,
            jobId,
            notificationIds,
            model
        );
        
        handler.handle(event);
        assertEquals(1, issueCounter.get());
    }

    private JiraServerJobDetailsModel createJobDetails(UUID jobId) {
        return new JiraServerJobDetailsModel(
            jobId,
            "user",
            "jiraProject",
            "Task",
            "Resolve",
            "Reopen",
            List.of(),
            ""
        );
    }

    private StatusDetailsComponent createStatusDetailsComponent() {
        StatusCategory statusCategory = new StatusCategory();

        return new StatusDetailsComponent("self", "description", "iconUrl", "name", "id", statusCategory);
    }

    private TransitionComponent createTransition() {
        return new TransitionComponent("id", "Resolve", null, false, false, false, Map.of(), null);
    }
}
