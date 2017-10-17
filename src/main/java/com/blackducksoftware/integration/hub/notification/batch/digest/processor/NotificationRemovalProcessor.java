package com.blackducksoftware.integration.hub.notification.batch.digest.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.notification.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

public class NotificationRemovalProcessor {
    private Map<String, NotificationEntity> entityCache;

    public List<NotificationEntity> process(final List<NotificationEntity> notificationList) {
        final List<NotificationEntity> resultList = new ArrayList<>();

        notificationList.forEach(entity -> {
            final boolean processed = processPolicyNotifications(entity);
            if (!processed) {
                processVulnerabilityNotifications(entity);
            }
        });

        resultList.addAll(entityCache.values());
        return resultList;
    }

    private boolean processPolicyNotifications(final NotificationEntity entity) {
        final String notificationType = entity.getNotificationType();
        if (NotificationCategoryEnum.POLICY_VIOLATION.name().equals(notificationType)) {
            entityCache.put(entity.getEventKey(), entity);
            return true;
        } else if (NotificationCategoryEnum.POLICY_VIOLATION_CLEARED.name().equals(notificationType) || NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE.name().equals(notificationType)) {
            if (entityCache.containsKey(entity.getEventKey())) {
                entityCache.remove(entity.getEventKey());
            } else {
                entityCache.put(entity.getEventKey(), entity);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean processVulnerabilityNotifications(final NotificationEntity entity) {
        return false;
    }
}
