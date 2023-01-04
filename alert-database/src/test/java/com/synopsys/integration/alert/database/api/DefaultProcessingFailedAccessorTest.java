package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingFailedAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.mock.MockAuditFailedEntryRepository;
import com.synopsys.integration.alert.database.api.mock.MockAuditFailedNotificationRepository;
import com.synopsys.integration.alert.database.audit.AuditFailedEntity;
import com.synopsys.integration.alert.database.audit.AuditFailedEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditFailedNotificationRepository;

class DefaultProcessingFailedAccessorTest {
    private AuditFailedEntryRepository auditFailedEntryRepository;
    private AuditFailedNotificationRepository auditFailedNotificationRepository;
    private ProcessingFailedAccessor processingFailedAccessor;

    private final List<Long> validNotificationIds = List.of(1L, 2L, 3L);

    private final Long providerConfigId = 1L;

    @BeforeEach
    public void initializeRepositories() {
        auditFailedEntryRepository = new MockAuditFailedEntryRepository(AuditFailedEntity::getId);
        auditFailedNotificationRepository = new MockAuditFailedNotificationRepository(MockAuditFailedNotificationRepository::generateRelationKey);
        NotificationAccessor notificationAccessor = createNotificationAccessor();

        processingFailedAccessor = new DefaultProcessingFailedAccessor(auditFailedEntryRepository, auditFailedNotificationRepository, notificationAccessor);
    }

    @Test
    void failureWithErrorMessageTest() {
        UUID jobId = UUID.randomUUID();
        OffsetDateTime occurence = DateUtils.createCurrentDateTimestamp();
        String errorMessage = "Error Message";
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        processingFailedAccessor.setAuditFailure(jobId, notificationIds, occurence, errorMessage);

        assertEquals(notificationIds.size(), auditFailedEntryRepository.count(), "wrong audit failure count");
        assertEquals(notificationIds.size(), auditFailedNotificationRepository.count(), "wrong audit failure notification relation count");
        for (AuditFailedEntity entity : auditFailedEntryRepository.findAll()) {
            assertNotNull(entity.getId());
            assertEquals(jobId, entity.getJobConfigId());
            assertEquals(providerConfigId, entity.getProviderId());
            assertEquals(errorMessage, entity.getErrorMessage());
            assertNotNull(entity.getNotificationType());
            assertTrue(entity.getErrorStackTrace().isEmpty());
        }
    }

    @Test
    void failureWithEmptyNotificationSetErrorMessageTest() {
        UUID jobId = UUID.randomUUID();
        OffsetDateTime occurence = DateUtils.createCurrentDateTimestamp();
        String errorMessage = "Error Message";
        processingFailedAccessor.setAuditFailure(jobId, Set.of(), occurence, errorMessage);

        assertEquals(0, auditFailedEntryRepository.count(), "wrong audit failure count");
        assertEquals(0, auditFailedNotificationRepository.count(), "wrong audit failure notification relation count");
    }

    @Test
    void failureWithStackTrace() {
        UUID jobId = UUID.randomUUID();
        OffsetDateTime occurence = DateUtils.createCurrentDateTimestamp();
        String errorMessage = "Error Message";
        String stackTrace = "stack trace";
        Set<Long> notificationIds = Set.of(1L, 2L, 3L, 4L);
        processingFailedAccessor.setAuditFailure(jobId, notificationIds, occurence, errorMessage, stackTrace);

        assertEquals(notificationIds.size(), auditFailedEntryRepository.count(), "wrong audit failure count");
        assertEquals(notificationIds.size(), auditFailedNotificationRepository.count(), "wrong audit failure notification relation count");
        for (AuditFailedEntity entity : auditFailedEntryRepository.findAll()) {
            assertNotNull(entity.getId());
            assertEquals(jobId, entity.getJobConfigId());
            assertEquals(providerConfigId, entity.getProviderId());
            assertEquals(errorMessage, entity.getErrorMessage());
            assertNotNull(stackTrace, entity.getErrorStackTrace().orElseThrow(() -> new AssertionError("Expected a value for stack trace and none found")));
            assertNotNull(entity.getNotificationType());
        }

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
        String providerName = "My Provider";
        String content = "notification content";
        OffsetDateTime creationTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime providerCreationTime = creationTime.minus(1, ChronoUnit.MINUTES);
        return new AlertNotificationModel(
            notificationId,
            providerConfigId,
            provider,
            providerName,
            "VULNERABILITY",
            content,
            creationTime,
            providerCreationTime,
            false
        );
    }
}
