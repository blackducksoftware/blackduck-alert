/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.diagnostic.database;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.DiagnosticAccessor;
import com.synopsys.integration.alert.component.diagnostic.model.AuditDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.DiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.NotificationDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.RabbitMQDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.model.SystemDiagnosticModel;
import com.synopsys.integration.alert.component.diagnostic.utility.RabbitMQDiagnosticUtility;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;

@Component
public class DefaultDiagnosticAccessor implements DiagnosticAccessor {
    private final NotificationContentRepository notificationContentRepository;
    private final AuditEntryRepository auditEntryRepository;
    private final RabbitMQDiagnosticUtility rabbitMQDiagnosticUtility;

    @Autowired
    public DefaultDiagnosticAccessor(
        NotificationContentRepository notificationContentRepository,
        AuditEntryRepository auditEntryRepository,
        RabbitMQDiagnosticUtility rabbitMQDiagnosticUtility
    ) {
        this.notificationContentRepository = notificationContentRepository;
        this.auditEntryRepository = auditEntryRepository;
        this.rabbitMQDiagnosticUtility = rabbitMQDiagnosticUtility;
    }

    @Override
    @Transactional(readOnly = true)
    public DiagnosticModel getDiagnosticInfo() {
        NotificationDiagnosticModel notificationDiagnosticModel = getNotificationDiagnosticInfo();
        AuditDiagnosticModel auditDiagnosticModel = getAuditDiagnosticInfo();
        SystemDiagnosticModel systemDiagnosticModel = getSystemInfo();
        RabbitMQDiagnosticModel rabbitMQDiagnosticModel = rabbitMQDiagnosticUtility.getRabbitMQDiagnostics();
        return new DiagnosticModel(LocalDateTime.now().toString(), notificationDiagnosticModel, auditDiagnosticModel, systemDiagnosticModel, rabbitMQDiagnosticModel);
    }

    private NotificationDiagnosticModel getNotificationDiagnosticInfo() {
        long numberOfNotifications = notificationContentRepository.count();
        long numberOfNotificationsProcessed = notificationContentRepository.countByProcessed(true);
        long numberOfNotificationsUnprocessed = notificationContentRepository.countByProcessed(false);
        return new NotificationDiagnosticModel(numberOfNotifications, numberOfNotificationsProcessed, numberOfNotificationsUnprocessed);
    }

    private AuditDiagnosticModel getAuditDiagnosticInfo() {
        long numberOfAuditEntriesSuccessful = auditEntryRepository.countByStatus(AuditEntryStatus.SUCCESS.name());
        long numberOfAuditEntriesFailed = auditEntryRepository.countByStatus(AuditEntryStatus.FAILURE.name());
        long numberOfAuditEntriesPending = auditEntryRepository.countByStatus(AuditEntryStatus.PENDING.name());
        return new AuditDiagnosticModel(
            numberOfAuditEntriesSuccessful,
            numberOfAuditEntriesFailed,
            numberOfAuditEntriesPending,
            auditEntryRepository.getAverageAuditEntryCompletionTime().orElse(AuditDiagnosticModel.NO_AUDIT_CONTENT_MESSAGE)
        );
    }

    private SystemDiagnosticModel getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        return new SystemDiagnosticModel(runtime.availableProcessors(), runtime.maxMemory(), runtime.totalMemory(), runtime.freeMemory());
    }
}
