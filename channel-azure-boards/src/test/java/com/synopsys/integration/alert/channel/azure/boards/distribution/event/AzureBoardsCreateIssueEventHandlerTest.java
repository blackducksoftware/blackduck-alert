package com.synopsys.integration.alert.channel.azure.boards.distribution.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.IssueTrackerResponsePostProcessor;
import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.callback.ProviderCallbackIssueTrackerResponsePostProcessor;
import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCreateIssueEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCreationModel;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsHttpExceptionMessageImprover;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsProperties;
import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsPropertiesFactory;
import com.synopsys.integration.alert.channel.azure.boards.distribution.AzureBoardsMessageSenderFactory;
import com.synopsys.integration.alert.channel.azure.boards.distribution.event.mock.MockAzureBoardsJobDetailsRepository;
import com.synopsys.integration.alert.channel.azure.boards.distribution.event.mock.MockCorrelationToNotificationRelationRepository;
import com.synopsys.integration.alert.channel.azure.boards.distribution.event.mock.MockJobSubTaskStatusRepository;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.workflow.JobSubTaskStatusModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.database.api.workflow.DefaultJobSubTaskAccessor;
import com.synopsys.integration.alert.database.job.azure.boards.DefaultAzureBoardsJobDetailsAccessor;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.azure.boards.common.http.AzureHttpRequestCreator;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.proxy.ProxyInfo;

class AzureBoardsCreateIssueEventHandlerTest {

    public static final String ISSUE_KEY = "JP-1";
    private Gson gson = new Gson();
    private AtomicInteger issueCounter;
    private EventManager eventManager;
    private DefaultJobSubTaskAccessor jobSubTaskAccessor;
    private IssueTrackerResponsePostProcessor responsePostProcessor;
    private DefaultAzureBoardsJobDetailsAccessor jobDetailsAccessor;
    private ExecutingJobManager executingJobManager;

    @BeforeEach
    public void init() {
        issueCounter = new AtomicInteger(0);
        eventManager = Mockito.mock(EventManager.class);
        responsePostProcessor = new ProviderCallbackIssueTrackerResponsePostProcessor(eventManager);

        MockJobSubTaskStatusRepository subTaskRepository = new MockJobSubTaskStatusRepository();
        MockCorrelationToNotificationRelationRepository relationRepository = new MockCorrelationToNotificationRelationRepository();
        jobSubTaskAccessor = new DefaultJobSubTaskAccessor(subTaskRepository, relationRepository);

        MockAzureBoardsJobDetailsRepository azureBoardsJobDetailsRepository = new MockAzureBoardsJobDetailsRepository();
        jobDetailsAccessor = new DefaultAzureBoardsJobDetailsAccessor(azureBoardsJobDetailsRepository);
        executingJobManager = Mockito.mock(ExecutingJobManager.class);
    }

    @Test
    void handleUnknownJobTest() {
        UUID parentEventId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);

        AzureBoardsPropertiesFactory propertiesFactory = Mockito.mock(AzureBoardsPropertiesFactory.class);
        IssueTrackerCallbackInfoCreator callbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        ProxyManager mockProxyManager = Mockito.mock(ProxyManager.class);
        AzureBoardsHttpExceptionMessageImprover exceptionMessageImprover = new AzureBoardsHttpExceptionMessageImprover(gson);
        AzureBoardsMessageSenderFactory messageSenderFactory = new AzureBoardsMessageSenderFactory(
            gson,
            callbackInfoCreator,
            ChannelKeys.AZURE_BOARDS,
            propertiesFactory,
            mockProxyManager,
            exceptionMessageImprover,
            issueCategoryRetriever,
            eventManager,
            jobSubTaskAccessor,
            executingJobManager
        );

        Mockito.when(mockProxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(ProxyInfo.NO_PROXY_INFO);

        AzureBoardsJobDetailsModel jobDetailsModel = createJobDetails(jobId);
        jobSubTaskAccessor.createSubTaskStatus(parentEventId, jobDetailsModel.getJobId(), 1L, notificationIds);

        AzureBoardsCreateIssueEventHandler handler = new AzureBoardsCreateIssueEventHandler(
            eventManager,
            jobSubTaskAccessor,
            gson,
            propertiesFactory,
            messageSenderFactory,
            mockProxyManager,
            jobDetailsAccessor,
            responsePostProcessor,
            executingJobManager
        );

        LinkableItem provider = new LinkableItem("provider", "test-provider");
        IssueCreationModel issueCreationModel = IssueCreationModel.simple("title", "description", List.of(), provider);
        AzureBoardsCreateIssueEvent event = new AzureBoardsCreateIssueEvent(
            IssueTrackerCreateIssueEvent.createDefaultEventDestination(ChannelKeys.AZURE_BOARDS),
            parentEventId,
            jobExecutionId,
            jobId,
            notificationIds,
            issueCreationModel
        );

        handler.handleEvent(event);
        assertEquals(0, issueCounter.get());
        Optional<JobSubTaskStatusModel> jobSubTaskStatusModelOptional = jobSubTaskAccessor.getSubTaskStatus(parentEventId);
        assertTrue(jobSubTaskStatusModelOptional.isPresent());
    }

    @Test
    @Disabled("Blocked by IALERT-3136")
    void handleIssueQueryBlankTest() throws IntegrationException, IOException {
        UUID parentEventId = UUID.randomUUID();
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);

        AzureBoardsPropertiesFactory propertiesFactory = Mockito.mock(AzureBoardsPropertiesFactory.class);
        AzureBoardsProperties azureBoardsProperties = Mockito.mock(AzureBoardsProperties.class);
        AzureHttpRequestCreator azureHttpRequestCreator = Mockito.mock(AzureHttpRequestCreator.class);

        AzureBoardsJobDetailsModel jobDetailsModel = createJobDetails(jobId);
        jobSubTaskAccessor.createSubTaskStatus(parentEventId, jobDetailsModel.getJobId(), 1L, notificationIds);

        //TODO: Mockito.doAnswer when we create a work item to increment the issueCounter

        IssueTrackerCallbackInfoCreator callbackInfoCreator = new IssueTrackerCallbackInfoCreator();
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        ProxyManager mockProxyManager = Mockito.mock(ProxyManager.class);
        AzureBoardsHttpExceptionMessageImprover exceptionMessageImprover = new AzureBoardsHttpExceptionMessageImprover(gson);
        AzureBoardsMessageSenderFactory messageSenderFactory = new AzureBoardsMessageSenderFactory(
            gson,
            callbackInfoCreator,
            ChannelKeys.AZURE_BOARDS,
            propertiesFactory,
            mockProxyManager,
            exceptionMessageImprover,
            issueCategoryRetriever,
            eventManager,
            jobSubTaskAccessor,
            executingJobManager
        );

        //TODO: Mock out services required for Azure, blocked by IALERT-3136

        Mockito.when(propertiesFactory.createAzureBoardsProperties(Mockito.any())).thenReturn(azureBoardsProperties);
        Mockito.when(azureBoardsProperties.getOrganizationName()).thenReturn("organizationName");
        Mockito.when(azureBoardsProperties.createAzureHttpRequestCreator(Mockito.any(), Mockito.any())).thenReturn(azureHttpRequestCreator);
        Mockito.when(mockProxyManager.createProxyInfoForHost(Mockito.anyString())).thenReturn(ProxyInfo.NO_PROXY_INFO);

        jobDetailsAccessor.saveAzureBoardsJobDetails(jobId, jobDetailsModel);
        AzureBoardsCreateIssueEventHandler handler = new AzureBoardsCreateIssueEventHandler(
            eventManager,
            jobSubTaskAccessor,
            gson,
            propertiesFactory,
            messageSenderFactory,
            mockProxyManager,
            jobDetailsAccessor,
            responsePostProcessor,
            executingJobManager
        );

        LinkableItem provider = new LinkableItem("provider", "test-provider");
        IssueCreationModel issueCreationModel = IssueCreationModel.simple("title", "description", List.of(), provider);
        AzureBoardsCreateIssueEvent event = new AzureBoardsCreateIssueEvent(
            IssueTrackerCreateIssueEvent.createDefaultEventDestination(ChannelKeys.AZURE_BOARDS),
            parentEventId,
            jobExecutionId,
            jobId,
            notificationIds,
            issueCreationModel
        );

        handler.handle(event);
        assertEquals(0, issueCounter.get());
        Optional<JobSubTaskStatusModel> jobSubTaskStatusModelOptional = jobSubTaskAccessor.getSubTaskStatus(parentEventId);
        assertTrue(jobSubTaskStatusModelOptional.isPresent());
    }

    private AzureBoardsJobDetailsModel createJobDetails(UUID jobId) {
        return new AzureBoardsJobDetailsModel(
            jobId,
            true,
            "azureProject",
            "Task",
            "Done",
            "To Do"
        );
    }

}
