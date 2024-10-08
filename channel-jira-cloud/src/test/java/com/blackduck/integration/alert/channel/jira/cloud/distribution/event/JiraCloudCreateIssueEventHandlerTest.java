package com.blackduck.integration.alert.channel.jira.cloud.distribution.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.channel.jira.cloud.database.accessor.mock.MockJiraCloudJobCustomFieldRepository;
import com.blackduck.integration.alert.channel.jira.cloud.database.accessor.mock.MockJiraCloudJobDetailsRepository;
import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.tracker.IssueTrackerResponsePostProcessor;
import com.synopsys.integration.alert.api.channel.issue.tracker.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.tracker.callback.ProviderCallbackIssueTrackerResponsePostProcessor;
import com.synopsys.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueBomComponentDetails;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.blackduck.integration.alert.channel.jira.cloud.JiraCloudPropertiesFactory;
import com.blackduck.integration.alert.channel.jira.cloud.distribution.JiraCloudMessageSenderFactory;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.database.job.jira.cloud.DefaultJiraCloudJobDetailsAccessor;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.synopsys.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.cloud.model.IssueCreationRequestModel;
import com.blackduck.integration.jira.common.cloud.model.IssueSearchResponseModel;
import com.blackduck.integration.jira.common.cloud.service.FieldService;
import com.blackduck.integration.jira.common.cloud.service.IssueSearchService;
import com.blackduck.integration.jira.common.cloud.service.IssueService;
import com.blackduck.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.blackduck.integration.jira.common.cloud.service.ProjectService;
import com.blackduck.integration.jira.common.model.components.IssueFieldsComponent;
import com.blackduck.integration.jira.common.model.components.ProjectComponent;
import com.blackduck.integration.jira.common.model.response.IssueCreationResponseModel;
import com.blackduck.integration.jira.common.model.response.IssueResponseModel;
import com.blackduck.integration.jira.common.model.response.PageOfProjectsResponseModel;
import com.blackduck.integration.jira.common.rest.service.IssuePropertyService;

class JiraCloudCreateIssueEventHandlerTest {

    public static final String ISSUE_KEY = "JP-1";
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private AtomicInteger issueCounter;
    private EventManager eventManager;
    private IssueTrackerResponsePostProcessor responsePostProcessor;
    private DefaultJiraCloudJobDetailsAccessor jobDetailsAccessor;
    private ExecutingJobManager executingJobManager;

    @BeforeEach
    public void init() {
        issueCounter = new AtomicInteger(0);
        eventManager = Mockito.mock(EventManager.class);
        executingJobManager = Mockito.mock(ExecutingJobManager.class);
        responsePostProcessor = new ProviderCallbackIssueTrackerResponsePostProcessor(eventManager);

        MockJiraCloudJobDetailsRepository jiraCloudJobDetailsRepository = new MockJiraCloudJobDetailsRepository();
        MockJiraCloudJobCustomFieldRepository jiraCloudJobCustomFieldRepository = new MockJiraCloudJobCustomFieldRepository();
        jobDetailsAccessor = new DefaultJiraCloudJobDetailsAccessor(
            jiraCloudJobDetailsRepository,
            jiraCloudJobCustomFieldRepository
        );
    }

    @Test
    void handleUnknownJobTest() {
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);

        JiraCloudPropertiesFactory propertiesFactory = Mockito.mock(JiraCloudPropertiesFactory.class);
        IssueTrackerCallbackInfoCreator callbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        JiraCloudMessageSenderFactory messageSenderFactory = new JiraCloudMessageSenderFactory(
            gson,
            ChannelKeys.JIRA_CLOUD,
            propertiesFactory,
            callbackInfoCreator,
            issueCategoryRetriever,
            eventManager,
            executingJobManager
        );

        JiraCloudCreateIssueEventHandler handler = new JiraCloudCreateIssueEventHandler(
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
        JiraCloudCreateIssueEvent event = new JiraCloudCreateIssueEvent(
            IssueTrackerCreateIssueEvent.createDefaultEventDestination(ChannelKeys.JIRA_CLOUD),
            jobExecutionId,
            jobId,
            notificationIds,
            issueCreationModel
        );

        handler.handle(event);
    }

    @Test
    void handleIssueQueryBlankTest() throws IntegrationException {
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);

        JiraCloudJobDetailsModel jobDetailsModel = createJobDetails(jobId);

        JiraCloudPropertiesFactory propertiesFactory = Mockito.mock(JiraCloudPropertiesFactory.class);
        JiraCloudProperties jiraProperties = Mockito.mock(JiraCloudProperties.class);
        JiraCloudServiceFactory jiraServiceFactory = Mockito.mock(JiraCloudServiceFactory.class);
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
            .thenReturn(new IssueSearchResponseModel("", List.of(createIssueResponseModel()), List.of(), null, null));
        Mockito.when(projectService.getProjectsByName(Mockito.any())).thenReturn(createProjectComponent());

        Mockito.when(jiraServiceFactory.createFieldService()).thenReturn(fieldService);
        Mockito.when(jiraServiceFactory.createIssueService()).thenReturn(issueService);
        Mockito.when(jiraServiceFactory.createIssueSearchService()).thenReturn(issueSearchService);
        Mockito.when(jiraServiceFactory.createProjectService()).thenReturn(projectService);
        Mockito.when(jiraProperties.createJiraServicesCloudFactory(Mockito.any(), Mockito.any())).thenReturn(jiraServiceFactory);
        Mockito.when(propertiesFactory.createJiraProperties()).thenReturn(jiraProperties);

        IssueTrackerCallbackInfoCreator callbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        JiraCloudMessageSenderFactory messageSenderFactory = new JiraCloudMessageSenderFactory(
            gson,
            ChannelKeys.JIRA_CLOUD,
            propertiesFactory,
            callbackInfoCreator,
            issueCategoryRetriever,
            eventManager,
            executingJobManager
        );

        jobDetailsAccessor.saveJiraCloudJobDetails(jobId, jobDetailsModel);
        JiraCloudCreateIssueEventHandler handler = new JiraCloudCreateIssueEventHandler(
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
        JiraCloudCreateIssueEvent event = new JiraCloudCreateIssueEvent(
            IssueTrackerCreateIssueEvent.createDefaultEventDestination(ChannelKeys.JIRA_CLOUD),
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

        JiraCloudJobDetailsModel jobDetailsModel = createJobDetails(jobId);

        JiraCloudPropertiesFactory propertiesFactory = Mockito.mock(JiraCloudPropertiesFactory.class);
        JiraCloudProperties jiraProperties = Mockito.mock(JiraCloudProperties.class);
        JiraCloudServiceFactory jiraServiceFactory = Mockito.mock(JiraCloudServiceFactory.class);
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
            .thenReturn(new IssueSearchResponseModel("", List.of(), List.of(), null, null));
        Mockito.when(projectService.getProjectsByName(Mockito.any())).thenReturn(createProjectComponent());

        Mockito.when(jiraServiceFactory.createFieldService()).thenReturn(fieldService);
        Mockito.when(jiraServiceFactory.createIssueService()).thenReturn(issueService);
        Mockito.when(jiraServiceFactory.createIssueSearchService()).thenReturn(issueSearchService);
        Mockito.when(jiraServiceFactory.createProjectService()).thenReturn(projectService);
        Mockito.when(jiraServiceFactory.createIssuePropertyService()).thenReturn(issuePropertyService);
        Mockito.when(jiraProperties.createJiraServicesCloudFactory(Mockito.any(), Mockito.any())).thenReturn(jiraServiceFactory);
        Mockito.when(propertiesFactory.createJiraProperties()).thenReturn(jiraProperties);

        IssueTrackerCallbackInfoCreator callbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        JiraCloudMessageSenderFactory messageSenderFactory = new JiraCloudMessageSenderFactory(
            gson,
            ChannelKeys.JIRA_CLOUD,
            propertiesFactory,
            callbackInfoCreator,
            issueCategoryRetriever,
            eventManager,
            executingJobManager
        );

        jobDetailsAccessor.saveJiraCloudJobDetails(jobId, jobDetailsModel);
        JiraCloudCreateIssueEventHandler handler = new JiraCloudCreateIssueEventHandler(
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
        JiraCloudCreateIssueEvent event = new JiraCloudCreateIssueEvent(
            IssueTrackerCreateIssueEvent.createDefaultEventDestination(ChannelKeys.JIRA_CLOUD),
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

        JiraCloudJobDetailsModel jobDetailsModel = createJobDetails(jobId);

        JiraCloudPropertiesFactory propertiesFactory = Mockito.mock(JiraCloudPropertiesFactory.class);
        JiraCloudProperties jiraProperties = Mockito.mock(JiraCloudProperties.class);
        JiraCloudServiceFactory jiraServiceFactory = Mockito.mock(JiraCloudServiceFactory.class);
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
        Mockito.when(projectService.getProjectsByName(Mockito.any())).thenReturn(createProjectComponent());

        Mockito.when(jiraServiceFactory.createFieldService()).thenReturn(fieldService);
        Mockito.when(jiraServiceFactory.createIssueService()).thenReturn(issueService);
        Mockito.when(jiraServiceFactory.createIssueSearchService()).thenReturn(issueSearchService);
        Mockito.when(jiraServiceFactory.createProjectService()).thenReturn(projectService);
        Mockito.when(jiraProperties.createJiraServicesCloudFactory(Mockito.any(), Mockito.any())).thenReturn(jiraServiceFactory);
        Mockito.when(propertiesFactory.createJiraProperties()).thenReturn(jiraProperties);

        IssueTrackerCallbackInfoCreator callbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        JiraCloudMessageSenderFactory messageSenderFactory = new JiraCloudMessageSenderFactory(
            gson,
            ChannelKeys.JIRA_CLOUD,
            propertiesFactory,
            callbackInfoCreator,
            issueCategoryRetriever,
            eventManager,
            executingJobManager
        );

        jobDetailsAccessor.saveJiraCloudJobDetails(jobId, jobDetailsModel);
        JiraCloudCreateIssueEventHandler handler = new JiraCloudCreateIssueEventHandler(
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
        JiraCloudCreateIssueEvent event = new JiraCloudCreateIssueEvent(
            IssueTrackerCreateIssueEvent.createDefaultEventDestination(ChannelKeys.JIRA_CLOUD),
            jobExecutionId,
            jobId,
            notificationIds,
            issueCreationModel
        );

        handler.handle(event);
        assertEquals(0, issueCounter.get());
    }

    private JiraCloudJobDetailsModel createJobDetails(UUID jobId) {
        return new JiraCloudJobDetailsModel(
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

    private PageOfProjectsResponseModel createProjectComponent() {
        ProjectComponent projectComponent = new ProjectComponent("", "JP", ISSUE_KEY, "jiraProject", null, null, true, "");
        return new PageOfProjectsResponseModel(List.of(projectComponent));
    }

    private IssueResponseModel createIssueResponseModel() {
        String id = ISSUE_KEY;
        IssueFieldsComponent issueFieldsComponent = new IssueFieldsComponent(List.of(), null, null, "summary", "description", List.of(), null, List.of(), null, null, null, null);

        return new IssueResponseModel("", id, "", id, Map.of(), Map.of(), Map.of(), Map.of(), List.of(), null, null, null, null, null, issueFieldsComponent);
    }

    private IssueSearchResponseModel createIssueSearchResponseModel() {
        IssueResponseModel issueResponseModel = createIssueResponseModel();
        return new IssueSearchResponseModel("", List.of(issueResponseModel), List.of(), null, null);
    }

}
