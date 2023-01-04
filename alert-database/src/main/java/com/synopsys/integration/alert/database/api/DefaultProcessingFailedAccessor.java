package com.synopsys.integration.alert.database.api;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingFailedAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.database.audit.AuditFailedEntity;
import com.synopsys.integration.alert.database.audit.AuditFailedEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditFailedNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditFailedNotificationRepository;

@Component
public class DefaultProcessingFailedAccessor implements ProcessingFailedAccessor {

    private final AuditFailedEntryRepository auditFailedEntryRepository;
    private final AuditFailedNotificationRepository auditFailedNotificationRepository;
    private final NotificationAccessor notificationAccessor;

    @Autowired
    public DefaultProcessingFailedAccessor(
        final AuditFailedEntryRepository auditFailedEntryRepository,
        final AuditFailedNotificationRepository auditFailedNotificationRepository,
        final NotificationAccessor notificationAccessor
    ) {
        this.auditFailedEntryRepository = auditFailedEntryRepository;
        this.auditFailedNotificationRepository = auditFailedNotificationRepository;
        this.notificationAccessor = notificationAccessor;
    }

    @Override
    @Transactional
    public void setAuditFailure(UUID jobId, Set<Long> notificationIds, OffsetDateTime occurrence, String errorMessage) {
        List<AlertNotificationModel> notificationModels = notificationAccessor.findByIds(new ArrayList<>(notificationIds));
        for (AlertNotificationModel notificationModel : notificationModels) {
            AuditFailedEntity auditFailedEntity = new AuditFailedEntity(
                UUID.randomUUID(),
                occurrence,
                jobId,
                notificationModel.getProviderConfigId(),
                notificationModel.getNotificationType(),
                errorMessage
            );
            auditFailedEntity = auditFailedEntryRepository.save(auditFailedEntity);
            auditFailedNotificationRepository.save(new AuditFailedNotificationRelation(auditFailedEntity.getId(), notificationModel.getId()));
        }
    }

    @Override
    @Transactional
    public void setAuditFailure(UUID jobId, Set<Long> notificationIds, OffsetDateTime occurrence, String errorMessage, String stackTrace) {
        List<AlertNotificationModel> notificationModels = notificationAccessor.findByIds(new ArrayList<>(notificationIds));
        for (AlertNotificationModel notificationModel : notificationModels) {
            AuditFailedEntity auditFailedEntity = new AuditFailedEntity(
                UUID.randomUUID(),
                occurrence,
                jobId,
                notificationModel.getProviderConfigId(),
                notificationModel.getNotificationType(),
                errorMessage,
                stackTrace
            );
            auditFailedEntity = auditFailedEntryRepository.save(auditFailedEntity);
            auditFailedNotificationRepository.save(new AuditFailedNotificationRelation(auditFailedEntity.getId(), notificationModel.getId()));
        }
    }
}
