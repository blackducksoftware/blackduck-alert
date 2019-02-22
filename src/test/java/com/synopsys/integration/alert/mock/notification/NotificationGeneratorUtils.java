package com.synopsys.integration.alert.mock.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.synopsys.integration.blackduck.api.generated.component.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.api.manual.component.AffectedProjectVersion;
import com.synopsys.integration.blackduck.api.manual.component.VulnerabilityNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.VulnerabilitySourceQualifiedId;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;

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

    public static void createCommonContentData(final VulnerabilityNotificationContent content) {
        final AffectedProjectVersion affectedProjectVersion = new AffectedProjectVersion();
        affectedProjectVersion.setProjectName("VulnerableProjectName");
        affectedProjectVersion.setProjectVersionName("1.2.3");
        affectedProjectVersion.setProjectVersion("projectURL");
        affectedProjectVersion.setComponentIssueUrl("componentIssueUrl");

        content.setComponentVersion("componentversionurl");
        content.setComponentName("VulnerableComponent");
        content.setVersionName("1.2.3");
        content.setComponentVersionOriginName("originName");
        content.setAffectedProjectVersions(Arrays.asList(affectedProjectVersion));
        content.setComponentVersionOriginId("originId");
    }

    public static List<VulnerabilitySourceQualifiedId> createSourceIdList(final String... ids) {
        final List<VulnerabilitySourceQualifiedId> sourceIdList = new ArrayList<>(ids.length);
        for (final String id : ids) {
            final VulnerabilitySourceQualifiedId vuln = new VulnerabilitySourceQualifiedId();
            vuln.setVulnerabilityId(id);
            sourceIdList.add(vuln);
        }
        return sourceIdList;
    }

    public static List<VulnerabilityView> createVulnerabilityList() {
        final VulnerabilityView vuln_1 = new VulnerabilityView();
        vuln_1.setName("1");
        vuln_1.setSeverity("LOW");
        vuln_1.setMeta(new ResourceMetadata());
        vuln_1.getMeta().setHref("href_1");

        final VulnerabilityView vuln_2 = new VulnerabilityView();
        vuln_2.setName("2");
        vuln_2.setSeverity("LOW");
        vuln_2.setMeta(new ResourceMetadata());
        vuln_2.getMeta().setHref("href_2");

        final VulnerabilityView vuln_3 = new VulnerabilityView();
        vuln_3.setName("3");
        vuln_3.setSeverity("LOW");
        vuln_3.setMeta(new ResourceMetadata());
        vuln_3.getMeta().setHref("href_3");

        final VulnerabilityView vuln_4 = new VulnerabilityView();
        vuln_4.setName("4");
        vuln_4.setSeverity("MEDIUM");
        vuln_4.setMeta(new ResourceMetadata());
        vuln_4.getMeta().setHref("href_4");

        final VulnerabilityView vuln_5 = new VulnerabilityView();
        vuln_5.setName("5");
        vuln_5.setSeverity("HIGH");
        vuln_5.setMeta(new ResourceMetadata());
        vuln_5.getMeta().setHref("href_5");

        final VulnerabilityView vuln_6 = new VulnerabilityView();
        vuln_6.setName("6");
        vuln_6.setSeverity("HIGH");
        vuln_6.setMeta(new ResourceMetadata());
        vuln_6.getMeta().setHref("href_6");

        final VulnerabilityView vuln_7 = new VulnerabilityView();
        vuln_7.setName("7");
        vuln_7.setSeverity("HIGH");
        vuln_7.setMeta(new ResourceMetadata());
        vuln_7.getMeta().setHref("href_7");

        final VulnerabilityView vuln_8 = new VulnerabilityView();
        vuln_8.setName("8");
        vuln_8.setSeverity("HIGH");
        vuln_8.setMeta(new ResourceMetadata());
        vuln_8.getMeta().setHref("href_8");

        final VulnerabilityView vuln_9 = new VulnerabilityView();
        vuln_9.setName("9");
        vuln_9.setSeverity("HIGH");
        vuln_9.setMeta(new ResourceMetadata());
        vuln_9.getMeta().setHref("href_9");

        final VulnerabilityView vuln_10 = new VulnerabilityView();
        vuln_10.setName("10");
        vuln_10.setSeverity("HIGH");
        vuln_10.setMeta(new ResourceMetadata());
        vuln_10.getMeta().setHref("href_10");

        final VulnerabilityView vuln_11 = new VulnerabilityView();
        vuln_11.setName("11");
        vuln_11.setSeverity("HIGH");
        vuln_11.setMeta(new ResourceMetadata());
        vuln_11.getMeta().setHref("href_11");

        return Arrays.asList(vuln_1, vuln_2, vuln_3, vuln_4, vuln_5, vuln_6, vuln_7, vuln_8, vuln_9, vuln_10, vuln_11);
    }
}
