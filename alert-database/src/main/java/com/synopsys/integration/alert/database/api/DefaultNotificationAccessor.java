/**
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.database.notification.NotificationEntity;

@Component
@Transactional
public class DefaultNotificationAccessor implements NotificationAccessor {
    private final NotificationContentRepository notificationContentRepository;
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;
    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    public DefaultNotificationAccessor(NotificationContentRepository notificationContentRepository,
        AuditEntryRepository auditEntryRepository,
        AuditNotificationRepository auditNotificationRepository,
        ConfigurationAccessor configurationAccessor) {
        this.notificationContentRepository = notificationContentRepository;
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public List<AlertNotificationModel> saveAllNotifications(Collection<AlertNotificationModel> notifications) {
        List<NotificationEntity> entitiesToSave = notifications
                                                      .stream()
                                                      .map(this::fromModel)
                                                      .collect(Collectors.toList());

        return notificationContentRepository.saveAll(entitiesToSave)
                   .stream()
                   .map(this::toModel)
                   .collect(Collectors.toList());
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<AlertNotificationModel> findAll(PageRequest pageRequest, boolean onlyShowSentNotifications) {
        if (onlyShowSentNotifications) {
            Page<NotificationEntity> allSentNotifications = notificationContentRepository.findAllSentNotifications(pageRequest);
            return allSentNotifications.map(this::toModel);
        }
        return notificationContentRepository.findAll(pageRequest).map(this::toModel);
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Page<AlertNotificationModel> findAllWithSearch(String searchTerm, PageRequest pageRequest, boolean onlyShowSentNotifications) {
        String lcSearchTerm = searchTerm.toLowerCase(Locale.ENGLISH);

        Page<NotificationEntity> matchingNotifications;
        if (onlyShowSentNotifications) {
            matchingNotifications = notificationContentRepository.findMatchingSentNotification(lcSearchTerm, pageRequest);
        } else {
            matchingNotifications = notificationContentRepository.findMatchingNotification(lcSearchTerm, pageRequest);
        }
        return matchingNotifications.map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<AlertNotificationModel> findByIds(List<Long> notificationIds) {
        return toModels(notificationContentRepository.findAllById(notificationIds));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<AlertNotificationModel> findById(Long notificationId) {
        return notificationContentRepository.findById(notificationId).map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<AlertNotificationModel> findByCreatedAtBetween(OffsetDateTime startDate, OffsetDateTime endDate) {
        List<NotificationEntity> byCreatedAtBetween = notificationContentRepository.findByCreatedAtBetween(startDate, endDate);
        return toModels(byCreatedAtBetween);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<AlertNotificationModel> findByCreatedAtBefore(OffsetDateTime date) {
        List<NotificationEntity> byCreatedAtBefore = notificationContentRepository.findByCreatedAtBefore(date);
        return toModels(byCreatedAtBefore);
    }

    @Override
    public List<AlertNotificationModel> findByCreatedAtBeforeDayOffset(int dayOffset) {
        OffsetDateTime searchTime = DateUtils.createCurrentDateTimestamp()
                                        .minusDays(dayOffset)
                                        .withHour(0).withMinute(0).withSecond(0).withNano(0);
        return findByCreatedAtBefore(searchTime);
    }

    @Override
    public void deleteNotificationList(List<AlertNotificationModel> notifications) {
        notifications.forEach(this::deleteNotification);
    }

    @Override
    public void deleteNotification(AlertNotificationModel notification) {
        deleteAuditEntries(notification.getId());
        notificationContentRepository.deleteById(notification.getId());
    }

    public PageRequest getPageRequestForNotifications(Integer pageNumber, Integer pageSize, @Nullable String sortField, @Nullable String sortOrder) {
        boolean sortQuery = false;
        String sortingField = "createdAt";
        // We can only modify the query for the fields that exist in NotificationContent
        if (StringUtils.isNotBlank(sortField) && "createdAt".equalsIgnoreCase(sortField)
                || "provider".equalsIgnoreCase(sortField)
                || "providerCreationTime".equalsIgnoreCase(sortField)
                || "notificationType".equalsIgnoreCase(sortField)
                || "content".equalsIgnoreCase(sortField)) {
            sortingField = sortField;
            sortQuery = true;
        }
        Sort.Order sortingOrder = Sort.Order.desc(sortingField);
        if (StringUtils.isNotBlank(sortOrder) && sortQuery && Sort.Direction.ASC.name().equalsIgnoreCase(sortOrder)) {
            sortingOrder = Sort.Order.asc(sortingField);
        }
        return PageRequest.of(pageNumber, pageSize, Sort.by(sortingOrder));
    }

    @Override
    public AlertPagedModel<AlertNotificationModel> getFirstPageOfNotificationsNotProcessed(int pageSize) {
        int currentPage = 0;
        Sort.Order sortingOrder = Sort.Order.asc("providerCreationTime");
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, Sort.by(sortingOrder));
        Page<AlertNotificationModel> pageOfNotifications = notificationContentRepository.findByProcessedFalse(pageRequest)
                                                               .map(this::toModel);
        List<AlertNotificationModel> alertNotificationModels = pageOfNotifications.getContent();
        return new AlertPagedModel<>(pageOfNotifications.getTotalPages(), currentPage, pageSize, alertNotificationModels);
    }

    @Override
    public void setNotificationsProcessed(List<AlertNotificationModel> notifications) {
        List<NotificationEntity> notificationEntities = notifications.stream()
                                                            .map(this::fromModel)
                                                            .collect(Collectors.toList());
        notificationEntities.forEach(NotificationEntity::setProcessedToTrue);
        notificationContentRepository.saveAll(notificationEntities);
    }

    private void deleteAuditEntries(Long notificationId) {
        List<AuditNotificationRelation> foundRelations = auditNotificationRepository.findByNotificationId(notificationId);
        List<Long> auditIdList = foundRelations
                                     .stream()
                                     .map(AuditNotificationRelation::getAuditEntryId)
                                     .collect(Collectors.toList());
        List<AuditEntryEntity> auditEntryList = auditEntryRepository.findAllById(auditIdList);
        auditEntryRepository.deleteAll(auditEntryList);
    }

    private List<AlertNotificationModel> toModels(List<NotificationEntity> notificationEntities) {
        return notificationEntities
                   .stream()
                   .map(this::toModel)
                   .collect(Collectors.toList());
    }

    private NotificationEntity fromModel(AlertNotificationModel model) {
        return new NotificationEntity(model.getId(), model.getCreatedAt(), model.getProvider(), model.getProviderConfigId(), model.getProviderCreationTime(), model.getNotificationType(), model.getContent(), model.getProcessed());
    }

    private AlertNotificationModel toModel(NotificationEntity entity) {
        Long providerConfigId = entity.getProviderConfigId();
        String providerConfigName = "DELETED CONFIGURATION";
        if (null != providerConfigId) {
            providerConfigName = configurationAccessor.getConfigurationById(providerConfigId)
                                     .flatMap(field -> field.getField(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME))
                                     .flatMap(ConfigurationFieldModel::getFieldValue)
                                     .orElse(providerConfigName);
        }
        return new AlertNotificationModel(entity.getId(),
            providerConfigId,
            entity.getProvider(),
            providerConfigName,
            entity.getNotificationType(),
            entity.getContent(),
            entity.getCreatedAt(),
            entity.getProviderCreationTime(),
            entity.getProcessed());
    }

}
