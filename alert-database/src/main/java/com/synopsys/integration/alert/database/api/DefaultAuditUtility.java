/**
 * alert-database
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.database.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.message.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.persistence.accessor.AuditUtility;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.CommonDistributionConfiguration;
import com.synopsys.integration.alert.common.rest.model.JobAuditModel;
import com.synopsys.integration.alert.common.rest.model.NotificationConfig;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.notification.NotificationContent;

@Component
public class DefaultAuditUtility implements AuditUtility {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    private static final Logger logger = LoggerFactory.getLogger(DefaultAuditUtility.class);
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;
    private final JobConfigReader jobConfigReader;
    private final DefaultNotificationManager notificationManager;
    private final ContentConverter contentConverter;

    @Autowired
    public DefaultAuditUtility(final AuditEntryRepository auditEntryRepository, final AuditNotificationRepository auditNotificationRepository, final JobConfigReader jobConfigReader,
        final DefaultNotificationManager notificationManager, final ContentConverter contentConverter) {
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
        this.jobConfigReader = jobConfigReader;
        this.notificationManager = notificationManager;
        this.contentConverter = contentConverter;
    }

    @Override
    public Optional<Long> findMatchingAuditId(final Long notificationId, final UUID commonDistributionId) {
        return auditEntryRepository.findMatchingAudit(notificationId, commonDistributionId).map(AuditEntryEntity::getId);
    }

    @Override
    @Transactional
    public Optional<AuditJobStatusModel> findFirstByJobId(final UUID jobId) {
        final Optional<AuditEntryEntity> auditEntryEntity = auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(jobId);
        return auditEntryEntity.map(this::convertToJobStatusModel);
    }

    @Override
    @Transactional
    public AlertPagedModel<AuditEntryModel> getPageOfAuditEntries(final Integer pageNumber, final Integer pageSize, final String searchTerm, final String sortField, final String sortOrder, final boolean onlyShowSentNotifications,
        final Function<AlertNotificationWrapper, AuditEntryModel> notificationToAuditEntryConverter) {
        final Page<AlertNotificationWrapper> auditPage = getPageOfNotifications(sortField, sortOrder, searchTerm, pageNumber, pageSize, onlyShowSentNotifications);
        final List<AuditEntryModel> auditEntries = convertToAuditEntryModelFromNotificationsSorted(auditPage.getContent(), notificationToAuditEntryConverter, sortField, sortOrder);
        return new AlertPagedModel<>(auditPage.getTotalPages(), auditPage.getNumber(), auditEntries.size(), auditEntries);
    }

    @Override
    @Transactional
    public Map<Long, Long> createAuditEntry(final Map<Long, Long> existingNotificationIdToAuditId, final UUID jobId, final AggregateMessageContent content) {
        final Map<Long, Long> notificationIdToAuditId = new HashMap<>();
        final Set<Long> notificationIds = content.getCategoryItemList().stream()
                                              .map(CategoryItem::getNotificationId)
                                              .collect(Collectors.toSet());
        for (final Long notificationId : notificationIds) {
            AuditEntryEntity auditEntryEntity = new AuditEntryEntity(jobId, new Date(System.currentTimeMillis()), null, null, null, null);

            if (null != existingNotificationIdToAuditId && !existingNotificationIdToAuditId.isEmpty()) {
                final Long auditEntryId = existingNotificationIdToAuditId.get(notificationId);
                if (null != auditEntryId) {
                    auditEntryEntity = auditEntryRepository.findById(auditEntryId).orElse(auditEntryEntity);
                }
            }

            auditEntryEntity.setStatus(AuditEntryStatus.PENDING.toString());
            final AuditEntryEntity savedAuditEntryEntity = auditEntryRepository.save(auditEntryEntity);

            notificationIdToAuditId.put(notificationId, savedAuditEntryEntity.getId());
            final AuditNotificationRelation auditNotificationRelation = new AuditNotificationRelation(savedAuditEntryEntity.getId(), notificationId);
            auditNotificationRepository.save(auditNotificationRelation);
        }

        return notificationIdToAuditId;
    }

    @Override
    @Transactional
    public void setAuditEntrySuccess(final Collection<Long> auditEntryIds) {
        for (final Long auditEntryId : auditEntryIds) {
            try {
                final Optional<AuditEntryEntity> auditEntryEntityOptional = auditEntryRepository.findById(auditEntryId);
                if (auditEntryEntityOptional.isEmpty()) {
                    logger.error("Could not find the audit entry {} to set the success status.", auditEntryId);
                }
                final AuditEntryEntity auditEntryEntity = auditEntryEntityOptional.orElse(new AuditEntryEntity());
                auditEntryEntity.setStatus(AuditEntryStatus.SUCCESS.toString());
                auditEntryEntity.setErrorMessage(null);
                auditEntryEntity.setErrorStackTrace(null);
                auditEntryEntity.setTimeLastSent(new Date(System.currentTimeMillis()));
                auditEntryRepository.save(auditEntryEntity);
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    @Transactional
    public void setAuditEntryFailure(final Collection<Long> auditEntryIds, final String errorMessage, final Throwable t) {
        for (final Long auditEntryId : auditEntryIds) {
            try {
                final Optional<AuditEntryEntity> auditEntryEntityOptional = auditEntryRepository.findById(auditEntryId);
                if (auditEntryEntityOptional.isEmpty()) {
                    logger.error("Could not find the audit entry {} to set the failure status. Error: {}", auditEntryId, errorMessage);
                }
                final AuditEntryEntity auditEntryEntity = auditEntryEntityOptional.orElse(new AuditEntryEntity());
                auditEntryEntity.setId(auditEntryId);
                auditEntryEntity.setStatus(AuditEntryStatus.FAILURE.toString());
                auditEntryEntity.setErrorMessage(errorMessage);
                final String[] rootCause = ExceptionUtils.getRootCauseStackTrace(t);
                String exceptionStackTrace = "";
                for (final String line : rootCause) {
                    if (exceptionStackTrace.length() + line.length() < AuditEntryEntity.STACK_TRACE_CHAR_LIMIT) {
                        exceptionStackTrace = String.format("%s%s%s", exceptionStackTrace, line, System.lineSeparator());
                    } else {
                        break;
                    }
                }
                auditEntryEntity.setErrorStackTrace(exceptionStackTrace);
                auditEntryEntity.setTimeLastSent(new Date(System.currentTimeMillis()));
                auditEntryRepository.save(auditEntryEntity);
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    @Transactional
    public AuditEntryModel convertToAuditEntryModelFromNotification(final AlertNotificationWrapper notificationContentEntry) {
        final List<AuditNotificationRelation> relations = auditNotificationRepository.findByNotificationId(notificationContentEntry.getId());
        final List<Long> auditEntryIds = relations.stream().map(AuditNotificationRelation::getAuditEntryId).collect(Collectors.toList());
        final List<AuditEntryEntity> auditEntryEntities = auditEntryRepository.findAllById(auditEntryIds);

        AuditEntryStatus overallStatus = null;
        String lastSent = null;
        Date lastSentDate = null;
        final List<JobAuditModel> jobAuditModels = new ArrayList<>();
        for (final AuditEntryEntity auditEntryEntity : auditEntryEntities) {
            final UUID commonConfigId = auditEntryEntity.getCommonConfigId();

            if (null == lastSentDate || (null != auditEntryEntity.getTimeLastSent() && lastSentDate.before(auditEntryEntity.getTimeLastSent()))) {
                lastSentDate = auditEntryEntity.getTimeLastSent();
                lastSent = contentConverter.getStringValue(lastSentDate);
            }
            final String id = contentConverter.getStringValue(auditEntryEntity.getId());
            final String configId = contentConverter.getStringValue(commonConfigId);
            final String timeCreated = contentConverter.getStringValue(auditEntryEntity.getTimeCreated());
            final String timeLastSent = contentConverter.getStringValue(auditEntryEntity.getTimeLastSent());

            AuditEntryStatus status = null;
            if (auditEntryEntity.getStatus() != null) {
                status = AuditEntryStatus.valueOf(auditEntryEntity.getStatus());
                overallStatus = getWorstStatus(overallStatus, status);
            }

            final String errorMessage = auditEntryEntity.getErrorMessage();
            final String errorStackTrace = auditEntryEntity.getErrorStackTrace();

            final Optional<CommonDistributionConfiguration> commonConfig = jobConfigReader.getPopulatedJobConfig(commonConfigId);
            String distributionConfigName = null;
            String eventType = null;
            if (commonConfig.isPresent()) {
                distributionConfigName = commonConfig.get().getName();
                eventType = commonConfig.get().getChannelName();
            }

            String statusDisplayName = null;
            if (null != status) {
                statusDisplayName = status.getDisplayName();
            }
            final AuditJobStatusModel auditJobStatusModel = new AuditJobStatusModel(timeCreated, timeLastSent, statusDisplayName);
            jobAuditModels.add(new JobAuditModel(id, configId, distributionConfigName, eventType, auditJobStatusModel, errorMessage, errorStackTrace));
        }
        final String id = contentConverter.getStringValue(notificationContentEntry.getId());
        final NotificationConfig notificationConfig = populateConfigFromEntity((NotificationContent) notificationContentEntry);

        String overallStatusDisplayName = null;
        if (null != overallStatus) {
            overallStatusDisplayName = overallStatus.getDisplayName();
        }
        return new AuditEntryModel(id, notificationConfig, jobAuditModels, overallStatusDisplayName, lastSent);
    }

    @Override
    public AuditEntryStatus getWorstStatus(final AuditEntryStatus overallStatus, final AuditEntryStatus currentStatus) {
        AuditEntryStatus newOverallStatus = overallStatus;
        if (null == overallStatus || currentStatus == AuditEntryStatus.FAILURE || (AuditEntryStatus.SUCCESS == overallStatus && AuditEntryStatus.SUCCESS != currentStatus)) {
            newOverallStatus = currentStatus;
        }
        return newOverallStatus;
    }

    private List<AuditEntryModel> convertToAuditEntryModelFromNotificationsSorted(final List<AlertNotificationWrapper> notificationContentEntries, final Function<AlertNotificationWrapper, AuditEntryModel> notificationToAuditEntryConverter,
        final String sortField, final String sortOrder) {
        final List<AuditEntryModel> auditEntryModels = notificationContentEntries.stream().map(notificationToAuditEntryConverter).collect(Collectors.toList());
        if (StringUtils.isBlank(sortField) || sortField.equalsIgnoreCase("lastSent") || sortField.equalsIgnoreCase("overallStatus")) {
            // We do this sorting here because lastSent is not a field in the NotificationContent entity and overallStatus is not stored in the database
            boolean ascendingOrder = false;
            if (StringUtils.isNotBlank(sortOrder) && Sort.Direction.ASC.name().equalsIgnoreCase(sortOrder)) {
                ascendingOrder = true;
            }
            Comparator<AuditEntryModel> comparator;
            if (StringUtils.isBlank(sortField) || sortField.equalsIgnoreCase("lastSent")) {
                final Function<AuditEntryModel, Date> function = auditEntryModel -> {
                    Date date = null;
                    if (StringUtils.isNotBlank(auditEntryModel.getLastSent())) {
                        date = parseDateString(auditEntryModel.getLastSent());
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

    private AuditJobStatusModel convertToJobStatusModel(final AuditEntryEntity auditEntryEntity) {
        String timeCreated = null;
        if (null != auditEntryEntity.getTimeCreated()) {
            timeCreated = contentConverter.getStringValue(auditEntryEntity.getTimeCreated());
        }
        String timeLastSent = null;
        if (null != auditEntryEntity.getTimeLastSent()) {
            timeLastSent = contentConverter.getStringValue(auditEntryEntity.getTimeLastSent());
        }
        String status = null;
        if (null != auditEntryEntity.getStatus()) {
            status = auditEntryEntity.getStatus();
        }
        return new AuditJobStatusModel(timeCreated, timeLastSent, status);
    }

    private Page<AlertNotificationWrapper> getPageOfNotifications(final String sortField, final String sortOrder, final String searchTerm, final Integer pageNumber, final Integer pageSize, final boolean onlyShowSentNotifications) {
        final PageRequest pageRequest = notificationManager.getPageRequestForNotifications(pageNumber, pageSize, sortField, sortOrder);
        final Page<AlertNotificationWrapper> auditPage;
        if (StringUtils.isNotBlank(searchTerm)) {
            auditPage = notificationManager.findAllWithSearch(searchTerm, pageRequest, onlyShowSentNotifications);
        } else {
            auditPage = notificationManager.findAll(pageRequest, onlyShowSentNotifications);
        }
        return auditPage;
    }

    private NotificationConfig populateConfigFromEntity(final NotificationContent notificationContent) {
        final NotificationContent notificationEntity = notificationContent;
        final String id = contentConverter.getStringValue(notificationEntity.getId());
        final String createdAt = contentConverter.getStringValue(notificationEntity.getCreatedAt());
        final String providerCreationTime = contentConverter.getStringValue(notificationEntity.getProviderCreationTime());
        return new NotificationConfig(id, createdAt, notificationEntity.getProvider(), providerCreationTime, notificationEntity.getNotificationType(), notificationEntity.getContent());
    }

    private Date parseDateString(final String dateString) {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = sdf.parse(dateString);
        } catch (final ParseException e) {
            logger.error(e.toString());
        }
        return date;
    }

}
