package com.synopsys.integration.alert.workflow.message.mocks;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;

public class MockNotificationAccessor implements NotificationAccessor {
    private final int pageSize = 100;

    List<AlertNotificationModel> alertNotificationModels = new ArrayList<>();

    public MockNotificationAccessor(List<AlertNotificationModel> alertNotificationModels) {
        this.alertNotificationModels = alertNotificationModels;
    }

    @Override
    public List<AlertNotificationModel> saveAllNotifications(Collection<AlertNotificationModel> notifications) {
        return null;
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
    public List<AlertNotificationModel> findByCreatedAtBetween(OffsetDateTime startDate, OffsetDateTime endDate) {
        return null;
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
    public Page<AlertNotificationModel> findNotificationsNotProcessed() {
        return new PageImpl<>(alertNotificationModels, Pageable.unpaged(), pageSize);
    }

    @Override
    public void setNotificationsProcessed(List<AlertNotificationModel> notifications) {

    }

    @Override
    public void deleteNotificationList(List<AlertNotificationModel> notifications) {

    }

    @Override
    public void deleteNotification(AlertNotificationModel notification) {

    }
}
