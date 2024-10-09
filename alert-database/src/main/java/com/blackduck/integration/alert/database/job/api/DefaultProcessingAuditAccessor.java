/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.api;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackduck.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.blackduck.integration.alert.common.persistence.util.AuditStackTraceUtil;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.database.audit.AuditEntryEntity;
import com.blackduck.integration.alert.database.audit.AuditEntryNotificationView;
import com.blackduck.integration.alert.database.audit.AuditEntryRepository;
import com.blackduck.integration.alert.database.audit.AuditNotificationRelation;
import com.blackduck.integration.alert.database.audit.AuditNotificationRepository;

@Component
public class DefaultProcessingAuditAccessor implements ProcessingAuditAccessor {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;

    @Autowired
    public DefaultProcessingAuditAccessor(AuditEntryRepository auditEntryRepository, AuditNotificationRepository auditNotificationRepository) {
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
    }

    @Override
    @Transactional
    public void createOrUpdatePendingAuditEntryForJob(UUID jobId, Set<Long> notificationIds) {
        if (notificationIds.isEmpty()) {
            return;
        }

        Map<Long, AuditEntryNotificationView> notificationIdToView = auditEntryRepository.findByJobIdAndNotificationIds(jobId, notificationIds)
                                                                         .stream()
                                                                         .collect(Collectors.toMap(AuditEntryNotificationView::getNotificationId, Function.identity()));

        Set<AuditNotificationRelation> relationsToUpdate = new HashSet<>();
        for (Long notificationId : notificationIds) {
            AuditEntryEntity auditEntryToSave;
            AuditEntryNotificationView view = notificationIdToView.get(notificationId);
            if (null != view) {
                auditEntryToSave = fromView(view);
            } else {
                auditEntryToSave = new AuditEntryEntity(jobId, DateUtils.createCurrentDateTimestamp(), null, AuditEntryStatus.PENDING.name(), null, null);
            }

            AuditEntryEntity savedAuditEntry = auditEntryRepository.save(auditEntryToSave);
            logger.trace("Created audit entry: {}. For notification: {}", savedAuditEntry.getId(), notificationId);

            AuditNotificationRelation auditNotificationRelation = new AuditNotificationRelation(savedAuditEntry.getId(), notificationId);
            relationsToUpdate.add(auditNotificationRelation);
        }
        auditNotificationRepository.saveAll(relationsToUpdate);
    }

    @Override
    @Transactional
    public void setAuditEntrySuccess(UUID jobId, Set<Long> notificationIds) {
        updateAuditEntries(jobId, notificationIds, auditEntry -> {
            auditEntry.setStatus(AuditEntryStatus.SUCCESS.name());
            auditEntry.setErrorMessage(null);
            auditEntry.setErrorStackTrace(null);
        });
    }

    @Override
    @Transactional
    public void setAuditEntrySuccess(UUID jobId, Set<Long> notificationIds, OffsetDateTime successTimestamp) {
        updateAuditEntries(jobId, notificationIds, auditEntry -> {
            auditEntry.setStatus(AuditEntryStatus.SUCCESS.name());
            auditEntry.setTimeLastSent(successTimestamp);
            auditEntry.setErrorMessage(null);
            auditEntry.setErrorStackTrace(null);
        });
    }

    @Override
    @Transactional
    public void setAuditEntryFailure(UUID jobId, Set<Long> notificationIds, String errorMessage, @Nullable Throwable exception) {
        String stackTraceString = null;
        if (null != exception) {
            stackTraceString = AuditStackTraceUtil.createStackTraceString(exception);
        }

        setAuditEntryFailure(jobId, notificationIds, errorMessage, stackTraceString);
    }

    @Override
    @Transactional
    public void setAuditEntryFailure(UUID jobId, Set<Long> notificationIds, String errorMessage, @Nullable String stackTrace) {
        updateAuditEntries(jobId, notificationIds, auditEntry -> {
            auditEntry.setStatus(AuditEntryStatus.FAILURE.name());
            auditEntry.setErrorMessage(errorMessage);
            if (null != stackTrace) {
                auditEntry.setErrorStackTrace(stackTrace);
            }
        });
    }

    @Override
    @Transactional
    public void setAuditEntryFailure(
        UUID jobId,
        Set<Long> notificationIds,
        OffsetDateTime failureTimestamp,
        String errorMessage,
        @Nullable String stackTrace
    ) {
        updateAuditEntries(jobId, notificationIds, auditEntry -> {
            auditEntry.setTimeLastSent(failureTimestamp);
            auditEntry.setStatus(AuditEntryStatus.FAILURE.name());
            auditEntry.setErrorMessage(errorMessage);
            if (null != stackTrace) {
                auditEntry.setErrorStackTrace(stackTrace);
            }
        });
    }

    private void updateAuditEntries(UUID jobId, Set<Long> notificationIds, Consumer<AuditEntryEntity> auditFieldSetter) {
        if (notificationIds.isEmpty()) {
            return;
        }

        List<AuditEntryNotificationView> auditEntryNotificationViews = auditEntryRepository.findByJobIdAndNotificationIds(jobId, notificationIds);

        List<AuditEntryEntity> updatedAuditEntries = new ArrayList<>(auditEntryNotificationViews.size());
        for (AuditEntryNotificationView view : auditEntryNotificationViews) {
            AuditEntryEntity auditEntryToSave = fromView(view);
            auditEntryToSave.setTimeLastSent(DateUtils.createCurrentDateTimestamp());
            auditFieldSetter.accept(auditEntryToSave);
            updatedAuditEntries.add(auditEntryToSave);
            logger.trace("Updated audit entry: {}.", auditEntryToSave.getId());
        }
        auditEntryRepository.saveAll(updatedAuditEntries);
    }

    private AuditEntryEntity fromView(AuditEntryNotificationView view) {
        AuditEntryEntity auditEntry = new AuditEntryEntity(view.getJobId(), view.getTimeCreated(), view.getTimeLastSent(), view.getStatus(), view.getErrorMessage(), view.getErrorStackTrace());
        auditEntry.setId(view.getId());
        return auditEntry;
    }

}
