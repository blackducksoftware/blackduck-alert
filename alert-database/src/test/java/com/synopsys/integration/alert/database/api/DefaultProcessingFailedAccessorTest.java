package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingFailedAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModelBuilder;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.mock.MockAuditFailedEntryRepository;
import com.synopsys.integration.alert.database.api.mock.MockAuditFailedNotificationRepository;
import com.synopsys.integration.alert.database.audit.AuditFailedEntity;
import com.synopsys.integration.alert.database.audit.AuditFailedEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditFailedNotificationRepository;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

class DefaultProcessingFailedAccessorTest {
    public static final String TEST_JOB_NAME = "Test Job";
    public static final String TEST_PROVIDER_NAME = "My Provider";
    private AuditFailedEntryRepository auditFailedEntryRepository;
    private AuditFailedNotificationRepository auditFailedNotificationRepository;
    private NotificationAccessor notificationAccessor;

    @BeforeEach
    public void initializeRepositories() {
        auditFailedEntryRepository = new MockAuditFailedEntryRepository(AuditFailedEntity::getId);
        auditFailedNotificationRepository = new MockAuditFailedNotificationRepository(MockAuditFailedNotificationRepository::generateRelationKey);
        notificationAccessor = createNotificationAccessor();
    }

    @Test
    void failureWithErrorMessageTest() {
        JobAccessor jobAccessor = createJobAccessor(this::createJobModel);
        ProcessingFailedAccessor processingFailedAccessor = new DefaultProcessingFailedAccessor(
            auditFailedEntryRepository,
            auditFailedNotificationRepository,
            notificationAccessor,
            jobAccessor
        );
        UUID jobId = UUID.randomUUID();
        OffsetDateTime occurence = DateUtils.createCurrentDateTimestamp();
        String errorMessage = "Error Message";
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        processingFailedAccessor.setAuditFailure(jobId, notificationIds, occurence, errorMessage);

        assertEquals(notificationIds.size(), auditFailedEntryRepository.count(), "wrong audit failure count");
        assertEquals(notificationIds.size(), auditFailedNotificationRepository.count(), "wrong audit failure notification relation count");
        for (AuditFailedEntity entity : auditFailedEntryRepository.findAll()) {
            assertNotNull(entity.getId());
            assertEquals(TEST_JOB_NAME, entity.getJobName());
            assertEquals(TEST_PROVIDER_NAME, entity.getProviderName());
            assertEquals(errorMessage, entity.getErrorMessage());
            assertNotNull(entity.getNotificationType());
            assertTrue(entity.getErrorStackTrace().isEmpty());
        }
    }

    @Test
    void failureWithEmptyNotificationSetErrorMessageTest() {
        UUID jobId = UUID.randomUUID();
        JobAccessor jobAccessor = createJobAccessor(this::createJobModel);
        ProcessingFailedAccessor processingFailedAccessor = new DefaultProcessingFailedAccessor(
            auditFailedEntryRepository,
            auditFailedNotificationRepository,
            notificationAccessor,
            jobAccessor
        );
        OffsetDateTime occurence = DateUtils.createCurrentDateTimestamp();
        String errorMessage = "Error Message";
        processingFailedAccessor.setAuditFailure(jobId, Set.of(), occurence, errorMessage);

        assertEquals(0, auditFailedEntryRepository.count(), "wrong audit failure count");
        assertEquals(0, auditFailedNotificationRepository.count(), "wrong audit failure notification relation count");
    }

    @Test
    void failureWithStackTrace() {
        UUID jobId = UUID.randomUUID();
        JobAccessor jobAccessor = createJobAccessor(this::createJobModel);
        ProcessingFailedAccessor processingFailedAccessor = new DefaultProcessingFailedAccessor(
            auditFailedEntryRepository,
            auditFailedNotificationRepository,
            notificationAccessor,
            jobAccessor
        );
        OffsetDateTime occurence = DateUtils.createCurrentDateTimestamp();
        String errorMessage = "Error Message";
        String stackTrace = "stack trace";
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);
        processingFailedAccessor.setAuditFailure(jobId, notificationIds, occurence, errorMessage, stackTrace);

        assertEquals(notificationIds.size(), auditFailedEntryRepository.count(), "wrong audit failure count");
        assertEquals(notificationIds.size(), auditFailedNotificationRepository.count(), "wrong audit failure notification relation count");
        for (AuditFailedEntity entity : auditFailedEntryRepository.findAll()) {
            assertNotNull(entity.getId());
            assertEquals(TEST_JOB_NAME, entity.getJobName());
            assertEquals(TEST_PROVIDER_NAME, entity.getProviderName());
            assertEquals(errorMessage, entity.getErrorMessage());
            assertNotNull(stackTrace, entity.getErrorStackTrace().orElseThrow(() -> new AssertionError("Expected a value for stack trace and none found")));
            assertNotNull(entity.getNotificationType());
        }

    }

    private DistributionJobModel createJobModel(UUID jobId) {
        String name = TEST_JOB_NAME;
        OffsetDateTime createdAt = DateUtils.createCurrentDateTimestamp();
        Long blackDuckGlobalConfigId = 1L;
        List<String> notificationTypes = Arrays.stream(NotificationType.values())
            .map(NotificationType::name)
            .collect(Collectors.toList());
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

    private NotificationAccessor createNotificationAccessor() {
        NotificationAccessor notificationAccessor = Mockito.mock(NotificationAccessor.class);
        Mockito.doAnswer(invocation -> {
            List<Long> notificationIds = invocation.getArgument(0);
            return notificationIds.stream()
                .map(notificationId -> createNotification(notificationId))
                .collect(Collectors.toList());
        }).when(notificationAccessor).findByIds(Mockito.any());
        return notificationAccessor;
    }

    private AlertNotificationModel createNotification(Long notificationId) {
        String provider = "Provider";
        String content = "notification content";
        OffsetDateTime creationTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime providerCreationTime = creationTime.minus(1, ChronoUnit.MINUTES);
        return new AlertNotificationModel(
            notificationId,
            1L,
            provider,
            TEST_PROVIDER_NAME,
            "VULNERABILITY",
            content,
            creationTime,
            providerCreationTime,
            false
        );
    }
}
