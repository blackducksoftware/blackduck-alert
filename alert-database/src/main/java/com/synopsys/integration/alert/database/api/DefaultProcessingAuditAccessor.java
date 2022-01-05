/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryNotificationView;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;

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
    public void setAuditEntryFailure(UUID jobId, Set<Long> notificationIds, String errorMessage, @Nullable Throwable exception) {
        updateAuditEntries(jobId, notificationIds, auditEntry -> {
            auditEntry.setStatus(AuditEntryStatus.FAILURE.name());
            auditEntry.setErrorMessage(errorMessage);
            if (null != exception) {
                String stackTraceString = createStackTraceString(exception);
                auditEntry.setErrorStackTrace(stackTraceString);
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

    private String createStackTraceString(Throwable exception) {
        String[] rootCause = ExceptionUtils.getRootCauseStackTrace(exception);
        String exceptionStackTrace = "";
        for (String line : rootCause) {
            if (exceptionStackTrace.length() + line.length() < AuditEntryEntity.STACK_TRACE_CHAR_LIMIT) {
                exceptionStackTrace = String.format("%s%s%s", exceptionStackTrace, line, System.lineSeparator());
            } else {
                break;
            }
        }
        return exceptionStackTrace;
    }

}
