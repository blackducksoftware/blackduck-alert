/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.tasks;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;

public class MockProcessingNotificationAccessor implements NotificationAccessor {
    ArrayList<AlertNotificationModel> alertNotificationModels;

    public MockProcessingNotificationAccessor(List<AlertNotificationModel> alertNotificationModels) {
        this.alertNotificationModels = new ArrayList<>(alertNotificationModels);
    }

    @Override
    public List<AlertNotificationModel> saveAllNotifications(Collection<AlertNotificationModel> notifications) {
        alertNotificationModels.addAll(notifications);
        return alertNotificationModels;
    }

    @Override
    public List<AlertNotificationModel> findByIds(List<Long> notificationIds) {
        return null;
    }

    @Override
    public Optional<AlertNotificationModel> findById(Long notificationId) {
        return Optional.empty();
    }

    @Override
    public AlertPagedModel<AlertNotificationModel> findByCreatedAtBetween(OffsetDateTime startDate, OffsetDateTime endDate, int pageNumber, int pageSize) {
        Predicate<AlertNotificationModel> beforePredicate = notification -> notification.getCreatedAt().isBefore(endDate) || notification.getCreatedAt().isEqual(endDate);
        Predicate<AlertNotificationModel> afterPredicate = notification -> notification.getCreatedAt().isAfter(startDate) || notification.getCreatedAt().isEqual(startDate);
        Predicate<AlertNotificationModel> withinRange = beforePredicate.and(afterPredicate);
        List<AlertNotificationModel> notifications = alertNotificationModels.stream()
            .sorted(Comparator.comparing(AlertNotificationModel::getCreatedAt))
            .filter(withinRange)
            .collect(Collectors.toList());
        List<List<AlertNotificationModel>> partitionedLists = ListUtils.partition(notifications, pageSize);
        int totalPages = partitionedLists.size();
        if (partitionedLists.size() >= pageNumber) {
            return new AlertPagedModel<>(totalPages, pageNumber, pageSize, partitionedLists.get(pageNumber));
        } else {
            return new AlertPagedModel<>(0, 0, pageSize, List.of());
        }
    }

    @Override
    public List<AlertNotificationModel> findByCreatedAtBefore(OffsetDateTime date) {
        return null;
    }

    @Override
    public List<AlertNotificationModel> findByCreatedAtBeforeDayOffset(int dayOffset) {
        return null;
    }

    @Override
    @Deprecated(since = "6.13.0")
    public AlertPagedModel<AlertNotificationModel> getFirstPageOfNotificationsNotProcessed(int pageSize) {
        List<AlertNotificationModel> notificationsNotProcessed = alertNotificationModels
            .stream()
            .filter(Predicate.not(AlertNotificationModel::getProcessed))
            .collect(Collectors.toList());
        
        Page<AlertNotificationModel> pageOfNotifications;
        if (notificationsNotProcessed.size() > 0) {
            pageOfNotifications = new PageImpl<>(notificationsNotProcessed);
        } else {
            pageOfNotifications = Page.empty();
        }
        return new AlertPagedModel<>(pageOfNotifications.getTotalPages(), pageOfNotifications.getNumber(), pageOfNotifications.getSize(), pageOfNotifications.getContent());
    }

    @Override
    public AlertPagedModel<AlertNotificationModel> getFirstPageOfNotificationsNotProcessed(long providerConfigId, int pageSize) {
        List<AlertNotificationModel> notificationsNotProcessed = alertNotificationModels
            .stream()
            .filter(model -> model.getProviderConfigId().equals(providerConfigId))
            .filter(Predicate.not(AlertNotificationModel::getProcessed))
            .collect(Collectors.toList());

        Page<AlertNotificationModel> pageOfNotifications;
        if (notificationsNotProcessed.size() > 0) {
            pageOfNotifications = new PageImpl<>(notificationsNotProcessed);
        } else {
            pageOfNotifications = Page.empty();
        }
        return new AlertPagedModel<>(pageOfNotifications.getTotalPages(), pageOfNotifications.getNumber(), pageOfNotifications.getSize(), pageOfNotifications.getContent());

    }

    @Override
    public void setNotificationsProcessed(List<AlertNotificationModel> notifications) {
        for (AlertNotificationModel notification : notifications) {
            AlertNotificationModel updatedNotification = createProcessedAlertNotificationModel(notification);
            int index = alertNotificationModels.indexOf(notification);
            alertNotificationModels.set(index, updatedNotification);
        }
    }

    @Override
    public void setNotificationsProcessedById(Set<Long> notificationIds) {

    }

    @Override
    public void deleteNotification(AlertNotificationModel notification) {

    }

    @Override
    public int deleteNotificationsCreatedBefore(OffsetDateTime date) {
        return 0;
    }

    @Override
    @Deprecated(since = "6.13.0")
    public boolean hasMoreNotificationsToProcess() {
        return alertNotificationModels.stream()
            .anyMatch(AlertNotificationModel::getProcessed);
    }

    @Override
    public boolean hasMoreNotificationsToProcess(long providerConfigId) {
        return alertNotificationModels.stream()
            .filter(model -> model.getProviderConfigId().equals(providerConfigId))
            .anyMatch(AlertNotificationModel::getProcessed);
    }

    @Override
    public long countNotificationsByProviderAndType(long providerConfigId, String notificationType) {
        Predicate<AlertNotificationModel> providerEqual = model -> model.getProviderConfigId().equals(providerConfigId);
        Predicate<AlertNotificationModel> notificationTypeEqual = model -> model.getNotificationType().equals(notificationType);
        return alertNotificationModels
            .stream()
            .filter(providerEqual.and(notificationTypeEqual))
            .count();
    }

    //AlertNotificationModel is immutable, this is a workaround for the unit test to set "processed" to true.
    private AlertNotificationModel createProcessedAlertNotificationModel(AlertNotificationModel alertNotificationModel) {
        return new AlertNotificationModel(
            alertNotificationModel.getId(),
            alertNotificationModel.getProviderConfigId(),
            alertNotificationModel.getProvider(),
            alertNotificationModel.getProviderConfigName(),
            alertNotificationModel.getNotificationType(),
            alertNotificationModel.getContent(),
            alertNotificationModel.getCreatedAt(),
            alertNotificationModel.getProviderCreationTime(),
            true,
            alertNotificationModel.getContentId()
        );
    }
}
