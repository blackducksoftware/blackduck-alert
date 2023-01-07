package com.synopsys.integration.alert.database.api;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingFailedAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.JobAuditModel;
import com.synopsys.integration.alert.common.rest.model.NotificationConfig;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.audit.AuditFailedEntity;
import com.synopsys.integration.alert.database.audit.AuditFailedEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditFailedNotificationEntity;
import com.synopsys.integration.alert.database.audit.AuditFailedNotificationRepository;

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
            matchingAuditEntities = auditFailedEntryRepository.findAllWithSearchTerm(searchTerm, pageRequest);
        } else {
            matchingAuditEntities = auditFailedEntryRepository.findAll(pageRequest);
        }
        List<AuditEntryModel> auditEntries = convertFailedEntries(matchingAuditEntities.getContent());
        return new AuditEntryPageModel(matchingAuditEntities.getTotalPages(), matchingAuditEntities.getNumber(), matchingAuditEntities.getSize(), auditEntries);
    }

    @Override
    @Transactional
    public void setAuditFailure(UUID jobId, Set<Long> notificationIds, OffsetDateTime occurrence, String errorMessage) {
        List<AlertNotificationModel> notificationModels = notificationAccessor.findByIds(new ArrayList<>(notificationIds));
        Optional<DistributionJobModel> distributionJobModel = jobAccessor.getJobById(jobId);
        for (AlertNotificationModel notificationModel : notificationModels) {
            Long notificationId = notificationModel.getId();
            AuditFailedEntity auditFailedEntity = new AuditFailedEntity(
                UUID.randomUUID(),
                occurrence,
                distributionJobModel.map(DistributionJobModel::getName).orElse(UNKNOWN_JOB),
                notificationModel.getProviderConfigName(),
                distributionJobModel.map(DistributionJobModel::getChannelDescriptorName).orElse(UNKNOWN_CHANNEL),
                notificationModel.getNotificationType(),
                errorMessage,
                notificationId
            );
            auditFailedEntryRepository.save(auditFailedEntity);
            if (!auditFailedNotificationRepository.existsById(notificationId)) {
                auditFailedNotificationRepository.save(new AuditFailedNotificationEntity(notificationId, notificationModel.getContent()));
            }
        }
    }

    @Override
    @Transactional
    public void setAuditFailure(UUID jobId, Set<Long> notificationIds, OffsetDateTime occurrence, String errorMessage, String stackTrace) {
        List<AlertNotificationModel> notificationModels = notificationAccessor.findByIds(new ArrayList<>(notificationIds));
        Optional<DistributionJobModel> distributionJobModel = jobAccessor.getJobById(jobId);
        for (AlertNotificationModel notificationModel : notificationModels) {
            Long notificationId = notificationModel.getId();
            AuditFailedEntity auditFailedEntity = new AuditFailedEntity(
                UUID.randomUUID(),
                occurrence,
                distributionJobModel.map(DistributionJobModel::getName).orElse(UNKNOWN_JOB),
                notificationModel.getProviderConfigName(),
                distributionJobModel.map(DistributionJobModel::getChannelDescriptorName).orElse(UNKNOWN_CHANNEL),
                notificationModel.getNotificationType(),
                errorMessage,
                stackTrace,
                notificationId
            );
            auditFailedEntryRepository.save(auditFailedEntity);
            if (!auditFailedNotificationRepository.existsById(notificationId)) {
                auditFailedNotificationRepository.save(new AuditFailedNotificationEntity(notificationId, notificationModel.getContent()));
            }
        }
    }

    private List<AuditEntryModel> convertFailedEntries(List<AuditFailedEntity> failedEntities) {
        List<AuditEntryModel> models = new LinkedList<>();
        Map<Long, List<JobAuditModel>> jobAuditModelMap = new HashMap<>();
        for (AuditFailedEntity entity : failedEntities) {
            List<JobAuditModel> jobAuditModels = jobAuditModelMap.computeIfAbsent(entity.getNotificationId(), ignored -> new LinkedList<>());

            AuditJobStatusModel jobStatusModel = new AuditJobStatusModel(
                UUID.randomUUID(),
                DateUtils.formatDateAsJsonString(entity.getTimeCreated()),
                DateUtils.formatDateAsJsonString(entity.getTimeCreated()),
                AuditEntryStatus.FAILURE.getDisplayName()
            );
            jobAuditModels.add(new JobAuditModel(
                "",
                "",
                entity.getJobName(),
                entity.getNotificationType(),
                jobStatusModel,
                entity.getErrorMessage(),
                entity.getErrorStackTrace().orElse(null)
            ));
        }

        for (AuditFailedEntity entity : failedEntities) {
            NotificationConfig notificationConfig = createNotificationConfig(entity);
            AuditEntryModel model = new AuditEntryModel(
                entity.getId().toString(),
                notificationConfig,
                jobAuditModelMap.getOrDefault(entity.getNotificationId(), List.of()),
                AuditEntryStatus.FAILURE.getDisplayName(),
                DateUtils.formatDateAsJsonString(entity.getTimeCreated())
            );
            models.add(model);
        }
        return models;
    }

    private NotificationConfig createNotificationConfig(AuditFailedEntity entity) {
        return new NotificationConfig(
            entity.getNotificationId().toString(),
            DateUtils.formatDateAsJsonString(entity.getTimeCreated()),
            "",
            0L,
            entity.getProviderName(),
            DateUtils.formatDateAsJsonString(entity.getTimeCreated()),
            entity.getNotificationType(),
            entity.getNotification().getNotificationContent()
        );
    }

    private PageRequest getPageRequestForFailures(Integer pageNumber, Integer pageSize, @Nullable String sortField, @Nullable String sortOrder) {
        boolean sortQuery = false;
        String sortingField = "timeCreated";
        List<String> validFields = List.of(
            "timeCreated",
            "jobName",
            "providerName",
            "channelName",
            "notificationType",
            "errorMessage",
            "errorStackTrace",
            "notificationId"
        );
        Predicate<String> sortFieldMatch = fieldName -> fieldName.equalsIgnoreCase(sortField);
        // We can only modify the query for the fields that exist in NotificationContent
        if (StringUtils.isNotBlank(sortField) && validFields.stream()
            .anyMatch(sortFieldMatch)) {
            sortingField = sortField;
            sortQuery = true;
        }
        Sort.Order sortingOrder = Sort.Order.desc(sortingField);
        if (StringUtils.isNotBlank(sortOrder) && sortQuery && Sort.Direction.ASC.name().equalsIgnoreCase(sortOrder)) {
            sortingOrder = Sort.Order.asc(sortingField);
        }
        return PageRequest.of(pageNumber, pageSize, Sort.by(sortingOrder));
    }
}
