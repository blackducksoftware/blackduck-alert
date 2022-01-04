/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.RestApiAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModelData;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.JobAuditModel;
import com.synopsys.integration.alert.common.rest.model.NotificationConfig;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;

@Component
public class DefaultRestApiAuditAccessor implements RestApiAuditAccessor {
    private final Logger logger = LoggerFactory.getLogger(DefaultRestApiAuditAccessor.class);
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;
    private final JobAccessor jobAccessor;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final DefaultNotificationAccessor notificationAccessor;
    private final ContentConverter contentConverter;

    @Autowired
    public DefaultRestApiAuditAccessor(AuditEntryRepository auditEntryRepository, AuditNotificationRepository auditNotificationRepository, JobAccessor jobAccessor,
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor,
        DefaultNotificationAccessor notificationAccessor, ContentConverter contentConverter) {
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
        this.jobAccessor = jobAccessor;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.notificationAccessor = notificationAccessor;
        this.contentConverter = contentConverter;
    }

    @Override
    public Optional<Long> findMatchingAuditId(Long notificationId, UUID commonDistributionId) {
        return auditEntryRepository.findMatchingAudit(notificationId, commonDistributionId).map(AuditEntryEntity::getId);
    }

    @Override
    @Transactional
    public Optional<AuditJobStatusModel> findFirstByJobId(UUID jobId) {
        Optional<AuditEntryEntity> auditEntryEntity = auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(jobId);
        return auditEntryEntity.map(this::convertToJobStatusModel);
    }

    @Override
    @Transactional
    public List<AuditJobStatusModel> findByJobIds(Collection<UUID> jobIds) {
        if (jobIds.isEmpty()) {
            return List.of();
        }

        // Add these one at a time to avoid complex query with multiple nested "DISTINCT" selections
        List<AuditJobStatusModel> auditJobStatuses = new ArrayList<>(jobIds.size());
        for (UUID jobId : jobIds) {
            auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(jobId)
                .map(this::convertToJobStatusModel)
                .ifPresent(auditJobStatuses::add);
        }
        return auditJobStatuses;
    }

    @Override
    @Transactional
    public AuditEntryPageModel getPageOfAuditEntries(Integer pageNumber, Integer pageSize, String searchTerm, String sortField, String sortOrder, boolean onlyShowSentNotifications,
        Function<AlertNotificationModel, AuditEntryModel> notificationToAuditEntryConverter) {
        Page<AlertNotificationModel> auditPage = getPageOfNotifications(sortField, sortOrder, searchTerm, pageNumber, pageSize, onlyShowSentNotifications);
        List<AuditEntryModel> auditEntries = convertToAuditEntryModelFromNotificationsSorted(auditPage.getContent(), notificationToAuditEntryConverter, sortField, sortOrder);
        return new AuditEntryPageModel(auditPage.getTotalPages(), auditPage.getNumber(), auditEntries.size(), auditEntries);
    }

    @Override
    @Transactional
    public AuditEntryModel convertToAuditEntryModelFromNotification(AlertNotificationModel notificationContentEntry) {
        List<AuditNotificationRelation> relations = auditNotificationRepository.findByNotificationId(notificationContentEntry.getId());
        List<Long> auditEntryIds = relations.stream().map(AuditNotificationRelation::getAuditEntryId).collect(Collectors.toList());
        List<AuditEntryEntity> auditEntryEntities = auditEntryRepository.findAllById(auditEntryIds);

        AuditEntryStatus overallStatus = null;
        String timeLastSent = null;
        OffsetDateTime timeLastSentOffsetDateTime = null;
        List<JobAuditModel> jobAuditModels = new ArrayList<>();
        for (AuditEntryEntity auditEntryEntity : auditEntryEntities) {
            UUID jobId = auditEntryEntity.getCommonConfigId();

            if (null != auditEntryEntity.getTimeLastSent() && (null == timeLastSentOffsetDateTime || timeLastSentOffsetDateTime.isBefore(auditEntryEntity.getTimeLastSent()))) {
                timeLastSentOffsetDateTime = auditEntryEntity.getTimeLastSent();
                timeLastSent = formatAuditDate(timeLastSentOffsetDateTime);
            }
            String id = contentConverter.getStringValue(auditEntryEntity.getId());
            String configId = contentConverter.getStringValue(jobId);
            String timeCreated = formatAuditDate(auditEntryEntity.getTimeCreated());

            AuditEntryStatus status = null;
            if (auditEntryEntity.getStatus() != null) {
                status = AuditEntryStatus.valueOf(auditEntryEntity.getStatus());
                overallStatus = getWorstStatus(overallStatus, status);
            }

            String errorMessage = auditEntryEntity.getErrorMessage();
            String errorStackTrace = auditEntryEntity.getErrorStackTrace();

            Optional<DistributionJobModel> distributionJobModel = jobAccessor.getJobById(jobId);
            String distributionConfigName = distributionJobModel.map(DistributionJobModelData::getName).orElse(null);
            String eventType = distributionJobModel.map(DistributionJobModelData::getChannelDescriptorName).orElse(null);

            String statusDisplayName = null;
            if (null != status) {
                statusDisplayName = status.getDisplayName();
            }
            AuditJobStatusModel auditJobStatusModel = new AuditJobStatusModel(jobId, timeCreated, timeLastSent, statusDisplayName);
            jobAuditModels.add(new JobAuditModel(id, configId, distributionConfigName, eventType, auditJobStatusModel, errorMessage, errorStackTrace));
        }
        String id = contentConverter.getStringValue(notificationContentEntry.getId());
        NotificationConfig notificationConfig = populateConfigFromEntity(notificationContentEntry);

        String overallStatusDisplayName = null;
        if (null != overallStatus) {
            overallStatusDisplayName = overallStatus.getDisplayName();
        }
        return new AuditEntryModel(id, notificationConfig, jobAuditModels, overallStatusDisplayName, timeLastSent);
    }

    private AuditEntryStatus getWorstStatus(AuditEntryStatus overallStatus, AuditEntryStatus currentStatus) {
        AuditEntryStatus newOverallStatus = overallStatus;
        if (null == overallStatus || currentStatus == AuditEntryStatus.FAILURE || (AuditEntryStatus.SUCCESS == overallStatus && AuditEntryStatus.SUCCESS != currentStatus)) {
            newOverallStatus = currentStatus;
        }
        return newOverallStatus;
    }

    private List<AuditEntryModel> convertToAuditEntryModelFromNotificationsSorted(List<AlertNotificationModel> notificationContentEntries, Function<AlertNotificationModel, AuditEntryModel> notificationToAuditEntryConverter,
        String sortField, String sortOrder) {
        List<AuditEntryModel> auditEntryModels = notificationContentEntries
                                                     .stream()
                                                     .map(notificationToAuditEntryConverter)
                                                     .collect(Collectors.toList());
        if (StringUtils.isBlank(sortField) || sortField.equalsIgnoreCase("lastSent") || sortField.equalsIgnoreCase("overallStatus")) {
            // We do this sorting here because lastSent is not a field in the NotificationContent entity and overallStatus is not stored in the database
            boolean ascendingOrder = false;
            if (StringUtils.isNotBlank(sortOrder) && Sort.Direction.ASC.name().equalsIgnoreCase(sortOrder)) {
                ascendingOrder = true;
            }
            Comparator<AuditEntryModel> comparator;
            if (StringUtils.isBlank(sortField) || sortField.equalsIgnoreCase("lastSent")) {
                Function<AuditEntryModel, OffsetDateTime> function = auditEntryModel -> {
                    OffsetDateTime date = null;
                    if (StringUtils.isNotBlank(auditEntryModel.getLastSent())) {
                        date = parseAuditDateString(auditEntryModel.getLastSent());
                    }
                    return date;
                };
                comparator = Comparator.comparing(function, Comparator.nullsFirst(Comparator.reverseOrder()));
            } else {
                comparator = Comparator.comparing(AuditEntryModel::getOverallStatus, Comparator.nullsLast(String::compareTo));
            }
            if (ascendingOrder) {
                comparator = comparator.reversed();
            }
            auditEntryModels.sort(comparator);
        }
        return auditEntryModels;
    }

    private AuditJobStatusModel convertToJobStatusModel(AuditEntryEntity auditEntryEntity) {
        String timeCreated = formatAuditDate(auditEntryEntity.getTimeCreated());
        String timeLastSent = formatAuditDate(auditEntryEntity.getTimeLastSent());
        String status = null;
        if (null != auditEntryEntity.getStatus()) {
            status = AuditEntryStatus.valueOf(auditEntryEntity.getStatus()).getDisplayName();
        }
        return new AuditJobStatusModel(auditEntryEntity.getCommonConfigId(), timeCreated, timeLastSent, status);
    }

    private Page<AlertNotificationModel> getPageOfNotifications(String sortField, String sortOrder, String searchTerm, Integer pageNumber, Integer pageSize, boolean onlyShowSentNotifications) {
        PageRequest pageRequest = notificationAccessor.getPageRequestForNotifications(pageNumber, pageSize, sortField, sortOrder);
        Page<AlertNotificationModel> auditPage;
        if (StringUtils.isNotBlank(searchTerm)) {
            auditPage = notificationAccessor.findAllWithSearch(searchTerm, pageRequest, onlyShowSentNotifications);
        } else {
            auditPage = notificationAccessor.findAll(pageRequest, onlyShowSentNotifications);
        }
        return auditPage;
    }

    private NotificationConfig populateConfigFromEntity(AlertNotificationModel notificationEntity) {
        String id = contentConverter.getStringValue(notificationEntity.getId());
        String createdAt = formatAuditDate(notificationEntity.getCreatedAt());
        String providerCreationTime = formatAuditDate(notificationEntity.getProviderCreationTime());

        Long providerConfigId = notificationEntity.getProviderConfigId();
        String providerConfigName = retrieveProviderConfigName(providerConfigId);

        return new NotificationConfig(id, createdAt, notificationEntity.getProvider(), providerConfigId, providerConfigName, providerCreationTime, notificationEntity.getNotificationType(), notificationEntity.getContent());
    }

    @Nullable
    private String formatAuditDate(OffsetDateTime dateTime) {
        if (null != dateTime) {
            OffsetDateTime utcDateTime = DateUtils.fromDateUTC(Date.from(dateTime.toInstant()));
            return DateUtils.formatDate(utcDateTime, DateUtils.AUDIT_DATE_FORMAT);
        }
        return null;
    }

    private OffsetDateTime parseAuditDateString(String dateString) {
        OffsetDateTime date = null;
        try {
            date = DateUtils.parseDate(dateString, DateUtils.AUDIT_DATE_FORMAT);
        } catch (ParseException e) {
            logger.error(e.toString());
        }
        return date;
    }

    private String retrieveProviderConfigName(Long providerConfigId) {
        return configurationModelConfigurationAccessor.getConfigurationById(providerConfigId)
                   .stream()
                   .map(ConfigurationModel::getCopyOfFieldList)
                   .flatMap(List::stream)
                   .filter(field -> field.getFieldKey().equals(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME))
                   .map(ConfigurationFieldModel::getFieldValue)
                   .flatMap(Optional::stream)
                   .findFirst()
                   .orElse("UNKNOWN PROVIDER CONFIG");
    }

}
