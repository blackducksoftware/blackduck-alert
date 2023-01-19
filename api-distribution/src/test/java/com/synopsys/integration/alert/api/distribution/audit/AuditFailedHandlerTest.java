package com.synopsys.integration.alert.api.distribution.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.distribution.mock.MockAuditEntryRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockAuditFailedEntryRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockAuditFailedNotificationRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockAuditNotificationRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockJobExecutionStatusDurationsRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockJobExecutionStatusRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockNotificationContentRepository;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobExecutionStatusAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingFailedAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModelBuilder;
import com.synopsys.integration.alert.common.persistence.model.job.executions.JobExecutionStatusModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.DefaultJobExecutionStatusAccessor;
import com.synopsys.integration.alert.database.api.DefaultNotificationAccessor;
import com.synopsys.integration.alert.database.api.DefaultProcessingFailedAccessor;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditFailedEntity;
import com.synopsys.integration.alert.database.audit.AuditFailedEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditFailedNotificationEntity;
import com.synopsys.integration.alert.database.audit.AuditFailedNotificationRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelationPK;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.job.execution.JobExecutionDurationsRepository;
import com.synopsys.integration.alert.database.job.execution.JobExecutionRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.database.notification.NotificationEntity;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

class AuditFailedHandlerTest {
    public static final String TEST_JOB_NAME = "Test Job";

    private ExecutingJobManager executingJobManager;
    private final AtomicLong idContainer = new AtomicLong(0L);

    private AuditFailedEntryRepository auditFailedEntryRepository;
    private AuditFailedNotificationRepository auditFailedNotificationRepository;

    private NotificationContentRepository notificationContentRepository;
    private NotificationAccessor notificationAccessor;

    private JobExecutionStatusAccessor jobExecutionStatusAccessor;

    private final AtomicLong notificationIdContainer = new AtomicLong(0);

    @BeforeEach
    public void init() {
        AuditNotificationRepository auditNotificationRepository = new MockAuditNotificationRepository(this::generateRelationKey);
        AuditEntryRepository auditEntryRepository = new MockAuditEntryRepository(this::generateEntityKey, auditNotificationRepository);
        executingJobManager = new ExecutingJobManager();
        notificationContentRepository = new MockNotificationContentRepository(this::generateNotificationId);
        auditFailedEntryRepository = new MockAuditFailedEntryRepository(AuditFailedEntity::getId);
        auditFailedNotificationRepository = new MockAuditFailedNotificationRepository(AuditFailedNotificationEntity::getNotificationId);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        notificationAccessor = new DefaultNotificationAccessor(notificationContentRepository, auditEntryRepository, configurationModelConfigurationAccessor);
        JobExecutionDurationsRepository jobExecutionDurationsRepository = new MockJobExecutionStatusDurationsRepository();
        JobExecutionRepository jobExecutionRepository = new MockJobExecutionStatusRepository(jobExecutionDurationsRepository);

        jobExecutionStatusAccessor = new DefaultJobExecutionStatusAccessor(jobExecutionRepository, jobExecutionDurationsRepository);
    }

    private Long generateNotificationId(NotificationEntity entity) {
        Long id = entity.getId();
        if (null == id) {
            id = notificationIdContainer.incrementAndGet();
            entity.setId(id);
        }
        return id;
    }

    private Long generateEntityKey(AuditEntryEntity entity) {
        Long id = entity.getId();
        if (null == id) {
            id = idContainer.incrementAndGet();
            entity.setId(id);
        }
        return id;
    }

    private AuditNotificationRelationPK generateRelationKey(AuditNotificationRelation relation) {
        AuditNotificationRelationPK key = new AuditNotificationRelationPK();
        key.setAuditEntryId(relation.getAuditEntryId());
        key.setNotificationId(relation.getNotificationId());
        return key;
    }

    @Test
    void handleEventTest() {
        JobAccessor jobAccessor = createJobAccessor(this::createJobModel);
        ProcessingFailedAccessor processingFailedAccessor = new DefaultProcessingFailedAccessor(
            auditFailedEntryRepository,
            auditFailedNotificationRepository,
            notificationAccessor,
            jobAccessor
        );
        UUID jobExecutionId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        ExecutingJob executingJob = executingJobManager.startJob(jobExecutionId, notificationIds.size());
        String errorMessage = "Error message";
        String stackTrace = "Stack trace goes here";
        UUID executingJobId = executingJob.getExecutionId();

        AuditFailedHandler handler = new AuditFailedHandler(processingFailedAccessor, executingJobManager, jobExecutionStatusAccessor);
        notificationIds.stream()
            .map(this::createNotification)
            .forEach(notificationContentRepository::save);
        AuditFailedEvent event = new AuditFailedEvent(executingJobId, notificationIds, errorMessage, stackTrace);

        handler.handle(event);

        List<AuditFailedEntity> failedEntities = auditFailedEntryRepository.findAll();
        for (AuditFailedEntity entity : failedEntities) {
            assertNotNull(entity.getId());
            assertNotNull(entity.getProviderName());
            assertEquals(TEST_JOB_NAME, entity.getJobName());
            assertEquals(ChannelKeys.SLACK.getUniversalKey(), entity.getChannelName());
            assertEquals(event.getCreatedTimestamp(), entity.getTimeCreated());
            assertEquals(errorMessage, entity.getErrorMessage());
            assertEquals(stackTrace, entity.getErrorStackTrace().orElseThrow(() -> new AssertionError("Expected stack trace but none found")));
        }

        JobExecutionStatusModel statusModel = jobExecutionStatusAccessor.getJobExecutionStatus(jobExecutionId)
            .orElseThrow(() -> new AssertionError("Executing Job cannot be missing from the test."));
        assertEquals(AuditEntryStatus.FAILURE.name(), statusModel.getLatestStatus());
        assertEquals(0, statusModel.getSuccessCount());
        assertEquals(1, statusModel.getFailureCount());
        assertEquals(0, statusModel.getNotificationCount());
        assertTrue(executingJobManager.getExecutingJob(executingJobId).isEmpty());

    }

    @Test
    void handleEventAuditEntryMissingTest() {
        JobAccessor jobAccessor = createJobAccessor(this::createJobModel);
        ProcessingFailedAccessor processingFailedAccessor = new DefaultProcessingFailedAccessor(
            auditFailedEntryRepository,
            auditFailedNotificationRepository,
            notificationAccessor,
            jobAccessor
        );
        UUID jobExecutionId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        String errorMessage = "Error message";
        String stackTrace = "Stack trace goes here";

        notificationIds.stream()
            .map(this::createNotification)
            .forEach(notificationContentRepository::save);
        AuditFailedHandler handler = new AuditFailedHandler(processingFailedAccessor, executingJobManager, jobExecutionStatusAccessor);
        AuditFailedEvent event = new AuditFailedEvent(jobExecutionId, notificationIds, errorMessage, stackTrace);

        handler.handle(event);

        List<AuditFailedEntity> failedEntities = auditFailedEntryRepository.findAll();
        for (AuditFailedEntity entity : failedEntities) {
            assertNotNull(entity.getId());
            assertNotNull(entity.getProviderName());
            assertEquals(TEST_JOB_NAME, entity.getJobName());
            assertEquals(ChannelKeys.SLACK.getUniversalKey(), entity.getChannelName());
            assertEquals(event.getCreatedTimestamp(), entity.getTimeCreated());
            assertEquals(errorMessage, entity.getErrorMessage());
            assertEquals(stackTrace, entity.getErrorStackTrace().orElseThrow(() -> new AssertionError("Expected stack trace but none found")));
        }
        assertTrue(jobExecutionStatusAccessor.getJobExecutionStatus(jobExecutionId).isEmpty());
        assertTrue(executingJobManager.getExecutingJob(jobExecutionId).isEmpty());
    }

    private DistributionJobModel createJobModel(UUID jobId) {
        String name = TEST_JOB_NAME;
        OffsetDateTime createdAt = DateUtils.createCurrentDateTimestamp();
        Long blackDuckGlobalConfigId = 1L;
        List<String> notificationTypes = List.of("VULNERABILITY");
        DistributionJobModelBuilder jobBuilder = new DistributionJobModelBuilder();
        jobBuilder.jobId(jobId)
            .name(name)
            .createdAt(createdAt)
            .blackDuckGlobalConfigId(blackDuckGlobalConfigId)
            .distributionFrequency(FrequencyType.REAL_TIME)
            .processingType(ProcessingType.DEFAULT)
            .channelDescriptorName(ChannelKeys.SLACK.getUniversalKey())
            .notificationTypes(notificationTypes);

        return jobBuilder.build();
    }

    private JobAccessor createJobAccessor(Function<UUID, DistributionJobModel> jobModelSupplier) {
        JobAccessor accessor = Mockito.mock(JobAccessor.class);
        Mockito.doAnswer(invocation -> {
            UUID jobId = invocation.getArgument(0);
            return Optional.ofNullable(jobModelSupplier.apply(jobId));
        }).when(accessor).getJobById(Mockito.any());
        return accessor;
    }

    private NotificationEntity createNotification(Long id) {
        String provider = "Provider";
        String content = "notification content";
        OffsetDateTime creationTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime providerCreationTime = creationTime.minus(1, ChronoUnit.MINUTES);
        return new NotificationEntity(
            id,
            creationTime,
            provider,
            1L,
            providerCreationTime,
            "VULNERABILITY",
            content,
            false
        );
    }
}
