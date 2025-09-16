/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.api;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.blackduck.integration.alert.common.enumeration.AuditEntryStatus;
import com.blackduck.integration.alert.common.persistence.accessor.JobAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.ProcessingFailedAccessor;
import com.blackduck.integration.alert.common.persistence.model.AuditEntryModel;
import com.blackduck.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.blackduck.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.rest.model.JobAuditModel;
import com.blackduck.integration.alert.common.rest.model.NotificationConfig;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.database.audit.AuditFailedEntity;
import com.blackduck.integration.alert.database.audit.AuditFailedEntryRepository;
import com.blackduck.integration.alert.database.audit.AuditFailedNotificationEntity;
import com.blackduck.integration.alert.database.audit.AuditFailedNotificationRepository;

@Component
public class DefaultProcessingFailedAccessor implements ProcessingFailedAccessor {

    public static final String UNKNOWN_JOB = "Job Unknown";
    public static final String UNKNOWN_CHANNEL = "Unknown Channel";
    private final AuditFailedEntryRepository auditFailedEntryRepository;
    private final AuditFailedNotificationRepository auditFailedNotificationRepository;
    private final NotificationAccessor notificationAccessor;
    private final JobAccessor jobAccessor;

    @Autowired
    public DefaultProcessingFailedAccessor(
        AuditFailedEntryRepository auditFailedEntryRepository,
        AuditFailedNotificationRepository auditFailedNotificationRepository,
        NotificationAccessor notificationAccessor,
        JobAccessor jobAccessor
    ) {
        this.auditFailedEntryRepository = auditFailedEntryRepository;
        this.auditFailedNotificationRepository = auditFailedNotificationRepository;
        this.notificationAccessor = notificationAccessor;
        this.jobAccessor = jobAccessor;
    }

    @Override
    @Transactional
    public AuditEntryPageModel getPageOfAuditEntries(Integer pageNumber, Integer pageSize, String searchTerm, String sortField, String sortOrder) {
        PageRequest pageRequest = getPageRequestForFailures(pageNumber, pageSize, sortField, sortOrder);
        Page<AuditFailedEntity> matchingAuditEntities;
        if (StringUtils.isNotBlank(searchTerm)) {
            matchingAuditEntities = auditFailedEntryRepository.findAllWithSearchTerm(searchTerm.toLowerCase(), pageRequest);
        } else {
            matchingAuditEntities = auditFailedEntryRepository.findAll(pageRequest);
        }
        List<AuditEntryModel> auditEntries = convertFailedEntries(matchingAuditEntities.getContent());
        return new AuditEntryPageModel(matchingAuditEntities.getTotalPages(), matchingAuditEntities.getNumber(), matchingAuditEntities.getSize(), auditEntries);
    }

    @Override
    @Transactional()
    public void setAuditFailure(UUID jobId, Set<Long> notificationIds, OffsetDateTime occurrence, String errorMessage) {
        List<AlertNotificationModel> notificationModels = notificationAccessor.findByIds(new ArrayList<>(notificationIds));
        Optional<DistributionJobModel> distributionJobModel = jobAccessor.getJobById(jobId);
        Map<Long, String> auditedNotifications = new HashMap<>();
        for (AlertNotificationModel notificationModel : notificationModels) {
            Long notificationId = notificationModel.getId();
            String jobName = distributionJobModel.map(DistributionJobModel::getName).orElse(UNKNOWN_JOB);
            if (!auditFailedEntryRepository.existsByJobNameAndNotificationId(jobName, notificationId)) {
                AuditFailedEntity auditFailedEntity = new AuditFailedEntity(
                    UUID.randomUUID(),
                    occurrence,
                    jobName,
                    notificationModel.getProvider(),
                    notificationModel.getProviderConfigName(),
                    distributionJobModel.map(DistributionJobModel::getChannelDescriptorName).orElse(UNKNOWN_CHANNEL),
                    notificationModel.getNotificationType(),
                    errorMessage,
                    notificationId
                );
                auditFailedEntryRepository.save(auditFailedEntity);
                auditedNotifications.put(notificationId, notificationModel.getContent());
            }
        }
        List<AuditFailedNotificationEntity> notificationEntities = auditedNotifications.entrySet().stream()
            .filter(notification -> !auditFailedNotificationRepository.existsById(notification.getKey()))
            .map(notification -> new AuditFailedNotificationEntity(notification.getKey(), notification.getValue(), true))
            .collect(Collectors.toList());
        auditFailedNotificationRepository.saveAllAndFlush(notificationEntities);
    }

    @Override
    @Transactional
    public void setAuditFailure(UUID jobId, Set<Long> notificationIds, OffsetDateTime occurrence, String errorMessage, String stackTrace) {
        List<AlertNotificationModel> notificationModels = notificationAccessor.findByIds(new ArrayList<>(notificationIds));
        Optional<DistributionJobModel> distributionJobModel = jobAccessor.getJobById(jobId);
        Map<Long, String> auditedNotifications = new HashMap<>();
        for (AlertNotificationModel notificationModel : notificationModels) {
            Long notificationId = notificationModel.getId();
            String jobName = distributionJobModel.map(DistributionJobModel::getName).orElse(UNKNOWN_JOB);
            if (!auditFailedEntryRepository.existsByJobNameAndNotificationId(jobName, notificationId)) {
                AuditFailedEntity auditFailedEntity = new AuditFailedEntity(
                    UUID.randomUUID(),
                    occurrence,
                    jobName,
                    notificationModel.getProvider(),
                    notificationModel.getProviderConfigName(),
                    distributionJobModel.map(DistributionJobModel::getChannelDescriptorName).orElse(UNKNOWN_CHANNEL),
                    notificationModel.getNotificationType(),
                    errorMessage,
                    stackTrace,
                    notificationId
                );
                auditFailedEntryRepository.save(auditFailedEntity);
                auditedNotifications.put(notificationId, notificationModel.getContent());
            }
        }

        List<AuditFailedNotificationEntity> notificationEntities = auditedNotifications.entrySet().stream()
            .filter(notification -> !auditFailedNotificationRepository.existsById(notification.getKey()))
            .map(notification -> new AuditFailedNotificationEntity(notification.getKey(), notification.getValue(), true))
            .collect(Collectors.toList());
        auditFailedNotificationRepository.saveAllAndFlush(notificationEntities);
    }

    @Override
    @Transactional
    public void deleteAuditEntriesBefore(OffsetDateTime expirationDate) {
        List<AuditFailedEntity> auditFailedEntities = auditFailedEntryRepository.findAllByCreatedAtBefore(expirationDate);
        Set<Long> notificationIds = auditFailedEntities.stream()
            .map(AuditFailedEntity::getNotificationId)
            .collect(Collectors.toSet());
        List<UUID> entryIds = auditFailedEntities.stream()
            .map(AuditFailedEntity::getId)
            .collect(Collectors.toList());
        if (!entryIds.isEmpty()) {
            auditFailedEntryRepository.deleteAllById(entryIds);
        }

        Predicate<Long> notificationNoLongExists = notificationId -> !auditFailedEntryRepository.existsByNotificationId(notificationId);
        List<Long> notificationIdsToRemove = notificationIds.stream()
            .filter(notificationNoLongExists)
            .collect(Collectors.toList());

        if (!notificationIdsToRemove.isEmpty()) {
            auditFailedNotificationRepository.deleteAllById(notificationIdsToRemove);
        }
    }

    @Override
    @Transactional
    public void deleteAuditsWithNotificationId(Long notificationId) {
        auditFailedEntryRepository.deleteAllByNotificationId(notificationId);
    }

    @Override
    @Transactional
    public void deleteAuditsWithJobIdAndNotificationId(UUID jobId, Long notificationId) {
        Optional<DistributionJobModel> distributionJobModel = jobAccessor.getJobById(jobId);
        String jobName = distributionJobModel.map(DistributionJobModel::getName).orElse(UNKNOWN_JOB);
        auditFailedEntryRepository.deleteAllByJobNameAndNotificationId(jobName, notificationId);
    }

    private List<AuditEntryModel> convertFailedEntries(List<AuditFailedEntity> failedEntities) {
        Map<Long, List<JobAuditModel>> jobAuditModelMap = new HashMap<>();
        for (AuditFailedEntity entity : failedEntities) {
            List<JobAuditModel> jobAuditModels = jobAuditModelMap.computeIfAbsent(entity.getNotificationId(), ignored -> new LinkedList<>());
            AuditJobStatusModel jobStatusModel = new AuditJobStatusModel(
                UUID.randomUUID(),
                formatAuditDate(entity.getCreatedAt()),
                formatAuditDate(entity.getCreatedAt()),
                AuditEntryStatus.FAILURE.getDisplayName()
            );
            String jobConfigId = jobAccessor.getJobByName(entity.getJobName())
                .map(DistributionJobModel::getJobId)
                .map(UUID::toString)
                .orElse("");
            jobAuditModels.add(new JobAuditModel(
                entity.getId().toString(),
                jobConfigId,
                entity.getJobName(),
                entity.getChannelName(),
                jobStatusModel,
                entity.getErrorMessage(),
                entity.getErrorStackTrace().orElse(null)
            ));
        }

        Map<Long, AuditEntryModel> auditEntryModelMap = new LinkedHashMap<>();
        for (AuditFailedEntity entity : failedEntities) {
            Long notificationId = entity.getNotificationId();
            auditEntryModelMap.computeIfAbsent(notificationId, ignoredKey -> {
                NotificationConfig notificationConfig = createNotificationConfig(entity);
                return new AuditEntryModel(
                    notificationConfig.getId(),
                    notificationConfig,
                    jobAuditModelMap.getOrDefault(notificationId, List.of()),
                    AuditEntryStatus.FAILURE.getDisplayName(),
                    formatAuditDate(entity.getCreatedAt())
                );
            });
        }
        return new ArrayList<>(auditEntryModelMap.values());
    }

    private NotificationConfig createNotificationConfig(AuditFailedEntity entity) {
        Optional<AuditFailedNotificationEntity> notificationEntity = auditFailedNotificationRepository.findById(entity.getNotificationId());
        return new NotificationConfig(
            entity.getNotificationId().toString(),
            formatAuditDate(entity.getCreatedAt()),
            entity.getProviderKey(),
            0L,
            entity.getProviderName(),
            formatAuditDate(entity.getCreatedAt()),
            entity.getNotificationType(),
            notificationEntity.map(AuditFailedNotificationEntity::getNotificationContent).orElse("")
        );
    }

    private String formatAuditDate(OffsetDateTime dateTime) {
        OffsetDateTime utcDateTime = DateUtils.fromInstantUTC(dateTime.toInstant());
        return DateUtils.formatDate(utcDateTime, DateUtils.AUDIT_DATE_FORMAT);
    }

    private PageRequest getPageRequestForFailures(Integer pageNumber, Integer pageSize, @Nullable String sortField, @Nullable String sortOrder) {
        boolean sortQuery = false;
        String defaultSortField = "createdAt";
        String inputSortField;
        String sortingField = defaultSortField;
        if ("lastSent".equals(sortField)) {
            inputSortField = defaultSortField;
        } else {
            inputSortField = sortField;
        }
        List<String> validFields = List.of(
            defaultSortField,
            "jobName",
            "providerName",
            "channelName",
            "notificationType",
            "errorMessage",
            "errorStackTrace",
            "notificationId"
        );
        Predicate<String> sortFieldMatch = fieldName -> fieldName.equalsIgnoreCase(inputSortField);
        // We can only modify the query for the fields that exist in NotificationContent
        if (StringUtils.isNotBlank(inputSortField) && validFields.stream()
            .anyMatch(sortFieldMatch)) {
            sortingField = inputSortField;
            sortQuery = true;
        }
        Sort.Order sortingOrder = Sort.Order.desc(sortingField);
        if (StringUtils.isNotBlank(sortOrder) && sortQuery && Sort.Direction.ASC.name().equalsIgnoreCase(sortOrder)) {
            sortingOrder = Sort.Order.asc(sortingField);
        }
        return PageRequest.of(pageNumber, pageSize, Sort.by(sortingOrder));
    }
}
