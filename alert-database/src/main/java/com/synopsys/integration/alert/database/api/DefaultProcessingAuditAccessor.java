/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
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
    public Long findOrCreatePendingAuditEntryForJob(UUID jobId, Set<Long> notificationIds) {
        Long auditEntryId;
        Set<Long> notificationIdsToRelate = notificationIds;

        Optional<AuditEntryEntity> optionalExistingEntry = auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(jobId);
        if (optionalExistingEntry.isPresent()) {
            AuditEntryEntity exitingEntry = optionalExistingEntry.get();
            exitingEntry.setStatus(AuditEntryStatus.PENDING.name());
            AuditEntryEntity updatedEntry = auditEntryRepository.save(exitingEntry);

            auditEntryId = updatedEntry.getId();
            Set<Long> existingNotificationIds = auditNotificationRepository.findByAuditEntryId(auditEntryId)
                                                    .stream()
                                                    .map(AuditNotificationRelation::getNotificationId)
                                                    .collect(Collectors.toSet());
            notificationIdsToRelate = SetUtils.difference(notificationIds, existingNotificationIds);
        } else {
            AuditEntryEntity auditEntryToSave = new AuditEntryEntity(jobId, DateUtils.createCurrentDateTimestamp(), null, AuditEntryStatus.PENDING.name(), null, null);
            AuditEntryEntity savedAuditEntry = auditEntryRepository.save(auditEntryToSave);
            auditEntryId = savedAuditEntry.getId();
        }

        List<AuditNotificationRelation> auditNotificationRelationsToSave = new ArrayList<>(notificationIds.size());
        for (Long notificationId : notificationIdsToRelate) {
            AuditNotificationRelation auditNotificationRelation = new AuditNotificationRelation(auditEntryId, notificationId);
            auditNotificationRelationsToSave.add(auditNotificationRelation);
        }

        auditNotificationRepository.saveAll(auditNotificationRelationsToSave);
        return auditEntryId;
    }

    @Override
    @Transactional
    public void setAuditEntrySuccess(Collection<Long> auditEntryIds) {
        for (Long auditEntryId : auditEntryIds) {
            try {
                Optional<AuditEntryEntity> auditEntryEntityOptional = auditEntryRepository.findById(auditEntryId);
                if (auditEntryEntityOptional.isEmpty()) {
                    logger.error("Could not find the audit entry {} to set the success status.", auditEntryId);
                }
                AuditEntryEntity auditEntryEntity = auditEntryEntityOptional.orElse(new AuditEntryEntity());
                auditEntryEntity.setStatus(AuditEntryStatus.SUCCESS.toString());
                auditEntryEntity.setErrorMessage(null);
                auditEntryEntity.setErrorStackTrace(null);
                auditEntryEntity.setTimeLastSent(DateUtils.createCurrentDateTimestamp());
                auditEntryRepository.save(auditEntryEntity);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    @Transactional
    public void setAuditEntryFailure(Collection<Long> auditEntryIds, String errorMessage, Throwable t) {
        for (Long auditEntryId : auditEntryIds) {
            try {
                Optional<AuditEntryEntity> auditEntryEntityOptional = auditEntryRepository.findById(auditEntryId);
                if (auditEntryEntityOptional.isEmpty()) {
                    logger.error("Could not find the audit entry {} to set the failure status. Error: {}", auditEntryId, errorMessage);
                }
                AuditEntryEntity auditEntryEntity = auditEntryEntityOptional.orElse(new AuditEntryEntity());
                auditEntryEntity.setId(auditEntryId);
                auditEntryEntity.setStatus(AuditEntryStatus.FAILURE.toString());
                auditEntryEntity.setErrorMessage(errorMessage);
                String[] rootCause = ExceptionUtils.getRootCauseStackTrace(t);
                String exceptionStackTrace = "";
                for (String line : rootCause) {
                    if (exceptionStackTrace.length() + line.length() < AuditEntryEntity.STACK_TRACE_CHAR_LIMIT) {
                        exceptionStackTrace = String.format("%s%s%s", exceptionStackTrace, line, System.lineSeparator());
                    } else {
                        break;
                    }
                }
                auditEntryEntity.setErrorStackTrace(exceptionStackTrace);
                auditEntryEntity.setTimeLastSent(DateUtils.createCurrentDateTimestamp());
                auditEntryRepository.save(auditEntryEntity);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
