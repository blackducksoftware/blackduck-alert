package com.blackducksoftware.integration.hub.alert.mock.notification;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.notification.content.PolicyOverrideNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationClearedNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetail;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetailFactory;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.rest.connection.RestConnection;

public class NotificationGeneratorUtils {

    private NotificationGeneratorUtils() {

    }

    public static NotificationView createNotificationView(final NotificationType type) {
        return createNotificationView(new Date(), type);
    }

    public static NotificationView createNotificationView(final Date createdAt, final NotificationType type) {
        final NotificationView view = new NotificationView();
        view.contentType = "application/json";
        view.createdAt = createdAt;
        view.type = type;
        return view;
    }

    public static List<NotificationContentDetail> createNotificationDetailList(final NotificationView view, final RuleViolationNotificationContent content) {
        final NotificationContentDetailFactory factory = new NotificationContentDetailFactory(null, null);
        final List<NotificationContentDetail> detailList = factory.generateContentDetails(view.contentType, view.createdAt, null, view.type, content);
        return detailList;
    }

    public static List<NotificationContentDetail> createNotificationDetailList(final NotificationView view, final RuleViolationClearedNotificationContent content) {
        final NotificationContentDetailFactory factory = new NotificationContentDetailFactory(null, null);
        final List<NotificationContentDetail> detailList = factory.generateContentDetails(view.contentType, view.createdAt, null, view.type, content);
        return detailList;
    }

    public static List<NotificationContentDetail> createNotificationDetailList(final NotificationView view, final PolicyOverrideNotificationContent content) {
        final NotificationContentDetailFactory factory = new NotificationContentDetailFactory(null, null);
        final List<NotificationContentDetail> detailList = factory.generateContentDetails(view.contentType, view.createdAt, null, view.type, content);
        return detailList;
    }

    public static List<NotificationContentDetail> createNotificationDetailList(final NotificationView view, final VulnerabilityNotificationContent content) {
        final NotificationContentDetailFactory factory = new NotificationContentDetailFactory(null, null);
        final List<NotificationContentDetail> detailList = factory.generateContentDetails(view.contentType, view.createdAt, null, view.type, content);
        return detailList;
    }

    public static NotificationDetailResults createNotificationResults(final List<NotificationContentDetail> detailList) {
        final Date createdAt = detailList.get(detailList.size() - 1).getCreatedAt();
        final NotificationDetailResults results = new NotificationDetailResults(detailList, Optional.of(createdAt), Optional.of(RestConnection.formatDate(createdAt)), new HubBucket());
        return results;
    }
}
