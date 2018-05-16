package com.blackducksoftware.integration.hub.alert.mock.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.NotificationResults;
import com.blackducksoftware.integration.hub.notification.NotificationViewResult;
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

    public static NotificationViewResult createNotificationViewResult(final NotificationView view, final NotificationContent content) {
        final CommonNotificationState commonNotificationState = new CommonNotificationState(view, content);
        final List<NotificationContentDetail> notificationContentDetails = createNotificationDetailList(commonNotificationState);
        final NotificationViewResult notificationViewResult = new NotificationViewResult(commonNotificationState, notificationContentDetails);
        return notificationViewResult;
    }

    public static List<NotificationContentDetail> createNotificationDetailList(final CommonNotificationState commonNotificationState) {
        final ContentDetailCollector detailsCollector = new ContentDetailCollector();
        final List<NotificationViewResult> resultList = detailsCollector.collect(Arrays.asList(commonNotificationState));
        final List<NotificationContentDetail> detailList = new ArrayList<>();
        resultList.forEach(notificationViewResult -> {
            detailList.addAll(notificationViewResult.getNotificationContentDetails());
        });
        return detailList;
    }

    public static NotificationResults createNotificationResults(final List<CommonNotificationState> commonNotificationStates) {
        final Date createdAt = commonNotificationStates.get(commonNotificationStates.size() - 1).getCreatedAt();
        final ContentDetailCollector detailsCollector = new ContentDetailCollector();
        final List<NotificationViewResult> resultList = detailsCollector.collect(commonNotificationStates);
        final NotificationViewResults viewResults = new NotificationViewResults(resultList, Optional.of(createdAt), Optional.of(RestConnection.formatDate(createdAt)));
        final NotificationResults results = new NotificationResults(viewResults, new HubBucket());
        return results;
    }
}
