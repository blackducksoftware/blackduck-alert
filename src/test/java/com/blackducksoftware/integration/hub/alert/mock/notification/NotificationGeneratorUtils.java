package com.blackducksoftware.integration.hub.alert.mock.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.mockito.Mockito;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.component.AffectedProjectVersion;
import com.blackducksoftware.integration.hub.api.core.ResourceMetadata;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.generated.view.VulnerabilityV2View;
import com.blackducksoftware.integration.hub.notification.CommonNotificationView;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResult;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.notification.content.PolicyOverrideNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationClearedNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetail;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetailFactory;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.hub.service.bucket.HubBucketService;
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

    public static CommonNotificationView createCommonNotificationView(final NotificationView view) {
        return new CommonNotificationView(view);
    }

    public static List<NotificationDetailResult> createNotificationDetailList(final NotificationView view, final RuleViolationNotificationContent content) {
        final NotificationContentDetailFactory factory = new NotificationContentDetailFactory(null, null);
        final CommonNotificationView commonNotificationView = createCommonNotificationView(view);
        final String notificationGroup = NotificationContentDetail.CONTENT_KEY_GROUP_POLICY;
        final List<NotificationContentDetail> notificationContentDetails = new ArrayList<>();
        factory.populateContentDetails(notificationContentDetails, notificationGroup, content);

        return Arrays.asList(new NotificationDetailResult(content, commonNotificationView.getContentType(), commonNotificationView.getCreatedAt(), commonNotificationView.getType(), notificationGroup,
                Optional.ofNullable(commonNotificationView.getNotificationState()), notificationContentDetails));
    }

    public static List<NotificationDetailResult> createNotificationDetailList(final NotificationView view, final RuleViolationClearedNotificationContent content) {
        final NotificationContentDetailFactory factory = new NotificationContentDetailFactory(null, null);
        final CommonNotificationView commonNotificationView = createCommonNotificationView(view);
        final String notificationGroup = NotificationContentDetail.CONTENT_KEY_GROUP_POLICY;
        final List<NotificationContentDetail> notificationContentDetails = new ArrayList<>();
        factory.populateContentDetails(notificationContentDetails, notificationGroup, content);

        return Arrays.asList(new NotificationDetailResult(content, commonNotificationView.getContentType(), commonNotificationView.getCreatedAt(), commonNotificationView.getType(), notificationGroup,
                Optional.ofNullable(commonNotificationView.getNotificationState()), notificationContentDetails));
    }

    public static List<NotificationDetailResult> createNotificationDetailList(final NotificationView view, final PolicyOverrideNotificationContent content) {
        final NotificationContentDetailFactory factory = new NotificationContentDetailFactory(null, null);
        final CommonNotificationView commonNotificationView = createCommonNotificationView(view);
        final String notificationGroup = NotificationContentDetail.CONTENT_KEY_GROUP_POLICY;
        final List<NotificationContentDetail> notificationContentDetails = new ArrayList<>();
        factory.populateContentDetails(notificationContentDetails, notificationGroup, content);

        return Arrays.asList(new NotificationDetailResult(content, commonNotificationView.getContentType(), commonNotificationView.getCreatedAt(), commonNotificationView.getType(), notificationGroup,
                Optional.ofNullable(commonNotificationView.getNotificationState()), notificationContentDetails));
    }

    public static NotificationDetailResult createNotificationDetailList(final NotificationView view, final VulnerabilityNotificationContent content) {
        final NotificationContentDetailFactory factory = new NotificationContentDetailFactory(null, null);
        final CommonNotificationView commonNotificationView = createCommonNotificationView(view);
        final String notificationGroup = NotificationContentDetail.CONTENT_KEY_GROUP_VULNERABILITY;
        final List<NotificationContentDetail> notificationContentDetails = new ArrayList<>();
        factory.populateContentDetails(notificationContentDetails, notificationGroup, content);

        return new NotificationDetailResult(content, commonNotificationView.getContentType(), commonNotificationView.getCreatedAt(), commonNotificationView.getType(), notificationGroup,
                Optional.ofNullable(commonNotificationView.getNotificationState()), notificationContentDetails);
    }

    public static NotificationDetailResults createNotificationResults(final List<NotificationDetailResult> detailList) {
        final Date createdAt = detailList.get(detailList.size() - 1).getCreatedAt();
        final NotificationDetailResults results = new NotificationDetailResults(detailList, Optional.of(createdAt), Optional.of(RestConnection.formatDate(createdAt)), new HubBucket());
        return results;
    }

    public static NotificationDetailResults initializeTestData(final GlobalProperties globalProperties, final ComponentVersionView versionView, final VulnerabilityNotificationContent content) throws IntegrationException {
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final HubService hubService = Mockito.mock(HubService.class);
        final HubBucketService bucketService = Mockito.mock(HubBucketService.class);
        final List<VulnerabilityV2View> vulnerabilityViewList = createVulnerabilityList();
        final RestConnection restConnection = Mockito.mock(RestConnection.class);

        Mockito.when(globalProperties.createRestConnectionAndLogErrors(Mockito.any())).thenReturn(restConnection);
        Mockito.when(globalProperties.createHubServicesFactory(Mockito.any())).thenReturn(hubServicesFactory);
        Mockito.when(hubServicesFactory.createHubService()).thenReturn(hubService);
        Mockito.when(hubServicesFactory.createHubBucketService()).thenReturn(bucketService);
        Mockito.when(hubService.getResponse(Mockito.any(UriSingleResponse.class))).thenReturn(versionView);
        Mockito.when(hubService.getAllResponses(versionView, ComponentVersionView.VULNERABILITIES_LINK_RESPONSE)).thenReturn(vulnerabilityViewList);
        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.VULNERABILITY);

        createCommonContentData(content);

        final NotificationDetailResult detail = NotificationGeneratorUtils.createNotificationDetailList(view, content);
        final NotificationDetailResults notificationResults = NotificationGeneratorUtils.createNotificationResults(Arrays.asList(detail));
        final HubBucket bucket = notificationResults.getHubBucket();
        // need to map the component version uri to a view in order for the processing rule to work
        // otherwise the rule will always have an empty list
        bucket.addValid(content.componentVersion, versionView);
        return notificationResults;
    }

    public static void createCommonContentData(final VulnerabilityNotificationContent content) {
        final AffectedProjectVersion affectedProjectVersion = new AffectedProjectVersion();
        affectedProjectVersion.projectName = "VulnerableProjectName";
        affectedProjectVersion.projectVersionName = "1.2.3";
        affectedProjectVersion.projectVersion = "projectURL";
        affectedProjectVersion.componentIssueUrl = "componentIssueUrl";

        content.componentVersion = "componentversionurl";
        content.componentName = "VulnerableComponent";
        content.versionName = "1.2.3";
        content.componentVersionOriginName = "originName";
        content.affectedProjectVersions = Arrays.asList(affectedProjectVersion);
        content.componentVersionOriginId = "originId";
    }

    public static List<VulnerabilitySourceQualifiedId> createSourceIdList(final String... ids) {
        final List<VulnerabilitySourceQualifiedId> sourceIdList = new ArrayList<>(ids.length);
        for (final String id : ids) {
            final VulnerabilitySourceQualifiedId vuln = new VulnerabilitySourceQualifiedId();
            vuln.vulnerabilityId = id;
            sourceIdList.add(vuln);
        }
        return sourceIdList;
    }

    public static List<VulnerabilityV2View> createVulnerabilityList() {
        final VulnerabilityV2View vuln_1 = new VulnerabilityV2View();
        vuln_1.name = "1";
        vuln_1.severity = "LOW";
        vuln_1._meta = new ResourceMetadata();
        vuln_1._meta.href = "href_1";

        final VulnerabilityV2View vuln_2 = new VulnerabilityV2View();
        vuln_2.name = "2";
        vuln_2.severity = "LOW";
        vuln_2._meta = new ResourceMetadata();
        vuln_2._meta.href = "href_2";

        final VulnerabilityV2View vuln_3 = new VulnerabilityV2View();
        vuln_3.name = "3";
        vuln_3.severity = "LOW";
        vuln_3._meta = new ResourceMetadata();
        vuln_3._meta.href = "href_3";

        final VulnerabilityV2View vuln_4 = new VulnerabilityV2View();
        vuln_4.name = "4";
        vuln_4.severity = "MEDIUM";
        vuln_4._meta = new ResourceMetadata();
        vuln_4._meta.href = "href_4";

        final VulnerabilityV2View vuln_5 = new VulnerabilityV2View();
        vuln_5.name = "5";
        vuln_5.severity = "HIGH";
        vuln_5._meta = new ResourceMetadata();
        vuln_5._meta.href = "href_5";

        final VulnerabilityV2View vuln_6 = new VulnerabilityV2View();
        vuln_6.name = "6";
        vuln_6.severity = "HIGH";
        vuln_6._meta = new ResourceMetadata();
        vuln_6._meta.href = "href_6";

        final VulnerabilityV2View vuln_7 = new VulnerabilityV2View();
        vuln_7.name = "7";
        vuln_7.severity = "HIGH";
        vuln_7._meta = new ResourceMetadata();
        vuln_7._meta.href = "href_7";

        final VulnerabilityV2View vuln_8 = new VulnerabilityV2View();
        vuln_8.name = "8";
        vuln_8.severity = "HIGH";
        vuln_8._meta = new ResourceMetadata();
        vuln_8._meta.href = "href_8";

        final VulnerabilityV2View vuln_9 = new VulnerabilityV2View();
        vuln_9.name = "9";
        vuln_9.severity = "HIGH";
        vuln_9._meta = new ResourceMetadata();
        vuln_9._meta.href = "href_9";

        final VulnerabilityV2View vuln_10 = new VulnerabilityV2View();
        vuln_10.name = "10";
        vuln_10.severity = "HIGH";
        vuln_10._meta = new ResourceMetadata();
        vuln_10._meta.href = "href_10";

        final VulnerabilityV2View vuln_11 = new VulnerabilityV2View();
        vuln_11.name = "11";
        vuln_11.severity = "HIGH";
        vuln_11._meta = new ResourceMetadata();
        vuln_11._meta.href = "href_11";

        return Arrays.asList(vuln_1, vuln_2, vuln_3, vuln_4, vuln_5, vuln_6, vuln_7, vuln_8, vuln_9, vuln_10, vuln_11);
    }
}
