package com.synopsys.integration.alert.component.diagnostic.database;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.DiagnosticAccessor;
import com.synopsys.integration.alert.component.diagnostic.model.DiagnosticModel;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;

@Component
public class DefaultDiagnosticAccessor implements DiagnosticAccessor {
    private final NotificationContentRepository notificationContentRepository;
    private final AuditEntryRepository auditEntryRepository;

    @Autowired
    public DefaultDiagnosticAccessor(NotificationContentRepository notificationContentRepository, AuditEntryRepository auditEntryRepository) {
        this.notificationContentRepository = notificationContentRepository;
        this.auditEntryRepository = auditEntryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DiagnosticModel getDiagnosticInfo() {
        long numberOfNotifications = notificationContentRepository.count();
        long numberOfNotificationsProcessed = notificationContentRepository.countByProcessed(true);
        long numberOfNotificationsUnprocessed = notificationContentRepository.countByProcessed(false);

        long numberOfAuditEntriesSuccessful = auditEntryRepository.countByStatus(AuditEntryStatus.SUCCESS.name());
        long numberOfAuditEntriesFailed = auditEntryRepository.countByStatus(AuditEntryStatus.FAILURE.name());
        long numberOfAuditEntriesPending = auditEntryRepository.countByStatus(AuditEntryStatus.PENDING.name());

        return new DiagnosticModel(
            numberOfNotifications,
            numberOfNotificationsProcessed,
            numberOfNotificationsUnprocessed,
            numberOfAuditEntriesSuccessful,
            numberOfAuditEntriesFailed,
            numberOfAuditEntriesPending,
            LocalDateTime.now().toString(),
            auditEntryRepository.getAverageAuditEntryCompletionTime().orElse(null)
        );
    }
}
