/**
 * alert-database
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditUtility;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.JobAuditModel;
import com.synopsys.integration.alert.common.rest.model.NotificationConfig;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;

@Component
public class DefaultAuditUtility implements AuditUtility {
    private final Logger logger = LoggerFactory.getLogger(DefaultAuditUtility.class);
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;
    private final ConfigurationAccessor configurationAccessor;
    private final DefaultNotificationAccessor notificationManager;
    private final ContentConverter contentConverter;

    @Autowired
    public DefaultAuditUtility(AuditEntryRepository auditEntryRepository, AuditNotificationRepository auditNotificationRepository, ConfigurationAccessor configurationAccessor,
        DefaultNotificationAccessor notificationManager, ContentConverter contentConverter) {
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
        this.configurationAccessor = configurationAccessor;
        this.notificationManager = notificationManager;
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
    public AuditEntryPageModel getPageOfAuditEntries(Integer pageNumber, Integer pageSize, String searchTerm, String sortField, String sortOrder, boolean onlyShowSentNotifications,
        Function<AlertNotificationModel, AuditEntryModel> notificationToAuditEntryConverter) {
        Page<AlertNotificationModel> auditPage = getPageOfNotifications(sortField, sortOrder, searchTerm, pageNumber, pageSize, onlyShowSentNotifications);
        List<AuditEntryModel> auditEntries = convertToAuditEntryModelFromNotificationsSorted(auditPage.getContent(), notificationToAuditEntryConverter, sortField, sortOrder);
        return new AuditEntryPageModel(auditPage.getTotalPages(), auditPage.getNumber(), auditEntries.size(), auditEntries);
    }

    @Override
    @Transactional
    public Map<Long, Long> createAuditEntry(Map<Long, Long> existingNotificationIdToAuditId, UUID jobId, MessageContentGroup contentGroup) {
        Map<Long, Long> notificationIdToAuditId = new HashMap<>();
        List<ProviderMessageContent> subContent = contentGroup.getSubContent();
        Set<Long> componentNotificationIds = subContent
                                                 .stream()
                                                 .map(ProviderMessageContent::getComponentItems)
                                                 .flatMap(Collection::stream)
                                                 .map(ComponentItem::getNotificationIds)
                                                 .flatMap(Set::stream)
                                                 .collect(Collectors.toSet());
        Set<Long> topLevelActionNotificationIds = subContent
                                                      .stream()
                                                      .filter(ProviderMessageContent::isTopLevelActionOnly)
                                                      .map(ProviderMessageContent::getNotificationId)
                                                      .flatMap(Optional::stream)
                                                      .collect(Collectors.toSet());
        Set<Long> allMessageNotificationIds = Stream
                                                  .concat(componentNotificationIds.stream(), topLevelActionNotificationIds.stream())
                                                  .collect(Collectors.toSet());
        for (Long notificationId : allMessageNotificationIds) {
            AuditEntryEntity auditEntryEntity = new AuditEntryEntity(jobId, DateUtils.createCurrentDateTimestamp(), null, null, null, null);

            boolean didAuditEntryExist = false;
            if (null != existingNotificationIdToAuditId && !existingNotificationIdToAuditId.isEmpty()) {
                Long auditEntryId = existingNotificationIdToAuditId.get(notificationId);
                didAuditEntryExist = null != auditEntryId;
                if (didAuditEntryExist) {
                    auditEntryEntity = auditEntryRepository.findById(auditEntryId).orElse(auditEntryEntity);
                }
            }

            auditEntryEntity.setStatus(AuditEntryStatus.PENDING.toString());
            AuditEntryEntity savedAuditEntryEntity = auditEntryRepository.save(auditEntryEntity);

            notificationIdToAuditId.put(notificationId, savedAuditEntryEntity.getId());
            if (!didAuditEntryExist) {
                AuditNotificationRelation auditNotificationRelation = new AuditNotificationRelation(savedAuditEntryEntity.getId(), notificationId);
                auditNotificationRepository.save(auditNotificationRelation);
            }
        }
        return notificationIdToAuditId;
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
            UUID commonConfigId = auditEntryEntity.getCommonConfigId();

            if (null != auditEntryEntity.getTimeLastSent() && (null == timeLastSentOffsetDateTime || timeLastSentOffsetDateTime.isBefore(auditEntryEntity.getTimeLastSent()))) {
                timeLastSentOffsetDateTime = auditEntryEntity.getTimeLastSent();
                timeLastSent = formatAuditDate(timeLastSentOffsetDateTime);
            }
            String id = contentConverter.getStringValue(auditEntryEntity.getId());
            String configId = contentConverter.getStringValue(commonConfigId);
            String timeCreated = formatAuditDate(auditEntryEntity.getTimeCreated());

            AuditEntryStatus status = null;
            if (auditEntryEntity.getStatus() != null) {
                status = AuditEntryStatus.valueOf(auditEntryEntity.getStatus());
                overallStatus = getWorstStatus(overallStatus, status);
            }

            String errorMessage = auditEntryEntity.getErrorMessage();
            String errorStackTrace = auditEntryEntity.getErrorStackTrace();

            Optional<ConfigurationJobModel> commonConfig = Optional.empty();
            try {
                commonConfig = configurationAccessor.getJobById(commonConfigId);
            } catch (AlertDatabaseConstraintException e) {
                logger.error("There was an issue accessing the job.");
            }
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
            AuditJobStatusModel auditJobStatusModel = new AuditJobStatusModel(timeCreated, timeLastSent, statusDisplayName);
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
        return new AuditJobStatusModel(timeCreated, timeLastSent, status);
    }

    private Page<AlertNotificationModel> getPageOfNotifications(String sortField, String sortOrder, String searchTerm, Integer pageNumber, Integer pageSize, boolean onlyShowSentNotifications) {
        PageRequest pageRequest = notificationManager.getPageRequestForNotifications(pageNumber, pageSize, sortField, sortOrder);
        Page<AlertNotificationModel> auditPage;
        if (StringUtils.isNotBlank(searchTerm)) {
            auditPage = notificationManager.findAllWithSearch(searchTerm, pageRequest, onlyShowSentNotifications);
        } else {
            auditPage = notificationManager.findAll(pageRequest, onlyShowSentNotifications);
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
            return DateUtils.formatDate(dateTime, DateUtils.AUDIT_DATE_FORMAT);
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
        String configName = "UNKNOWN PROVIDER CONFIG";
        try {
            configName = configurationAccessor.getConfigurationById(providerConfigId)
                             .stream()
                             .map(ConfigurationModel::getCopyOfFieldList)
                             .flatMap(List::stream)
                             .filter(field -> field.getFieldKey().equals(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME))
                             .map(ConfigurationFieldModel::getFieldValue)
                             .flatMap(Optional::stream)
                             .findFirst()
                             .orElse(configName);
        } catch (AlertDatabaseConstraintException e) {
            logger.error("There was a problem retrieving the provider config", e);
        }
        return configName;
    }

}
