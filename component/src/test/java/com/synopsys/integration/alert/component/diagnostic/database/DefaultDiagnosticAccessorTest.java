package com.synopsys.integration.alert.component.diagnostic.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.distribution.execution.JobStage;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.JobExecutionStatusAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModelBuilder;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobExecutionStatusDurations;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobExecutionStatusModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedQueryDetails;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.diagnostic.model.AlertQueueInformation;
import com.synopsys.integration.alert.component.diagnostic.model.AuditDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.CompletedJobDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.CompletedJobDurationDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.CompletedJobStageDurationModel;
import com.synopsys.integration.alert.component.diagnostic.model.CompletedJobsDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.DiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.JobExecutionsDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.NotificationDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.RabbitMQDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.SystemDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.utility.RabbitMQDiagnosticUtility;
import com.synopsys.integration.alert.database.api.StaticJobAccessor;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

class DefaultDiagnosticAccessorTest {
    public static final String TEST_JOB_NAME = "Job Name";
    private NotificationContentRepository notificationContentRepository;
    private AuditEntryRepository auditEntryRepository;
    private RabbitMQDiagnosticUtility rabbitMQDiagnosticUtility;
    private ExecutingJobManager executingJobManager;
    private StaticJobAccessor staticJobAccessor;
    private JobExecutionStatusAccessor completedJobsAccessor;

    @BeforeEach
    public void init() {
        notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        rabbitMQDiagnosticUtility = Mockito.mock(RabbitMQDiagnosticUtility.class);
        staticJobAccessor = Mockito.mock(StaticJobAccessor.class);
        completedJobsAccessor = Mockito.mock(JobExecutionStatusAccessor.class);
        JobExecutionStatusAccessor jobExecutionStatusAccessor = Mockito.mock(JobExecutionStatusAccessor.class);
        executingJobManager = new ExecutingJobManager(jobExecutionStatusAccessor);
    }

    @Test
    void testGetDiagnosticInfo() {
        DefaultDiagnosticAccessor diagnosticAccessor = new DefaultDiagnosticAccessor(
            notificationContentRepository,
            auditEntryRepository,
            rabbitMQDiagnosticUtility,
            staticJobAccessor,
            completedJobsAccessor,
            executingJobManager
        );
        addJobExecutions();
        NotificationDiagnosticModel notificationDiagnosticModel = createNotificationDiagnosticModel();
        AuditDiagnosticModel auditDiagnosticModel = createAuditDiagnosticModel();
        RabbitMQDiagnosticModel rabbitMQDiagnosticModel = createRabbitMQDiagnosticModel();
        CompletedJobsDiagnosticModel completedJobsDiagnosticModel = createJobDiagnosticModel();
        DiagnosticModel diagnosticModel = diagnosticAccessor.getDiagnosticInfo();

        assertEquals(notificationDiagnosticModel, diagnosticModel.getNotificationDiagnosticModel());
        assertEquals(auditDiagnosticModel, diagnosticModel.getAuditDiagnosticModel());
        assertEquals(rabbitMQDiagnosticModel, diagnosticModel.getRabbitMQDiagnosticModel());
        assertEquals(completedJobsDiagnosticModel, diagnosticModel.getCompletedJobsDiagnosticModel());
        assertExecutingJobDetails(diagnosticModel.getJobExecutionsDiagnosticModel());
        assertSystemDiagnostics(diagnosticModel.getSystemDiagnosticModel());
    }

    private NotificationDiagnosticModel createNotificationDiagnosticModel() {
        long numberOfNotifications = 10L;
        long numberOfNotificationsProcessed = 5L;
        long numberOfNotificationsUnprocessed = 5L;
        Mockito.when(notificationContentRepository.count()).thenReturn(numberOfNotifications);
        Mockito.when(notificationContentRepository.countByProcessed(true)).thenReturn(numberOfNotificationsProcessed);
        Mockito.when(notificationContentRepository.countByProcessed(false)).thenReturn(numberOfNotificationsUnprocessed);
        Mockito.when(staticJobAccessor.getPageOfJobs(Mockito.anyInt(), Mockito.anyInt())).thenReturn(AlertPagedModel.empty(0, 0));
        return new NotificationDiagnosticModel(numberOfNotifications, numberOfNotificationsProcessed, numberOfNotificationsUnprocessed, List.of());
    }

    private AuditDiagnosticModel createAuditDiagnosticModel() {
        long numberOfAuditEntriesSuccessful = 10L;
        long numberOfAuditEntriesFailed = 15L;
        long numberOfAuditEntriesPending = 20L;
        String averageAuditProcessingTime = AuditDiagnosticModel.NO_AUDIT_CONTENT_MESSAGE;
        Mockito.when(auditEntryRepository.countByStatus(AuditEntryStatus.SUCCESS.name())).thenReturn(numberOfAuditEntriesSuccessful);
        Mockito.when(auditEntryRepository.countByStatus(AuditEntryStatus.FAILURE.name())).thenReturn(numberOfAuditEntriesFailed);
        Mockito.when(auditEntryRepository.countByStatus(AuditEntryStatus.PENDING.name())).thenReturn(numberOfAuditEntriesPending);
        Mockito.when(auditEntryRepository.getAverageAuditEntryCompletionTime()).thenReturn(Optional.of(averageAuditProcessingTime));
        return new AuditDiagnosticModel(numberOfAuditEntriesSuccessful, numberOfAuditEntriesFailed, numberOfAuditEntriesPending, averageAuditProcessingTime);
    }

    private RabbitMQDiagnosticModel createRabbitMQDiagnosticModel() {
        AlertQueueInformation queue1 = new AlertQueueInformation("queue1", 50, 1);
        AlertQueueInformation queue2 = new AlertQueueInformation("queue2", 0, 50);
        RabbitMQDiagnosticModel rabbitMQDiagnosticModel = new RabbitMQDiagnosticModel(List.of(queue1, queue2));
        Mockito.when(rabbitMQDiagnosticUtility.getRabbitMQDiagnostics()).thenReturn(rabbitMQDiagnosticModel);
        return rabbitMQDiagnosticModel;
    }

    private CompletedJobsDiagnosticModel createJobDiagnosticModel() {
        UUID jobConfigId = UUID.randomUUID();
        Long notificationCount = 10L;
        Long successCount = 1L;
        Long failureCount = 0L;
        String latestStatus = AuditEntryStatus.SUCCESS.name();
        OffsetDateTime lastRun = DateUtils.createCurrentDateTimestamp();
        Long jobDuration = 100000L;
        JobExecutionStatusDurations durations = new JobExecutionStatusDurations(jobDuration, 1000000L, 300000L, 0L, 0L, 0L);

        JobExecutionStatusModel statusModel = new JobExecutionStatusModel(
            jobConfigId,
            notificationCount,
            notificationCount,
            successCount,
            failureCount,
            latestStatus,
            lastRun,
            durations
        );
        AlertPagedModel<JobExecutionStatusModel> pageModel = new AlertPagedModel<>(1, 0, 10, List.of(statusModel));
        Mockito.when(completedJobsAccessor.getJobExecutionStatus(Mockito.any(AlertPagedQueryDetails.class))).thenReturn(pageModel);
        DistributionJobModelBuilder jobModelBuilder = DistributionJobModel.builder()
            .jobId(UUID.randomUUID())
            .name(TEST_JOB_NAME)
            .processingType(ProcessingType.DEFAULT)
            .distributionFrequency(FrequencyType.REAL_TIME)
            .blackDuckGlobalConfigId(1L)
            .createdAt(OffsetDateTime.now())
            .channelDescriptorName(ChannelKeys.SLACK.getUniversalKey())
            .notificationTypes(List.of("VULNERABILITY"));

        Mockito.when(staticJobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(jobModelBuilder.build()));
        CompletedJobStageDurationModel firstStage = new CompletedJobStageDurationModel(
            JobStage.NOTIFICATION_PROCESSING.name(),
            DateUtils.formatDurationFromNanos(1000000L)
        );
        CompletedJobStageDurationModel secondStage = new CompletedJobStageDurationModel(
            JobStage.CHANNEL_PROCESSING.name(),
            DateUtils.formatDurationFromNanos(300000L)
        );
        CompletedJobStageDurationModel thirdStage = new CompletedJobStageDurationModel(
            JobStage.ISSUE_CREATION.name(),
            DateUtils.formatDurationFromNanos(0L)
        );
        CompletedJobStageDurationModel fourthStage = new CompletedJobStageDurationModel(
            JobStage.ISSUE_COMMENTING.name(),
            DateUtils.formatDurationFromNanos(0L)
        );
        CompletedJobStageDurationModel fifthStage = new CompletedJobStageDurationModel(
            JobStage.ISSUE_TRANSITION.name(),
            DateUtils.formatDurationFromNanos(0L)
        );

        CompletedJobDurationDiagnosticModel durationDisgnostics = new CompletedJobDurationDiagnosticModel(
            DateUtils.formatDurationFromNanos(jobDuration),
            List.of(firstStage, secondStage, thirdStage, fourthStage, fifthStage)
        );
        List<CompletedJobDiagnosticModel> jobs = List.of(new CompletedJobDiagnosticModel(
            jobConfigId,
            TEST_JOB_NAME,
            notificationCount,
            notificationCount,
            successCount,
            failureCount,
            latestStatus,
            DateUtils.formatDateAsJsonString(lastRun),
            durationDisgnostics
        ));
        return new CompletedJobsDiagnosticModel(jobs);
    }

    private void addJobExecutions() {
        UUID jobConfigId = UUID.randomUUID();
        int processedNotificationCount = 10;
        int totalNotificationCount = 100;

        ExecutingJob executingJob = executingJobManager.startJob(jobConfigId, totalNotificationCount);
        executingJob.updateNotificationCount(processedNotificationCount);
        UUID jobExecutionId = executingJob.getExecutionId();

        OffsetDateTime firstStageStart = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime firstStageEnd = DateUtils.createCurrentDateTimestamp();
        executingJobManager.startStage(jobExecutionId, JobStage.NOTIFICATION_PROCESSING, firstStageStart.toInstant());
        executingJobManager.endStage(jobExecutionId, JobStage.NOTIFICATION_PROCESSING, firstStageEnd.toInstant());

        OffsetDateTime secondStageStart = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime secondStageEnd = DateUtils.createCurrentDateTimestamp();
        executingJobManager.startStage(jobExecutionId, JobStage.NOTIFICATION_PROCESSING, secondStageStart.toInstant());
        executingJobManager.endStage(jobExecutionId, JobStage.NOTIFICATION_PROCESSING, secondStageEnd.toInstant());

        DistributionJobModelBuilder jobModelBuilder = DistributionJobModel.builder()
            .jobId(jobConfigId)
            .name(TEST_JOB_NAME)
            .processingType(ProcessingType.DEFAULT)
            .distributionFrequency(FrequencyType.REAL_TIME)
            .blackDuckGlobalConfigId(1L)
            .createdAt(OffsetDateTime.now())
            .channelDescriptorName(ChannelKeys.SLACK.getUniversalKey())
            .notificationTypes(List.of("VULNERABILITY"));

        DistributionJobModel distributionJobModel = jobModelBuilder.build();
        Mockito.when(staticJobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(distributionJobModel));
    }

    private void assertExecutingJobDetails(JobExecutionsDiagnosticModel jobExecutionsDiagnosticModel) {

    }

    private void assertSystemDiagnostics(SystemDiagnosticModel systemDiagnosticModel) {
        // System diagnostics entirely depend on the system running them
        assertTrue(systemDiagnosticModel.getAvailableProcessors() > 0);
        assertTrue(systemDiagnosticModel.getMaxMemory() > 0);
        assertTrue(systemDiagnosticModel.getTotalMemory() > 0);
        assertTrue(systemDiagnosticModel.getFreeMemory() > 0);
        assertTrue(systemDiagnosticModel.getUsedMemory() > 0);
    }
}
