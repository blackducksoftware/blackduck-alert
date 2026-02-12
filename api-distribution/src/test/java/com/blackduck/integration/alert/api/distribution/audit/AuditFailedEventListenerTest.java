/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
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
import org.springframework.amqp.core.Message;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJob;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.distribution.mock.MockAuditEntryRepository;
import com.blackduck.integration.alert.api.distribution.mock.MockAuditFailedEntryRepository;
import com.blackduck.integration.alert.api.distribution.mock.MockAuditFailedNotificationRepository;
import com.blackduck.integration.alert.api.distribution.mock.MockAuditNotificationRepository;
import com.blackduck.integration.alert.api.distribution.mock.MockJobCompletionStatusDurationsRepository;
import com.blackduck.integration.alert.api.distribution.mock.MockJobCompletionStatusRepository;
import com.blackduck.integration.alert.api.distribution.mock.MockNotificationBatchRepository;
import com.blackduck.integration.alert.api.distribution.mock.MockNotificationContentRepository;
import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.JobAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.JobCompletionStatusModelAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.ProcessingFailedAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModelBuilder;
import com.blackduck.integration.alert.common.persistence.model.job.executions.JobCompletionStatusModel;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.database.audit.AuditEntryEntity;
import com.blackduck.integration.alert.database.audit.AuditEntryRepository;
import com.blackduck.integration.alert.database.audit.AuditFailedEntity;
import com.blackduck.integration.alert.database.audit.AuditFailedEntryRepository;
import com.blackduck.integration.alert.database.audit.AuditFailedNotificationEntity;
import com.blackduck.integration.alert.database.audit.AuditFailedNotificationRepository;
import com.blackduck.integration.alert.database.audit.AuditNotificationRelation;
import com.blackduck.integration.alert.database.audit.AuditNotificationRelationPK;
import com.blackduck.integration.alert.database.audit.AuditNotificationRepository;
import com.blackduck.integration.alert.database.job.api.DefaultJobCompletionStatusModelAccessor;
import com.blackduck.integration.alert.database.job.api.DefaultNotificationAccessor;
import com.blackduck.integration.alert.database.job.api.DefaultProcessingFailedAccessor;
import com.blackduck.integration.alert.database.job.execution.JobCompletionDurationsRepository;
import com.blackduck.integration.alert.database.job.execution.JobCompletionRepository;
import com.blackduck.integration.alert.database.notification.NotificationBatchRepository;
import com.blackduck.integration.alert.database.notification.NotificationContentRepository;
import com.blackduck.integration.alert.database.notification.NotificationEntity;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;

class AuditFailedEventListenerTest {
    public static final String TEST_JOB_NAME = "Test Job";
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private final TaskExecutor taskExecutor = new SyncTaskExecutor();
    private ExecutingJobManager executingJobManager;
    private final AtomicLong idContainer = new AtomicLong(0L);

    private AuditFailedEntryRepository auditFailedEntryRepository;

    private NotificationBatchRepository notificationBatchRepository;
    private NotificationContentRepository notificationContentRepository;
    private final AtomicLong notificationIdContainer = new AtomicLong(0);
    private NotificationAccessor notificationAccessor;
    private AuditFailedNotificationRepository auditFailedNotificationRepository;
    private JobCompletionStatusModelAccessor jobCompletionStatusModelAccessor;

    @BeforeEach
    public void init() {
        AuditNotificationRepository auditNotificationRepository = new MockAuditNotificationRepository(this::generateRelationKey);
        AuditEntryRepository auditEntryRepository = new MockAuditEntryRepository(this::generateEntityKey, auditNotificationRepository);

        notificationBatchRepository = new MockNotificationBatchRepository();
        notificationContentRepository = new MockNotificationContentRepository(this::generateNotificationId);
        auditFailedEntryRepository = new MockAuditFailedEntryRepository(AuditFailedEntity::getId);
        auditFailedNotificationRepository = new MockAuditFailedNotificationRepository(AuditFailedNotificationEntity::getNotificationId);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        notificationAccessor = new DefaultNotificationAccessor(notificationContentRepository, auditEntryRepository, configurationModelConfigurationAccessor, notificationBatchRepository);
        JobCompletionDurationsRepository jobCompletionDurationsRepository = new MockJobCompletionStatusDurationsRepository();
        JobCompletionRepository jobCompletionRepository = new MockJobCompletionStatusRepository(jobCompletionDurationsRepository);

        jobCompletionStatusModelAccessor = new DefaultJobCompletionStatusModelAccessor(jobCompletionRepository, jobCompletionDurationsRepository);
        executingJobManager = new ExecutingJobManager(jobCompletionStatusModelAccessor);
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
    void onMessageTest() {
        UUID jobConfigId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        ExecutingJob executingJob = executingJobManager.startJob(jobConfigId, notificationIds.size());
        UUID executingJobId = executingJob.getExecutionId();
        String errorMessage = "Error message";
        String stackTrace = "Stack trace goes here";

        JobAccessor jobAccessor = createJobAccessor(this::createJobModel);
        ProcessingFailedAccessor processingFailedAccessor = new DefaultProcessingFailedAccessor(
            auditFailedEntryRepository,
            auditFailedNotificationRepository,
            notificationAccessor,
            jobAccessor
        );
        AuditFailedHandler handler = new AuditFailedHandler(processingFailedAccessor, executingJobManager);

        notificationIds.stream()
            .map(this::createNotification)
            .forEach(notificationContentRepository::save);
        AuditFailedEventListener listener = new AuditFailedEventListener(gson, taskExecutor, handler);
        AuditFailedEvent event = new AuditFailedEvent(executingJobId, jobConfigId, notificationIds, errorMessage, stackTrace);
        Message message = new Message(gson.toJson(event).getBytes());
        listener.onMessage(message);

        List<AuditFailedEntity> failedEntities = auditFailedEntryRepository.findAll();
        for (AuditFailedEntity entity : failedEntities) {
            assertNotNull(entity.getId());
            assertNotNull(entity.getProviderName());
            assertEquals(TEST_JOB_NAME, entity.getJobName());
            assertEquals(ChannelKeys.SLACK.getUniversalKey(), entity.getChannelName());
            assertEquals(DateUtils.fromInstantUTC(Instant.ofEpochMilli(event.getCreatedTimestamp())), entity.getCreatedAt());
            assertEquals(errorMessage, entity.getErrorMessage());
            assertEquals(stackTrace, entity.getErrorStackTrace().orElseThrow(() -> new AssertionError("Expected stack trace but none found")));
        }

        JobCompletionStatusModel statusModel = jobCompletionStatusModelAccessor.getJobExecutionStatus(jobConfigId)
            .orElseThrow(() -> new AssertionError("Executing Job cannot be missing from the test."));
        assertEquals(AuditEntryStatus.FAILURE.name(), statusModel.getLatestStatus());
        assertEquals(0, statusModel.getSuccessCount());
        assertEquals(1, statusModel.getFailureCount());
        assertEquals(0, statusModel.getTotalNotificationCount());
        assertTrue(executingJobManager.getExecutingJob(executingJobId).isEmpty());
    }

    private DistributionJobModel createJobModel(UUID jobId) {
        OffsetDateTime createdAt = DateUtils.createCurrentDateTimestamp();
        Long blackDuckGlobalConfigId = 1L;
        List<String> notificationTypes = List.of("VULNERABILITY");
        DistributionJobModelBuilder jobBuilder = new DistributionJobModelBuilder();
        jobBuilder.jobId(jobId)
            .name(TEST_JOB_NAME)
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
            false,
            String.format("content-id-%s", UUID.randomUUID()),
            false
        );
    }

}
