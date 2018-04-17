package com.blackducksoftware.integration.hub.alert.accumulator;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.NotificationManager;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.hub.alert.enumeration.VulnerabilityOperationEnum;
import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.alert.processor.VulnerabilityCache;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.notification.ItemTypeEnum;
import com.blackducksoftware.integration.hub.notification.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.notification.NotificationContentItem;
import com.blackducksoftware.integration.hub.notification.NotificationEvent;
import com.blackducksoftware.integration.hub.notification.ProjectVersionModel;

public class AccumulatorWriterTest {

    @Test
    public void testWrite() throws Exception {
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);

        final AccumulatorWriter accumulatorWriter = new AccumulatorWriter(notificationManager, channelTemplateManager);

        final String eventKey = "_event_key_";
        final NotificationCategoryEnum categoryType = NotificationCategoryEnum.HIGH_VULNERABILITY;

        final NotificationEvent notificationEvent = new NotificationEvent(eventKey, categoryType, generateDataSet());
        final DBStoreEvent storeEvent = new DBStoreEvent(Arrays.asList(notificationEvent));

        accumulatorWriter.write(Arrays.asList(storeEvent));

        Mockito.verify(channelTemplateManager).sendEvent(Mockito.any());
    }

    @Test
    public void testWriteNullData() throws Exception {
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);

        final AccumulatorWriter accumulatorWriter = new AccumulatorWriter(notificationManager, channelTemplateManager);

        final String eventKey = "_event_key_";
        final NotificationCategoryEnum categoryType = NotificationCategoryEnum.HIGH_VULNERABILITY;

        final NotificationEvent notificationEvent = new NotificationEvent(eventKey, categoryType, generateBadeDataSet());
        final DBStoreEvent storeEvent = new DBStoreEvent(Arrays.asList(notificationEvent));

        accumulatorWriter.write(Arrays.asList(storeEvent));

        Mockito.verify(channelTemplateManager).sendEvent(Mockito.any());
    }

    private Map<String, Object> generateDataSet() {
        final Map<String, Object> dataSet = new HashMap<>();

        final Date createdAt = new Date();
        final ProjectVersionModel projectVersionModel = new ProjectVersionModel();
        projectVersionModel.setProjectLink("New project link");
        final String componentName = "notification test";
        final ComponentVersionView componentVersionView = new ComponentVersionView();
        final String componentVersionUrl = "sss";
        final String componentIssueUrl = "ddd";
        dataSet.put(NotificationEvent.DATA_SET_KEY_NOTIFICATION_CONTENT, new NotificationContentItem(createdAt, projectVersionModel, componentName, componentVersionView, componentVersionUrl, componentIssueUrl));

        dataSet.put(ItemTypeEnum.RULE.name(), "policyRuleName");
        dataSet.put(ItemTypeEnum.PERSON.name(), "policyUserName");
        dataSet.put(VulnerabilityCache.VULNERABILITY_OPERATION, VulnerabilityOperationEnum.ADD.name());

        final Set<String> vulnSet = new HashSet<>();
        vulnSet.add("vulnerabilityId");
        dataSet.put(VulnerabilityCache.VULNERABILITY_ID_SET, vulnSet);

        return dataSet;
    }

    private Map<String, Object> generateBadeDataSet() {
        final Map<String, Object> dataSet = new HashMap<>();

        final Date createdAt = new Date();
        final ProjectVersionModel projectVersionModel = new ProjectVersionModel();
        projectVersionModel.setProjectLink("New project link");
        final String componentName = null;
        final ComponentVersionView componentVersionView = null;
        final String componentVersionUrl = null;
        final String componentIssueUrl = null;
        dataSet.put(NotificationEvent.DATA_SET_KEY_NOTIFICATION_CONTENT, new NotificationContentItem(createdAt, projectVersionModel, componentName, componentVersionView, componentVersionUrl, componentIssueUrl));

        dataSet.put(ItemTypeEnum.RULE.name(), "policyRuleName");
        dataSet.put(ItemTypeEnum.PERSON.name(), "policyUserName");
        dataSet.put(VulnerabilityCache.VULNERABILITY_OPERATION, VulnerabilityOperationEnum.ADD.name());

        final Set<String> vulnSet = new HashSet<>();
        vulnSet.add("vulnerabilityId");
        dataSet.put(VulnerabilityCache.VULNERABILITY_ID_SET, vulnSet);

        return dataSet;
    }
}
