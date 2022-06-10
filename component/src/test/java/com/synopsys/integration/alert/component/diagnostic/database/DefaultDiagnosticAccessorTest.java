package com.synopsys.integration.alert.component.diagnostic.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.component.diagnostic.model.AuditDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.DiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.NotificationDiagnosticModel;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;

class DefaultDiagnosticAccessorTest {
    private NotificationContentRepository notificationContentRepository;
    private AuditEntryRepository auditEntryRepository;

    @BeforeEach
    public void init() {
        notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
    }

    @Test
    void testGetDiagnosticInfo() {
        DefaultDiagnosticAccessor diagnosticAccessor = new DefaultDiagnosticAccessor(notificationContentRepository, auditEntryRepository);
        NotificationDiagnosticModel notificationDiagnosticModel = createNotificationDiagnosticModel();
        AuditDiagnosticModel auditDiagnosticModel = createAuditDiagnosticModel();
        DiagnosticModel diagnosticModel = diagnosticAccessor.getDiagnosticInfo();

        assertEquals(notificationDiagnosticModel, diagnosticModel.getNotificationDiagnosticModel());
        assertEquals(auditDiagnosticModel, diagnosticModel.getAuditDiagnosticModel());
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
}
