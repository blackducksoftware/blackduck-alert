package com.synopsys.integration.alert.channel.jira.cloud.distribution.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
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
import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCommentEvent;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.channel.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueStatus;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.cloud.distribution.JiraCloudMessageSenderFactory;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.api.descriptor.model.ChannelKeys;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.FieldService;
import com.synopsys.integration.jira.common.cloud.service.IssueSearchService;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.cloud.service.ProjectService;
import com.synopsys.integration.jira.common.model.request.IssueCommentRequestModel;
import com.synopsys.integration.jira.common.model.response.IssueCommentResponseModel;

class JiraCloudCommentEventHandlerTest {
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private AtomicInteger issueCounter;
    private EventManager eventManager;
    private IssueTrackerResponsePostProcessor responsePostProcessor;
    private ExecutingJobManager executingJobManager;

    @BeforeEach
    public void init() {
        issueCounter = new AtomicInteger(0);
        eventManager = Mockito.mock(EventManager.class);
        responsePostProcessor = Mockito.mock(IssueTrackerResponsePostProcessor.class);
        executingJobManager = Mockito.mock(ExecutingJobManager.class);
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
        JobDetailsAccessor<JiraCloudJobDetailsModel> jobDetailsAccessor = jobId1 -> Optional.empty();

        JiraCloudCommentEventHandler handler = new JiraCloudCommentEventHandler(
            eventManager,
            gson,
            propertiesFactory,
            messageSenderFactory,
            jobDetailsAccessor,
            responsePostProcessor,
            executingJobManager
        );
        ExistingIssueDetails<String> existingIssueDetails = new ExistingIssueDetails<>("id", "key", "summary", "link", IssueStatus.UNKNOWN, IssueCategory.BOM);
        IssueCommentModel<String> model = new IssueCommentModel<>(existingIssueDetails, List.of(), null);
        JiraCloudCommentEvent event = new JiraCloudCommentEvent(
            IssueTrackerCommentEvent.createDefaultEventDestination(ChannelKeys.JIRA_SERVER),
            jobExecutionId,
            jobId,
            notificationIds,
            model
        );
        handler.handle(event);
        assertEquals(0, issueCounter.get());
    }

    @Test
    void handleCommentTest() throws IntegrationException {
        UUID jobExecutionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);

        JiraCloudPropertiesFactory propertiesFactory = Mockito.mock(JiraCloudPropertiesFactory.class);
        JiraCloudProperties jiraProperties = Mockito.mock(JiraCloudProperties.class);
        JiraCloudServiceFactory jiraServiceFactory = Mockito.mock(JiraCloudServiceFactory.class);
        IssueService issueService = Mockito.mock(IssueService.class);
        IssueSearchService issueSearchService = Mockito.mock(IssueSearchService.class);
        FieldService fieldService = Mockito.mock(FieldService.class);
        ProjectService projectService = Mockito.mock(ProjectService.class);

        Mockito.doAnswer(invocation -> {
            issueCounter.incrementAndGet();
            return new IssueCommentResponseModel();
        }).when(issueService).addComment(Mockito.any(IssueCommentRequestModel.class));

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
        JobDetailsAccessor<JiraCloudJobDetailsModel> jobDetailsAccessor = jobId1 -> Optional.of(createJobDetails(jobId));

        JiraCloudCommentEventHandler handler = new JiraCloudCommentEventHandler(
            eventManager,
            gson,
            propertiesFactory,
            messageSenderFactory,
            jobDetailsAccessor,
            responsePostProcessor,
            executingJobManager
        );
        ExistingIssueDetails<String> existingIssueDetails = new ExistingIssueDetails<>("id", "key", "summary", "link", IssueStatus.UNKNOWN, IssueCategory.BOM);
        IssueCommentModel<String> model = new IssueCommentModel<>(existingIssueDetails, List.of("A comment"), null);
        JiraCloudCommentEvent event = new JiraCloudCommentEvent(
            IssueTrackerCommentEvent.createDefaultEventDestination(ChannelKeys.JIRA_CLOUD),
            jobExecutionId,
            jobId,
            notificationIds,
            model
        );
        handler.handle(event);
        assertEquals(1, issueCounter.get());
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
}
