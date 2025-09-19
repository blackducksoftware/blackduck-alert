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

import com.blackduck.integration.jira.common.server.model.JiraServerIssueResponseModel;
import com.blackduck.integration.jira.common.server.model.component.JiraServerIssueFieldsComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerResponsePostProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.callback.IssueTrackerCallbackInfoCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.callback.ProviderCallbackIssueTrackerResponsePostProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueBomComponentDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.channel.jira.server.JiraServerProperties;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.DefaultJiraServerJobDetailsAccessor;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.mock.MockJiraServerJobCustomFieldRepository;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.mock.MockJiraServerJobDetailsRepository;
import com.blackduck.integration.alert.channel.jira.server.distribution.JiraServerMessageSenderFactory;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.components.IssueFieldsComponent;
import com.blackduck.integration.jira.common.model.components.ProjectComponent;
import com.blackduck.integration.jira.common.model.response.IssueCreationResponseModel;
import com.blackduck.integration.jira.common.model.response.IssueResponseModel;
import com.blackduck.integration.jira.common.model.response.IssueTypeResponseModel;
import com.blackduck.integration.jira.common.rest.service.IssuePropertyService;
import com.blackduck.integration.jira.common.server.model.IssueCreationRequestModel;
import com.blackduck.integration.jira.common.server.model.IssueSearchIssueComponent;
import com.blackduck.integration.jira.common.server.model.IssueSearchIssueFieldsComponent;
import com.blackduck.integration.jira.common.server.model.IssueSearchResponseModel;
import com.blackduck.integration.jira.common.server.service.FieldService;
import com.blackduck.integration.jira.common.server.service.IssueSearchService;
import com.blackduck.integration.jira.common.server.service.IssueService;
import com.blackduck.integration.jira.common.server.service.JiraServerServiceFactory;
import com.blackduck.integration.jira.common.server.service.ProjectService;
import com.google.gson.Gson;

class JiraServerCreateIssueEventHandlerTest {

    public static final String ISSUE_KEY = "JP-1";
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
        executingJobManager = Mockito.mock(ExecutingJobManager.class);
        responsePostProcessor = new ProviderCallbackIssueTrackerResponsePostProcessor(eventManager);

        MockJiraServerJobDetailsRepository jiraServerJobDetailsRepository = new MockJiraServerJobDetailsRepository();
        MockJiraServerJobCustomFieldRepository jiraServerJobCustomFieldRepository = new MockJiraServerJobCustomFieldRepository();
        jobDetailsAccessor = new DefaultJiraServerJobDetailsAccessor(
            ChannelKeys.JIRA_SERVER,
            jiraServerJobDetailsRepository,
            jiraServerJobCustomFieldRepository
        );
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

        JiraServerCreateIssueEventHandler handler = new JiraServerCreateIssueEventHandler(
            eventManager,
            gson,
            propertiesFactory,
            messageSenderFactory,
            jobDetailsAccessor,
            responsePostProcessor,
            executingJobManager
        );

        LinkableItem provider = new LinkableItem("provider", "test-provider");
        IssueCreationModel issueCreationModel = IssueCreationModel.simple("title", "description", List.of(), provider);
        JiraServerCreateIssueEvent event = new JiraServerCreateIssueEvent(
            IssueTrackerCreateIssueEvent.createDefaultEventDestination(ChannelKeys.JIRA_SERVER),
            jobExecutionId,
            jobId,
            notificationIds,
            issueCreationModel
        );

        handler.handle(event);
        assertEquals(0, issueCounter.get());
    }

    @Test
    void handleIssueQueryBlankTest() throws IntegrationException {
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
        IssuePropertyService issuePropertyService = Mockito.mock(IssuePropertyService.class);

        Mockito.doAnswer(invocation -> {
            issueCounter.incrementAndGet();
            return new IssueCreationResponseModel(ISSUE_KEY, "", ISSUE_KEY);
        }).when(issueService).createIssue(Mockito.any(IssueCreationRequestModel.class));
        Mockito.when(issueService.getIssue(Mockito.anyString())).thenReturn(createIssueResponseModel());
        Mockito.when(issueSearchService.queryForIssues(Mockito.any()))
            .thenReturn(new IssueSearchResponseModel(null, List.of(new IssueSearchIssueComponent("", ISSUE_KEY, ISSUE_KEY, null))));
        Mockito.when(projectService.getProjectsByName(Mockito.any())).thenReturn(List.of(createProjectComponent()));

        Mockito.when(jiraServiceFactory.createFieldService()).thenReturn(fieldService);
        Mockito.when(jiraServiceFactory.createIssueService()).thenReturn(issueService);
        Mockito.when(jiraServiceFactory.createIssueSearchService()).thenReturn(issueSearchService);
        Mockito.when(jiraServiceFactory.createProjectService()).thenReturn(projectService);
        Mockito.when(jiraServiceFactory.createIssuePropertyService()).thenReturn(issuePropertyService);
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
        JiraServerCreateIssueEventHandler handler = new JiraServerCreateIssueEventHandler(
            eventManager,
            gson,
            propertiesFactory,
            messageSenderFactory,
            jobDetailsAccessor,
            responsePostProcessor,
            executingJobManager
        );

        LinkableItem provider = new LinkableItem("provider", "test-provider");
        IssueCreationModel issueCreationModel = IssueCreationModel.simple("title", "description", List.of(), provider);
        JiraServerCreateIssueEvent event = new JiraServerCreateIssueEvent(
            IssueTrackerCreateIssueEvent.createDefaultEventDestination(ChannelKeys.JIRA_SERVER),
            jobExecutionId,
            jobId,
            notificationIds,
            issueCreationModel
        );

        handler.handle(event);
        assertEquals(1, issueCounter.get());
    }

    @Test
    void handleIssueIssueDoesNotExistTest() throws IntegrationException {
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
        IssuePropertyService issuePropertyService = Mockito.mock(IssuePropertyService.class);

        Mockito.doAnswer(invocation -> {
            issueCounter.incrementAndGet();
            return new IssueCreationResponseModel(ISSUE_KEY, "", ISSUE_KEY);
        }).when(issueService).createIssue(Mockito.any(IssueCreationRequestModel.class));
        Mockito.when(issueService.getIssue(Mockito.anyString())).thenReturn(createIssueResponseModel());
        Mockito.when(issueSearchService.queryForIssues(Mockito.any()))
            .thenReturn(new IssueSearchResponseModel("", List.of()));
        Mockito.when(projectService.getProjectsByName(Mockito.any())).thenReturn(List.of(createProjectComponent()));

        Mockito.when(jiraServiceFactory.createFieldService()).thenReturn(fieldService);
        Mockito.when(jiraServiceFactory.createIssueService()).thenReturn(issueService);
        Mockito.when(jiraServiceFactory.createIssueSearchService()).thenReturn(issueSearchService);
        Mockito.when(jiraServiceFactory.createProjectService()).thenReturn(projectService);
        Mockito.when(jiraServiceFactory.createIssuePropertyService()).thenReturn(issuePropertyService);
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
        JiraServerCreateIssueEventHandler handler = new JiraServerCreateIssueEventHandler(
            eventManager,
            gson,
            propertiesFactory,
            messageSenderFactory,
            jobDetailsAccessor,
            responsePostProcessor,
            executingJobManager
        );

        LinkableItem provider = new LinkableItem("provider", "test-provider");
        ProviderDetails providerDetails = new ProviderDetails(1L, provider);
        LinkableItem project = new LinkableItem("project", "test-project");
        LinkableItem projectVersion = new LinkableItem("projectVersion", "test-project-version");
        LinkableItem component = new LinkableItem("component", "test-component");
        IssueBomComponentDetails bomComponent = IssueBomComponentDetails.fromSearchResults(component, null);

        ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(providerDetails, project, projectVersion, bomComponent);
        IssueCreationModel issueCreationModel = IssueCreationModel.project("title", "description", List.of(), projectIssueModel, "JQL Query String");
        JiraServerCreateIssueEvent event = new JiraServerCreateIssueEvent(
            IssueTrackerCreateIssueEvent.createDefaultEventDestination(ChannelKeys.JIRA_SERVER),
            jobExecutionId,
            jobId,
            notificationIds,
            issueCreationModel
        );

        handler.handle(event);
        assertEquals(1, issueCounter.get());
    }

    @Test
    void handleIssueIssueExistsTest() throws IntegrationException {
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
            return new IssueCreationResponseModel(ISSUE_KEY, "", ISSUE_KEY);
        }).when(issueService).createIssue(Mockito.any(IssueCreationRequestModel.class));
        Mockito.when(issueService.getIssue(Mockito.anyString())).thenReturn(createIssueResponseModel());
        Mockito.when(issueSearchService.queryForIssues(Mockito.any()))
            .thenReturn(createIssueSearchResponseModel());
        Mockito.when(projectService.getProjectsByName(Mockito.any())).thenReturn(List.of(createProjectComponent()));

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
        JiraServerCreateIssueEventHandler handler = new JiraServerCreateIssueEventHandler(
            eventManager,
            gson,
            propertiesFactory,
            messageSenderFactory,
            jobDetailsAccessor,
            responsePostProcessor,
            executingJobManager
        );

        LinkableItem provider = new LinkableItem("provider", "test-provider");
        ProviderDetails providerDetails = new ProviderDetails(1L, provider);
        ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(providerDetails, null, null, null);
        IssueCreationModel issueCreationModel = IssueCreationModel.project("title", "description", List.of(), projectIssueModel, "JQL Query String");
        JiraServerCreateIssueEvent event = new JiraServerCreateIssueEvent(
            IssueTrackerCreateIssueEvent.createDefaultEventDestination(ChannelKeys.JIRA_SERVER),
            jobExecutionId,
            jobId,
            notificationIds,
            issueCreationModel
        );

        handler.handle(event);
        assertEquals(0, issueCounter.get());
    }

    // Helper Methods

    private JiraServerJobDetailsModel createJobDetails(UUID jobId) {
        return new JiraServerJobDetailsModel(
            jobId,
            true,
            "user",
            "jiraProject",
            "Task",
            "Resolve",
            "Reopen",
            List.of(),
            ""
        );
    }

    private ProjectComponent createProjectComponent() {
        return new ProjectComponent("", "JP", ISSUE_KEY, "jiraProject", null, null, true, "");
    }

    private JiraServerIssueResponseModel createIssueResponseModel() {
        String id = ISSUE_KEY;
        JiraServerIssueFieldsComponent issueFieldsComponent = new JiraServerIssueFieldsComponent(List.of(), null, null, "summary", "description", List.of(), null, List.of(), null, null, null, null);

        return new JiraServerIssueResponseModel("", id, "", id, Map.of(), Map.of(), Map.of(), Map.of(), List.of(), null, null, null, null, null, issueFieldsComponent);
    }

    private IssueSearchResponseModel createIssueSearchResponseModel() {
        IssueTypeResponseModel issueTypeResponseModel = new IssueTypeResponseModel();
        IssueSearchIssueComponent searchIssueComponent = new IssueSearchIssueComponent("", ISSUE_KEY, ISSUE_KEY, new IssueSearchIssueFieldsComponent(
            issueTypeResponseModel,
            List.of(),
            null,
            null,
            List.of(),
            null,
            "summary",
            null,
            null,
            List.of(),
            null,
            null,
            null,
            null,
            List.of(),
            List.of(),
            null,
            List.of(),
            null,
            null,
            null
        ));

        return new IssueSearchResponseModel("", List.of(searchIssueComponent));
    }
}
