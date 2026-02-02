/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.api;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.database.audit.AuditEntryRepository;
import com.blackduck.integration.alert.database.notification.NotificationContentRepository;
import com.blackduck.integration.alert.database.notification.NotificationEntity;

@Component
public class DefaultNotificationAccessor implements NotificationAccessor {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public static final String COLUMN_NAME_PROVIDER_CREATION_TIME = "providerCreationTime";
    private final NotificationContentRepository notificationContentRepository;
    private final AuditEntryRepository auditEntryRepository;
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    @Autowired
    public DefaultNotificationAccessor(
        NotificationContentRepository notificationContentRepository,
        AuditEntryRepository auditEntryRepository,
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor
    ) {
        this.notificationContentRepository = notificationContentRepository;
        this.auditEntryRepository = auditEntryRepository;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<AlertNotificationModel> saveAllNotifications(Collection<AlertNotificationModel> notifications) {
        List<NotificationEntity> entitiesToSave = new LinkedList<>();

        for (AlertNotificationModel model : notifications) {
            if (notificationContentRepository.existsByContentId(model.getContentId())) {
                logger.info("Notification already exists for provider: {} contentId: {}", model.getProviderConfigId(), model.getContentId());
                logger.debug("Content: {}", model.getContent());
            } else {
                entitiesToSave.add(fromModel(model));
            }
        }
        return notificationContentRepository.saveAllAndFlush(entitiesToSave)
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
        return toModels(notificationContentRepository.findAllByIdInOrderByProviderCreationTimeAsc(notificationIds));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Optional<AlertNotificationModel> findById(Long notificationId) {
        return notificationContentRepository.findById(notificationId).map(this::toModel);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public AlertPagedModel<AlertNotificationModel> findByCreatedAtBetween(OffsetDateTime startDate, OffsetDateTime endDate, int pageNumber, int pageSize) {
        Sort.Order sortingOrder = Sort.Order.asc(COLUMN_NAME_PROVIDER_CREATION_TIME);
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by(sortingOrder));
        Page<AlertNotificationModel> pageOfNotifications = notificationContentRepository.findByCreatedAtBetween(startDate, endDate, pageRequest)
            .map(this::toModel);
        List<AlertNotificationModel> alertNotificationModels = pageOfNotifications.getContent();
        return new AlertPagedModel<>(pageOfNotifications.getTotalPages(), pageNumber, pageSize, alertNotificationModels);

    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<AlertNotificationModel> findByCreatedAtBefore(OffsetDateTime date) {
        List<NotificationEntity> byCreatedAtBefore = notificationContentRepository.findByCreatedAtBefore(date);
        return toModels(byCreatedAtBefore);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<AlertNotificationModel> findByCreatedAtBeforeDayOffset(int dayOffset) {
        OffsetDateTime searchTime = DateUtils.createCurrentDateTimestamp()
            .minusDays(dayOffset)
            .withHour(0).withMinute(0).withSecond(0).withNano(0);
        return findByCreatedAtBefore(searchTime);
    }

    @Override
    @Transactional
    public void deleteNotification(AlertNotificationModel notification) {
        notificationContentRepository.deleteById(notification.getId());
    }

    @Override
    @Transactional
    public int deleteNotificationsCreatedBefore(OffsetDateTime date) {
        int deletedNotificationsCount = notificationContentRepository.bulkDeleteCreatedAtBefore(date);
        auditEntryRepository.bulkDeleteOrphanedEntries();
        return deletedNotificationsCount;
    }

    public PageRequest getPageRequestForNotifications(Integer pageNumber, Integer pageSize, @Nullable String sortField, @Nullable String sortOrder) {
        boolean sortQuery = false;
        String sortingField = "createdAt";
        // We can only modify the query for the fields that exist in NotificationContent
        if (StringUtils.isNotBlank(sortField) && "createdAt".equalsIgnoreCase(sortField)
                || "provider".equalsIgnoreCase(sortField)
                || COLUMN_NAME_PROVIDER_CREATION_TIME.equalsIgnoreCase(sortField)
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
    @Deprecated(since = "6.13.0")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public AlertPagedModel<AlertNotificationModel> getFirstPageOfNotificationsNotProcessed(int pageSize) {
        int currentPage = 0;
        Sort.Order sortingOrder = Sort.Order.asc(COLUMN_NAME_PROVIDER_CREATION_TIME);
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, Sort.by(sortingOrder));
        Page<AlertNotificationModel> pageOfNotifications = notificationContentRepository.findByProcessedFalseOrderByProviderCreationTimeAsc(pageRequest)
            .map(this::toModel);
        List<AlertNotificationModel> alertNotificationModels = pageOfNotifications.getContent();
        return new AlertPagedModel<>(pageOfNotifications.getTotalPages(), currentPage, pageSize, alertNotificationModels);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public AlertPagedModel<AlertNotificationModel> getFirstPageOfNotificationsNotProcessed(long providerConfigId, int pageSize) {
        int currentPage = 0;
        Sort.Order sortingOrder = Sort.Order.asc(COLUMN_NAME_PROVIDER_CREATION_TIME);
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, Sort.by(sortingOrder));
        Page<AlertNotificationModel> pageOfNotifications = notificationContentRepository.findByProviderConfigIdAndProcessedFalseOrderByProviderCreationTimeAsc(
                providerConfigId,
                pageRequest
            )
            .map(this::toModel);
        List<AlertNotificationModel> alertNotificationModels = pageOfNotifications.getContent();
        return new AlertPagedModel<>(pageOfNotifications.getTotalPages(), currentPage, pageSize, alertNotificationModels);
    }

    @Override
    @Transactional
    public void setNotificationsProcessed(List<AlertNotificationModel> notifications) {
        Set<Long> notificationIds = notifications
            .stream()
            .map(AlertNotificationModel::getId)
            .collect(Collectors.toSet());
        setNotificationsProcessedById(notificationIds);
    }

    @Override
    @Transactional
    public void setNotificationsProcessedById(Set<Long> notificationIds) {
        notificationContentRepository.setProcessedByIds(notificationIds);
        notificationContentRepository.flush();
    }

    @Override
    @Deprecated(since = "6.13.0")
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public boolean hasMoreNotificationsToProcess() {
        return notificationContentRepository.existsByProcessedFalse();
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public boolean hasMoreNotificationsToProcess(long providerConfigId) {
        return notificationContentRepository.existsByProviderConfigIdAndProcessedFalse(providerConfigId);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public long countNotificationsByProviderAndType(long providerConfigId, String notificationType) {
        return notificationContentRepository.countByProviderConfigIdAndNotificationType(providerConfigId, notificationType);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public AlertPagedModel<AlertNotificationModel> getFirstPageOfNotificationsNotMapped(long providerConfigId, int pageSize) {
        int currentPage = 0;
        Sort.Order sortingOrder = Sort.Order.asc(COLUMN_NAME_PROVIDER_CREATION_TIME);
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize, Sort.by(sortingOrder));
        Page<AlertNotificationModel> pageOfNotifications = notificationContentRepository.findByProviderConfigIdAndMappingToJobsFalseAndProcessedFalseOrderByProviderCreationTimeAscIdAsc(
                        providerConfigId,
                        pageRequest
                )
                .map(this::toModel);
        List<AlertNotificationModel> alertNotificationModels = pageOfNotifications.getContent();
        if (!alertNotificationModels.isEmpty()) {
            setNotificationsMapping(alertNotificationModels);
        }
        return new AlertPagedModel<>(pageOfNotifications.getTotalPages(), currentPage, pageSize, alertNotificationModels);
    }

    @Override
    @Transactional
    public void setNotificationsMapping(List<AlertNotificationModel> notifications) {
        Set<Long> notificationIds = notifications
                .stream()
                .map(AlertNotificationModel::getId)
                .collect(Collectors.toSet());
        setNotificationsMappingById(notificationIds);
    }

    @Override
    @Transactional
    public void setNotificationsMappingById(Set<Long> notificationIds) {
        notificationContentRepository.setMappingToJobsByIds(notificationIds);
        notificationContentRepository.flush();
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public boolean hasMoreNotificationsToMap(long providerConfigId) {
        return notificationContentRepository.existsByProviderConfigIdAndMappingToJobsFalse(providerConfigId);
    }

    @Override
    @Transactional
    public void setNotificationsMappingFalseWhenProcessedFalse(long providerConfigId) {
        notificationContentRepository.setMappingToJobsFalseWhenProcessedFalse(providerConfigId);
    }

    private List<AlertNotificationModel> toModels(List<NotificationEntity> notificationEntities) {
        return notificationEntities
            .stream()
            .map(this::toModel)
            .collect(Collectors.toList());
    }

    private NotificationEntity fromModel(AlertNotificationModel model) {
        return new NotificationEntity(
            model.getId(),
            model.getCreatedAt(),
            model.getProvider(),
            model.getProviderConfigId(),
            model.getProviderCreationTime(),
            model.getNotificationType(),
            model.getContent(),
            model.getProcessed(),
            model.getContentId(),
            model.isMappingToJobs()
        );
    }

    private AlertNotificationModel toModel(NotificationEntity entity) {
        Long providerConfigId = entity.getProviderConfigId();
        String providerConfigName = "DELETED CONFIGURATION";
        if (null != providerConfigId) {
            providerConfigName = configurationModelConfigurationAccessor.getConfigurationById(providerConfigId)
                .flatMap(field -> field.getField(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME))
                .flatMap(ConfigurationFieldModel::getFieldValue)
                .orElse(providerConfigName);
        }
        return new AlertNotificationModel(
            entity.getId(),
            providerConfigId,
            entity.getProvider(),
            providerConfigName,
            entity.getNotificationType(),
            entity.getContent(),
            entity.getCreatedAt(),
            entity.getProviderCreationTime(),
            entity.getProcessed(),
            entity.getContentId(),
            entity.isMappingToJobs()
        );
    }

}
