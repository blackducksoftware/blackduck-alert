package com.synopsys.integration.alert.channel.jira.server.distribution.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.IssueTrackerResponsePostProcessor;
import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.callback.ProviderCallbackIssueTrackerResponsePostProcessor;
import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCreateIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.DefaultJiraServerJobDetailsAccessor;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.mock.MockJiraServerJobCustomFieldRepository;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.mock.MockJiraServerJobDetailsRepository;
import com.synopsys.integration.alert.channel.jira.server.distribution.JiraServerMessageSenderFactory;
import com.synopsys.integration.alert.channel.jira.server.distribution.event.mock.MockCorrelationToNotificationRelationRepository;
import com.synopsys.integration.alert.channel.jira.server.distribution.event.mock.MockJobSubTaskStatusRepository;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.workflow.JobSubTaskStatusModel;
import com.synopsys.integration.alert.database.api.workflow.DefaultJobSubTaskAccessor;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.IssueFieldsComponent;
import com.synopsys.integration.jira.common.model.components.ProjectComponent;
import com.synopsys.integration.jira.common.model.response.IssueCreationResponseModel;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import com.synopsys.integration.jira.common.model.response.IssueTypeResponseModel;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.server.model.IssueCreationRequestModel;
import com.synopsys.integration.jira.common.server.model.IssueSearchIssueComponent;
import com.synopsys.integration.jira.common.server.model.IssueSearchIssueFieldsComponent;
import com.synopsys.integration.jira.common.server.model.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.server.service.FieldService;
import com.synopsys.integration.jira.common.server.service.IssueSearchService;
import com.synopsys.integration.jira.common.server.service.IssueService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.jira.common.server.service.ProjectService;

class JiraServerCreateIssueEventHandlerTest {

    public static final String ISSUE_KEY = "JP-1";
    private Gson gson = new Gson();
    private AtomicInteger issueCounter;
    private EventManager eventManager;
    private DefaultJobSubTaskAccessor jobSubTaskAccessor;
    private IssueTrackerResponsePostProcessor responsePostProcessor;
    private DefaultJiraServerJobDetailsAccessor jobDetailsAccessor;

    @BeforeEach
    public void init() {
        issueCounter = new AtomicInteger(0);
        eventManager = Mockito.mock(EventManager.class);
        responsePostProcessor = new ProviderCallbackIssueTrackerResponsePostProcessor(eventManager);

        MockJobSubTaskStatusRepository subTaskRepository = new MockJobSubTaskStatusRepository();
        MockCorrelationToNotificationRelationRepository relationRepository = new MockCorrelationToNotificationRelationRepository();
        jobSubTaskAccessor = new DefaultJobSubTaskAccessor(subTaskRepository, relationRepository);

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
        UUID parentEventId = UUID.randomUUID();
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
            jobSubTaskAccessor
        );

        JiraServerJobDetailsModel jobDetailsModel = createJobDetails(jobId);
        jobSubTaskAccessor.createSubTaskStatus(parentEventId, jobDetailsModel.getJobId(), jobExecutionId, 1L, notificationIds);

        JiraServerCreateIssueEventHandler handler = new JiraServerCreateIssueEventHandler(
            eventManager,
            jobSubTaskAccessor,
            gson,
            propertiesFactory,
            messageSenderFactory,
            jobDetailsAccessor,
            responsePostProcessor
        );

        LinkableItem provider = new LinkableItem("provider", "test-provider");
        IssueCreationModel issueCreationModel = IssueCreationModel.simple("title", "description", List.of(), provider);
        JiraServerCreateIssueEvent event = new JiraServerCreateIssueEvent(
            IssueTrackerCreateIssueEvent.createDefaultEventDestination(ChannelKeys.JIRA_SERVER),
            parentEventId,
            jobExecutionId,
            jobId,
            notificationIds,
            issueCreationModel
        );

        handler.handle(event);
        assertEquals(0, issueCounter.get());
        Optional<JobSubTaskStatusModel> jobSubTaskStatusModelOptional = jobSubTaskAccessor.getSubTaskStatus(parentEventId);
        assertTrue(jobSubTaskStatusModelOptional.isEmpty());
    }

    @Test
    void handleIssueQueryBlankTest() throws IntegrationException {
        UUID parentEventId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);

        JiraServerJobDetailsModel jobDetailsModel = createJobDetails(jobId);
        jobSubTaskAccessor.createSubTaskStatus(parentEventId, jobDetailsModel.getJobId(), jobExecutionId, 1L, notificationIds);

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
            jobSubTaskAccessor
        );

        jobDetailsAccessor.saveConcreteJobDetails(jobId, jobDetailsModel);
        JiraServerCreateIssueEventHandler handler = new JiraServerCreateIssueEventHandler(
            eventManager,
            jobSubTaskAccessor,
            gson,
            propertiesFactory,
            messageSenderFactory,
            jobDetailsAccessor,
            responsePostProcessor
        );

        LinkableItem provider = new LinkableItem("provider", "test-provider");
        IssueCreationModel issueCreationModel = IssueCreationModel.simple("title", "description", List.of(), provider);
        JiraServerCreateIssueEvent event = new JiraServerCreateIssueEvent(
            IssueTrackerCreateIssueEvent.createDefaultEventDestination(ChannelKeys.JIRA_SERVER),
            parentEventId,
            jobExecutionId,
            jobId,
            notificationIds,
            issueCreationModel
        );

        handler.handle(event);
        assertEquals(1, issueCounter.get());
        Optional<JobSubTaskStatusModel> jobSubTaskStatusModelOptional = jobSubTaskAccessor.getSubTaskStatus(parentEventId);
        assertFalse(jobSubTaskStatusModelOptional.isPresent());
    }

    @Test
    void handleIssueIssueDoesNotExistTest() throws IntegrationException {
        UUID parentEventId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);

        JiraServerJobDetailsModel jobDetailsModel = createJobDetails(jobId);
        jobSubTaskAccessor.createSubTaskStatus(parentEventId, jobDetailsModel.getJobId(), jobExecutionId, 1L, notificationIds);

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
            jobSubTaskAccessor
        );

        jobDetailsAccessor.saveConcreteJobDetails(jobId, jobDetailsModel);
        JiraServerCreateIssueEventHandler handler = new JiraServerCreateIssueEventHandler(
            eventManager,
            jobSubTaskAccessor,
            gson,
            propertiesFactory,
            messageSenderFactory,
            jobDetailsAccessor,
            responsePostProcessor
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
            parentEventId,
            jobExecutionId,
            jobId,
            notificationIds,
            issueCreationModel
        );

        handler.handle(event);
        assertEquals(1, issueCounter.get());
        Optional<JobSubTaskStatusModel> jobSubTaskStatusModelOptional = jobSubTaskAccessor.getSubTaskStatus(parentEventId);
        assertFalse(jobSubTaskStatusModelOptional.isPresent());
    }

    @Test
    void handleIssueIssueExistsTest() throws IntegrationException {
        UUID parentEventId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);

        JiraServerJobDetailsModel jobDetailsModel = createJobDetails(jobId);
        jobSubTaskAccessor.createSubTaskStatus(parentEventId, jobDetailsModel.getJobId(), jobExecutionId, 1L, notificationIds);

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
            jobSubTaskAccessor
        );

        jobDetailsAccessor.saveConcreteJobDetails(jobId, jobDetailsModel);
        JiraServerCreateIssueEventHandler handler = new JiraServerCreateIssueEventHandler(
            eventManager,
            jobSubTaskAccessor,
            gson,
            propertiesFactory,
            messageSenderFactory,
            jobDetailsAccessor,
            responsePostProcessor
        );

        LinkableItem provider = new LinkableItem("provider", "test-provider");
        ProviderDetails providerDetails = new ProviderDetails(1L, provider);
        ProjectIssueModel projectIssueModel = ProjectIssueModel.bom(providerDetails, null, null, null);
        IssueCreationModel issueCreationModel = IssueCreationModel.project("title", "description", List.of(), projectIssueModel, "JQL Query String");
        JiraServerCreateIssueEvent event = new JiraServerCreateIssueEvent(
            IssueTrackerCreateIssueEvent.createDefaultEventDestination(ChannelKeys.JIRA_SERVER),
            parentEventId,
            jobExecutionId,
            jobId,
            notificationIds,
            issueCreationModel
        );

        handler.handle(event);
        assertEquals(0, issueCounter.get());
        Optional<JobSubTaskStatusModel> jobSubTaskStatusModelOptional = jobSubTaskAccessor.getSubTaskStatus(parentEventId);
        assertFalse(jobSubTaskStatusModelOptional.isPresent());
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

    private IssueResponseModel createIssueResponseModel() {
        String id = ISSUE_KEY;
        IssueFieldsComponent issueFieldsComponent = new IssueFieldsComponent(List.of(), null, null, "summary", "description", List.of(), null, List.of(), null, null, null, null);

        return new IssueResponseModel("", id, "", id, Map.of(), Map.of(), Map.of(), Map.of(), List.of(), null, null, null, null, null, issueFieldsComponent);
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
