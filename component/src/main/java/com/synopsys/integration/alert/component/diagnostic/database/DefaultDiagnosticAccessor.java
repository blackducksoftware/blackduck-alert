package com.synopsys.integration.alert.component.diagnostic.database;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.DiagnosticAccessor;
import com.synopsys.integration.alert.component.diagnostic.model.DiagnosticModel;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;

@Component
public class DefaultDiagnosticAccessor implements DiagnosticAccessor {
    private final NotificationContentRepository notificationContentRepository;

    @Autowired
    public DefaultDiagnosticAccessor(NotificationContentRepository notificationContentRepository) {
        this.notificationContentRepository = notificationContentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DiagnosticModel getDiagnosticInfo() {
        long numberOfNotifications = notificationContentRepository.count();
        long numberOfNotificationsProcessed = notificationContentRepository.countByProcessedIsTrue();
        long numberOfNotificationsUnprocessed = notificationContentRepository.countByProcessedIsFalse();

        return new DiagnosticModel(numberOfNotifications, numberOfNotificationsProcessed, numberOfNotificationsUnprocessed, LocalDateTime.now().toString());
    }
}
