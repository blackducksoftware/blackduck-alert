package com.blackducksoftware.integration.hub.alert.mock.notification;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.NotificationContentDetailResults;
import com.blackducksoftware.integration.hub.notification.NotificationResults;
import com.blackducksoftware.integration.hub.notification.NotificationViewResults;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;
import com.blackducksoftware.integration.hub.notification.content.detail.ContentDetailCollector;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetail;
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

    public static CommonNotificationState createCommonNotificationState(final NotificationView view, final NotificationContent content) {
        return new CommonNotificationState(view, content);
    }

    public static List<NotificationContentDetail> createNotificationDetailList(final CommonNotificationState commonNotificationState) {
        final ContentDetailCollector detailsCollector = new ContentDetailCollector();
        final Map<NotificationContent, List<NotificationContentDetail>> detailMap = detailsCollector.collect(Arrays.asList(commonNotificationState));
        return detailMap.get(commonNotificationState.getContent());
    }

    public static NotificationResults createNotificationResults(final List<CommonNotificationState> commonNotificationStates) {
        final Date createdAt = commonNotificationStates.get(commonNotificationStates.size() - 1).getCreatedAt();
        final NotificationViewResults viewResults = new NotificationViewResults(commonNotificationStates, Optional.of(createdAt), Optional.of(RestConnection.formatDate(createdAt)));

        final ContentDetailCollector detailsCollector = new ContentDetailCollector();
        final Map<NotificationContent, List<NotificationContentDetail>> detailMap = detailsCollector.collect(commonNotificationStates);
        final NotificationContentDetailResults detailResults = new NotificationContentDetailResults(detailMap);
        final NotificationResults results = new NotificationResults(viewResults, new HubBucket(), detailResults);
        return results;
    }
}
