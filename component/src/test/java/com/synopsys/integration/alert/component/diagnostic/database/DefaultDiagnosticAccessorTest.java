package com.synopsys.integration.alert.component.diagnostic.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.component.diagnostic.model.AlertQueueInformation;
import com.synopsys.integration.alert.component.diagnostic.model.AuditDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.DiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.NotificationDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.RabbitMQDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.SystemDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.utility.RabbitMQDiagnosticUtility;
import com.synopsys.integration.alert.database.api.StaticJobAccessor;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;

class DefaultDiagnosticAccessorTest {
    private NotificationContentRepository notificationContentRepository;
    private AuditEntryRepository auditEntryRepository;
    private RabbitMQDiagnosticUtility rabbitMQDiagnosticUtility;
    private ExecutingJobManager executingJobManager;
    private StaticJobAccessor staticJobAccessor;

    @BeforeEach
    public void init() {
        notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        rabbitMQDiagnosticUtility = Mockito.mock(RabbitMQDiagnosticUtility.class);
        staticJobAccessor = Mockito.mock(StaticJobAccessor.class);
        executingJobManager = new ExecutingJobManager();
    }

    @Test
    void testGetDiagnosticInfo() {
        DefaultDiagnosticAccessor diagnosticAccessor = new DefaultDiagnosticAccessor(
            notificationContentRepository,
            auditEntryRepository,
            rabbitMQDiagnosticUtility,
            staticJobAccessor,
            executingJobManager
        );
        NotificationDiagnosticModel notificationDiagnosticModel = createNotificationDiagnosticModel();
        AuditDiagnosticModel auditDiagnosticModel = createAuditDiagnosticModel();
        RabbitMQDiagnosticModel rabbitMQDiagnosticModel = createRabbitMQDiagnosticModel();
        DiagnosticModel diagnosticModel = diagnosticAccessor.getDiagnosticInfo();

        assertEquals(notificationDiagnosticModel, diagnosticModel.getNotificationDiagnosticModel());
        assertEquals(auditDiagnosticModel, diagnosticModel.getAuditDiagnosticModel());
        assertEquals(rabbitMQDiagnosticModel, diagnosticModel.getRabbitMQDiagnosticModel());
        assertSystemDiagnostics(diagnosticModel.getSystemDiagnosticModel());
    }

    private NotificationDiagnosticModel createNotificationDiagnosticModel() {
        long numberOfNotifications = 10L;
        long numberOfNotificationsProcessed = 5L;
        long numberOfNotificationsUnprocessed = 5L;
        Mockito.when(notificationContentRepository.count()).thenReturn(numberOfNotifications);
        Mockito.when(notificationContentRepository.countByProcessed(true)).thenReturn(numberOfNotificationsProcessed);
        Mockito.when(notificationContentRepository.countByProcessed(false)).thenReturn(numberOfNotificationsUnprocessed);
        return new NotificationDiagnosticModel(numberOfNotifications, numberOfNotificationsProcessed, numberOfNotificationsUnprocessed);
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

    private void assertSystemDiagnostics(SystemDiagnosticModel systemDiagnosticModel) {
        // System diagnostics entirely depend on the system running them
        assertTrue(systemDiagnosticModel.getAvailableProcessors() > 0);
        assertTrue(systemDiagnosticModel.getMaxMemory() > 0);
        assertTrue(systemDiagnosticModel.getTotalMemory() > 0);
        assertTrue(systemDiagnosticModel.getFreeMemory() > 0);
        assertTrue(systemDiagnosticModel.getUsedMemory() > 0);
    }
}
