package com.blackducksoftware.integration.hub.alert.mock.notification;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.NotificationResults;
import com.blackducksoftware.integration.hub.notification.NotificationViewResults;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;
import com.blackducksoftware.integration.hub.notification.content.NotificationContentDetail;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.rest.connection.RestConnection;

public class NotificationGeneratorUtils {

    public NotificationView createNotificationView(final NotificationType type) {
        return createNotificationView(new Date(), type);
    }

    public NotificationView createNotificationView(final Date createdAt, final NotificationType type) {
        final NotificationView view = new NotificationView();
        view.contentType = "application/json";
        view.createdAt = createdAt;
        view.type = type;
        return view;
    }

    public CommonNotificationState createCommonNotificationState(final NotificationView view, final NotificationContent content) {
        return new CommonNotificationState(view, content);
    }

    public NotificationResults createNotificationResults(final List<CommonNotificationState> commonNotificationStates) {
        final Date createdAt = commonNotificationStates.get(commonNotificationStates.size() - 1).getCreatedAt();
        final NotificationViewResults viewResults = new NotificationViewResults(commonNotificationStates, Optional.of(createdAt), Optional.of(RestConnection.formatDate(createdAt)));
        final NotificationResults results = new NotificationResults(viewResults, new HubBucket());
        return results;
    }

    public boolean assertContentDetailListEqual(final List<NotificationContentDetail> detailList_1, final List<NotificationContentDetail> detailList_2) {
        boolean result = true;
        for (final NotificationContentDetail detail_1 : detailList_1) {
            for (final NotificationContentDetail detail_2 : detailList_2) {
                result = result && assertContentDetailEqual(detail_1, detail_2);
            }
        }
        return result;
    }

    public boolean assertContentDetailEqual(final NotificationContentDetail detail_1, final NotificationContentDetail detail_2) {

        final String projectName_1 = detail_1.getProjectName();
        final String projectVersionName_1 = detail_1.getProjectVersionName();
        final String componentName_1 = detail_1.getComponentName().orElse("");
        final String componentVersionName_1 = detail_1.getComponentVersionName().orElse("");
        final String policyName_1 = detail_1.getPolicyName().orElse("");
        final String componentVersionOriginName_1 = detail_1.getComponentVersionOriginName().orElse("");
        final String componentVersionOriginId_1 = detail_1.getComponentVersionOriginId().orElse("");
        final String contentDetailKey_1 = detail_1.getContentDetailKey();

        final String projectName_2 = detail_2.getProjectName();
        final String projectVersionName_2 = detail_2.getProjectVersionName();
        final String componentName_2 = detail_2.getComponentName().orElse("");
        final String componentVersionName_2 = detail_2.getComponentVersionName().orElse("");
        final String policyName_2 = detail_2.getPolicyName().orElse("");
        final String componentVersionOriginName_2 = detail_2.getComponentVersionOriginName().orElse("");
        final String componentVersionOriginId_2 = detail_2.getComponentVersionOriginId().orElse("");
        final String contentDetailKey_2 = detail_2.getContentDetailKey();

        return Objects.equals(projectName_1, projectName_2)
                && Objects.equals(projectVersionName_1, projectVersionName_2)
                && Objects.equals(componentName_1, componentName_2)
                && Objects.equals(componentVersionName_1, componentVersionName_2)
                && Objects.equals(policyName_1, policyName_2)
                && Objects.equals(componentVersionOriginName_1, componentVersionOriginName_2)
                && Objects.equals(componentVersionOriginId_1, componentVersionOriginId_2)
                && Objects.equals(contentDetailKey_1, contentDetailKey_2);

    }

}
