package com.synopsys.integration.alert.mock.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.mockito.Mockito;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.component.AffectedProjectVersion;
import com.synopsys.integration.blackduck.api.generated.component.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.NotificationView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityV2View;
import com.synopsys.integration.blackduck.notification.CommonNotificationView;
import com.synopsys.integration.blackduck.notification.NotificationDetailResult;
import com.synopsys.integration.blackduck.notification.NotificationDetailResults;
import com.synopsys.integration.blackduck.notification.content.PolicyOverrideNotificationContent;
import com.synopsys.integration.blackduck.notification.content.RuleViolationClearedNotificationContent;
import com.synopsys.integration.blackduck.notification.content.RuleViolationNotificationContent;
import com.synopsys.integration.blackduck.notification.content.VulnerabilityNotificationContent;
import com.synopsys.integration.blackduck.notification.content.VulnerabilitySourceQualifiedId;
import com.synopsys.integration.blackduck.notification.content.detail.NotificationContentDetail;
import com.synopsys.integration.blackduck.notification.content.detail.NotificationContentDetailFactory;
import com.synopsys.integration.blackduck.rest.BlackDuckRestConnection;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public class NotificationGeneratorUtils {

    private NotificationGeneratorUtils() {

    }

    public static NotificationView createNotificationView(final NotificationType type) {
        return createNotificationView(new Date(), type);
    }

    public static NotificationView createNotificationView(final Date createdAt, final NotificationType type) {
        final NotificationView view = new NotificationView();
        view.setContentType("application/json");
        view.setCreatedAt(createdAt);
        view.setType(type);
        return view;
    }

    public static CommonNotificationView createCommonNotificationView(final NotificationView view) {
        return new CommonNotificationView(view);
    }

    public static List<NotificationDetailResult> createNotificationDetailList(final NotificationView view, final RuleViolationNotificationContent content) {
        final NotificationContentDetailFactory factory = new NotificationContentDetailFactory(null);
        final CommonNotificationView commonNotificationView = createCommonNotificationView(view);
        final String notificationGroup = NotificationContentDetail.CONTENT_KEY_GROUP_POLICY;
        final List<NotificationContentDetail> notificationContentDetails = new ArrayList<>();
        factory.populateContentDetails(notificationContentDetails, notificationGroup, content);

        return Arrays.asList(new NotificationDetailResult(content, commonNotificationView.getContentType(), commonNotificationView.getCreatedAt(), commonNotificationView.getType(), notificationGroup,
                commonNotificationView.getNotificationState(), notificationContentDetails));
    }

    public static List<NotificationDetailResult> createNotificationDetailList(final NotificationView view, final RuleViolationClearedNotificationContent content) {
        final NotificationContentDetailFactory factory = new NotificationContentDetailFactory(null);
        final CommonNotificationView commonNotificationView = createCommonNotificationView(view);
        final String notificationGroup = NotificationContentDetail.CONTENT_KEY_GROUP_POLICY;
        final List<NotificationContentDetail> notificationContentDetails = new ArrayList<>();
        factory.populateContentDetails(notificationContentDetails, notificationGroup, content);

        return Arrays.asList(new NotificationDetailResult(content, commonNotificationView.getContentType(), commonNotificationView.getCreatedAt(), commonNotificationView.getType(), notificationGroup,
                commonNotificationView.getNotificationState(), notificationContentDetails));
    }

    public static List<NotificationDetailResult> createNotificationDetailList(final NotificationView view, final PolicyOverrideNotificationContent content) {
        final NotificationContentDetailFactory factory = new NotificationContentDetailFactory(null);
        final CommonNotificationView commonNotificationView = createCommonNotificationView(view);
        final String notificationGroup = NotificationContentDetail.CONTENT_KEY_GROUP_POLICY;
        final List<NotificationContentDetail> notificationContentDetails = new ArrayList<>();
        factory.populateContentDetails(notificationContentDetails, notificationGroup, content);

        return Arrays.asList(new NotificationDetailResult(content, commonNotificationView.getContentType(), commonNotificationView.getCreatedAt(), commonNotificationView.getType(), notificationGroup,
                commonNotificationView.getNotificationState(), notificationContentDetails));
    }

    public static NotificationDetailResult createNotificationDetailList(final NotificationView view, final VulnerabilityNotificationContent content) {
        final NotificationContentDetailFactory factory = new NotificationContentDetailFactory(null);
        final CommonNotificationView commonNotificationView = createCommonNotificationView(view);
        final String notificationGroup = NotificationContentDetail.CONTENT_KEY_GROUP_VULNERABILITY;
        final List<NotificationContentDetail> notificationContentDetails = new ArrayList<>();
        factory.populateContentDetails(notificationContentDetails, notificationGroup, content);

        return new NotificationDetailResult(content, commonNotificationView.getContentType(), commonNotificationView.getCreatedAt(), commonNotificationView.getType(), notificationGroup,
                commonNotificationView.getNotificationState(), notificationContentDetails);
    }

    public static NotificationDetailResults createNotificationResults(final List<NotificationDetailResult> detailList) {
        final Date createdAt = detailList.get(detailList.size() - 1).getCreatedAt();
        final NotificationDetailResults results = new NotificationDetailResults(detailList, Optional.of(createdAt), Optional.of(RestConstants.formatDate(createdAt)));
        return results;
    }

    public static NotificationDetailResults createEmptyNotificationResults() {
        return new NotificationDetailResults(Collections.emptyList(), Optional.empty(), Optional.empty());
    }

    @SuppressWarnings("unchecked")
    public static NotificationDetailResults initializeTestData(final BlackDuckProperties blackDuckProperties, final ComponentVersionView versionView, final VulnerabilityNotificationContent content, final BlackDuckBucket bucket)
            throws IntegrationException {
        final BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        final BlackDuckService blackDuckService = Mockito.mock(BlackDuckService.class);
        final BlackDuckBucketService bucketService = Mockito.mock(BlackDuckBucketService.class);
        final List<VulnerabilityV2View> vulnerabilityViewList = createVulnerabilityList();
        final BlackDuckRestConnection restConnection = Mockito.mock(BlackDuckRestConnection.class);

        Mockito.when(blackDuckProperties.createRestConnectionAndLogErrors(Mockito.any())).thenReturn(Optional.of(restConnection));
        Mockito.when(blackDuckProperties.createBlackDuckServicesFactory(Mockito.any(), Mockito.any())).thenReturn(blackDuckServicesFactory);
        Mockito.when(blackDuckServicesFactory.createBlackDuckService()).thenReturn(blackDuckService);
        Mockito.when(blackDuckServicesFactory.createBlackDuckBucketService()).thenReturn(bucketService);
        Mockito.when(blackDuckService.getResponse(Mockito.any(UriSingleResponse.class))).thenReturn(versionView);
        Mockito.when(blackDuckService.getAllResponses(versionView, ComponentVersionView.VULNERABILITIES_LINK_RESPONSE)).thenReturn(vulnerabilityViewList);
        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.VULNERABILITY);

        createCommonContentData(content);

        final NotificationDetailResult detail = NotificationGeneratorUtils.createNotificationDetailList(view, content);
        final NotificationDetailResults notificationResults = NotificationGeneratorUtils.createNotificationResults(Arrays.asList(detail));
        // need to map the component version uri to a view in order for the processing rule to work
        // otherwise the rule will always have an empty list
        bucket.addValid(content.componentVersion, versionView);
        return notificationResults;
    }

    public static void createCommonContentData(final VulnerabilityNotificationContent content) {
        final AffectedProjectVersion affectedProjectVersion = new AffectedProjectVersion();
        affectedProjectVersion.setProjectName("VulnerableProjectName");
        affectedProjectVersion.setProjectVersionName("1.2.3");
        affectedProjectVersion.setProjectVersion("projectURL");
        affectedProjectVersion.setComponentIssueUrl("componentIssueUrl");

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
        vuln_1.setName("1");
        vuln_1.setSeverity("LOW");
        vuln_1.setMeta(new ResourceMetadata());
        vuln_1.getMeta().setHref("href_1");

        final VulnerabilityV2View vuln_2 = new VulnerabilityV2View();
        vuln_2.setName("2");
        vuln_2.setSeverity("LOW");
        vuln_2.setMeta(new ResourceMetadata());
        vuln_2.getMeta().setHref("href_2");

        final VulnerabilityV2View vuln_3 = new VulnerabilityV2View();
        vuln_3.setName("3");
        vuln_3.setSeverity("LOW");
        vuln_3.setMeta(new ResourceMetadata());
        vuln_3.getMeta().setHref("href_3");

        final VulnerabilityV2View vuln_4 = new VulnerabilityV2View();
        vuln_4.setName("4");
        vuln_4.setSeverity("MEDIUM");
        vuln_4.setMeta(new ResourceMetadata());
        vuln_4.getMeta().setHref("href_4");

        final VulnerabilityV2View vuln_5 = new VulnerabilityV2View();
        vuln_5.setName("5");
        vuln_5.setSeverity("HIGH");
        vuln_5.setMeta(new ResourceMetadata());
        vuln_5.getMeta().setHref("href_5");

        final VulnerabilityV2View vuln_6 = new VulnerabilityV2View();
        vuln_6.setName("6");
        vuln_6.setSeverity("HIGH");
        vuln_6.setMeta(new ResourceMetadata());
        vuln_6.getMeta().setHref("href_6");

        final VulnerabilityV2View vuln_7 = new VulnerabilityV2View();
        vuln_7.setName("7");
        vuln_7.setSeverity("HIGH");
        vuln_7.setMeta(new ResourceMetadata());
        vuln_7.getMeta().setHref("href_7");

        final VulnerabilityV2View vuln_8 = new VulnerabilityV2View();
        vuln_8.setName("8");
        vuln_8.setSeverity("HIGH");
        vuln_8.setMeta(new ResourceMetadata());
        vuln_8.getMeta().setHref("href_8");

        final VulnerabilityV2View vuln_9 = new VulnerabilityV2View();
        vuln_9.setName("9");
        vuln_9.setSeverity("HIGH");
        vuln_9.setMeta(new ResourceMetadata());
        vuln_9.getMeta().setHref("href_9");

        final VulnerabilityV2View vuln_10 = new VulnerabilityV2View();
        vuln_10.setName("10");
        vuln_10.setSeverity("HIGH");
        vuln_10.setMeta(new ResourceMetadata());
        vuln_10.getMeta().setHref("href_10");

        final VulnerabilityV2View vuln_11 = new VulnerabilityV2View();
        vuln_11.setName("11");
        vuln_11.setSeverity("HIGH");
        vuln_11.setMeta(new ResourceMetadata());
        vuln_11.getMeta().setHref("href_11");

        return Arrays.asList(vuln_1, vuln_2, vuln_3, vuln_4, vuln_5, vuln_6, vuln_7, vuln_8, vuln_9, vuln_10, vuln_11);
    }
}
