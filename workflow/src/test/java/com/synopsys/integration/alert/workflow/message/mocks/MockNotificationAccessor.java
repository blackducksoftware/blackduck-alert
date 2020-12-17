package com.synopsys.integration.alert.workflow.message.mocks;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public class MockNotificationAccessor implements NotificationAccessor {
    ArrayList<AlertNotificationModel> alertNotificationModels;

    public MockNotificationAccessor(List<AlertNotificationModel> alertNotificationModels) {
        this.alertNotificationModels = new ArrayList<>(alertNotificationModels);
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
    public AlertPagedModel<AlertNotificationModel> getFirstPageOfNotificationsNotProcessed(int pageSize) {
        ArrayList<AlertNotificationModel> notificationsNotProcessed = new ArrayList<>();
        for (AlertNotificationModel notification : alertNotificationModels) {
            if (!notification.getProcessed()) {
                notificationsNotProcessed.add(notification);
            }
        }
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
    public void deleteNotificationList(List<AlertNotificationModel> notifications) {

    }

    @Override
    public void deleteNotification(AlertNotificationModel notification) {

    }

    //AlertNotificationModel is immutable, this is a workaround for the unit test to set "processed" to true.
    private AlertNotificationModel createProcessedAlertNotificationModel(AlertNotificationModel alertNotificationModel) {
        return new AlertNotificationModel(alertNotificationModel.getId(),
            alertNotificationModel.getProviderConfigId(),
            alertNotificationModel.getProvider(),
            alertNotificationModel.getProviderConfigName(),
            alertNotificationModel.getNotificationType(),
            alertNotificationModel.getContent(),
            alertNotificationModel.getCreatedAt(),
            alertNotificationModel.getProviderCreationTime(),
            true);
    }
}
