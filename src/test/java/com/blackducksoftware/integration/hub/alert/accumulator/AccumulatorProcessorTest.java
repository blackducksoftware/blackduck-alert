package com.blackducksoftware.integration.hub.alert.accumulator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.alert.mock.notification.NotificationGeneratorUtils;
import com.blackducksoftware.integration.hub.alert.processor.NotificationTypeProcessor;
import com.blackducksoftware.integration.hub.alert.processor.policy.PolicyNotificationTypeProcessor;
import com.blackducksoftware.integration.hub.alert.processor.vulnerability.VulnerabilityNotificationTypeProcessor;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResult;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.notification.content.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.notification.content.PolicyInfo;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;

public class AccumulatorProcessorTest {

    @Test
    public void testProcess() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final ComponentVersionView versionView = new ComponentVersionView();

        final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        content.newVulnerabilityCount = 4;
        content.updatedVulnerabilityCount = 3;
        content.deletedVulnerabilityCount = 4;
        content.newVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("1", "2", "3", "10");
        content.updatedVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("2", "4", "11");
        content.deletedVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("5", "6", "10", "11");

        final List<NotificationDetailResult> resultList = new ArrayList<>();
        final NotificationDetailResults vulnerabilityResults = NotificationGeneratorUtils.initializeTestData(globalProperties, versionView, content);
        resultList.addAll(vulnerabilityResults.getResults());
        resultList.addAll(createPolicyViolationNotification());

        final NotificationDetailResults notificationData = new NotificationDetailResults(resultList, vulnerabilityResults.getLatestNotificationCreatedAtDate(), vulnerabilityResults.getLatestNotificationCreatedAtString(),
                vulnerabilityResults.getHubBucket());
        final PolicyNotificationTypeProcessor policyNotificationTypeProcessor = new PolicyNotificationTypeProcessor();
        final VulnerabilityNotificationTypeProcessor vulnerabilityNotificationTypeProcessor = new VulnerabilityNotificationTypeProcessor();
        final List<NotificationTypeProcessor> processorList = Arrays.asList(policyNotificationTypeProcessor, vulnerabilityNotificationTypeProcessor);
        final AccumulatorProcessor accumulatorProcessor = new AccumulatorProcessor(globalProperties, processorList);

        final DBStoreEvent storeEvent = accumulatorProcessor.process(notificationData);

        assertNotNull(storeEvent);

        final List<NotificationModel> notifications = storeEvent.getNotificationList();

        assertFalse(notifications.isEmpty());
    }

    @Test
    public void testProcessorListNull() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final ComponentVersionView versionView = new ComponentVersionView();
        final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        content.newVulnerabilityCount = 4;
        content.updatedVulnerabilityCount = 3;
        content.deletedVulnerabilityCount = 4;
        content.newVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("1", "2", "3", "10");
        content.updatedVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("2", "4", "11");
        content.deletedVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("5", "6", "10", "11");

        final List<NotificationDetailResult> resultList = new ArrayList<>();
        final NotificationDetailResults vulnerabilityResults = NotificationGeneratorUtils.initializeTestData(globalProperties, versionView, content);
        resultList.addAll(vulnerabilityResults.getResults());
        resultList.addAll(createPolicyViolationNotification());

        final NotificationDetailResults notificationData = new NotificationDetailResults(resultList, vulnerabilityResults.getLatestNotificationCreatedAtDate(), vulnerabilityResults.getLatestNotificationCreatedAtString(),
                vulnerabilityResults.getHubBucket());
        final AccumulatorProcessor accumulatorProcessor = new AccumulatorProcessor(globalProperties, null);

        final DBStoreEvent storeEventNull = accumulatorProcessor.process(notificationData);
        assertNotNull(storeEventNull);
        assertTrue(storeEventNull.getNotificationList().isEmpty());
    }

    @Test
    public void testProcessNullList() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final AccumulatorProcessor accumulatorProcessor = new AccumulatorProcessor(globalProperties, null);
        final DBStoreEvent nullStoreEvent = accumulatorProcessor.process(null);
        assertNull(nullStoreEvent);
    }

    @Test
    public void testProcessThrowsException() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final ComponentVersionView versionView = new ComponentVersionView();

        final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        content.newVulnerabilityCount = 4;
        content.updatedVulnerabilityCount = 3;
        content.deletedVulnerabilityCount = 4;
        content.newVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("1", "2", "3", "10");
        content.updatedVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("2", "4", "11");
        content.deletedVulnerabilityIds = NotificationGeneratorUtils.createSourceIdList("5", "6", "10", "11");

        final List<NotificationDetailResult> resultList = new ArrayList<>();
        final NotificationDetailResults vulnerabilityResults = NotificationGeneratorUtils.initializeTestData(globalProperties, versionView, content);
        resultList.addAll(vulnerabilityResults.getResults());
        resultList.addAll(createPolicyViolationNotification());

        final NotificationDetailResults notificationData = new NotificationDetailResults(resultList, vulnerabilityResults.getLatestNotificationCreatedAtDate(), vulnerabilityResults.getLatestNotificationCreatedAtString(),
                vulnerabilityResults.getHubBucket());
        final PolicyNotificationTypeProcessor policyNotificationTypeProcessor = Mockito.mock(PolicyNotificationTypeProcessor.class);
        final VulnerabilityNotificationTypeProcessor vulnerabilityNotificationTypeProcessor = Mockito.mock(VulnerabilityNotificationTypeProcessor.class);
        Mockito.doThrow(new RuntimeException("Test Exception")).when(policyNotificationTypeProcessor).isApplicable(Mockito.any());
        Mockito.doThrow(new RuntimeException("Test Exception")).when(vulnerabilityNotificationTypeProcessor).isApplicable(Mockito.any());
        final List<NotificationTypeProcessor> processorList = Arrays.asList(policyNotificationTypeProcessor, vulnerabilityNotificationTypeProcessor);
        final AccumulatorProcessor accumulatorProcessor = new AccumulatorProcessor(globalProperties, processorList);

        final DBStoreEvent storeEvent = accumulatorProcessor.process(notificationData);
        assertNull(storeEvent);
    }

    private List<NotificationDetailResult> createPolicyViolationNotification() {
        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.RULE_VIOLATION);
        final RuleViolationNotificationContent content = new RuleViolationNotificationContent();
        content.projectName = "PolicyProject";
        content.projectVersionName = "1.0.0";
        content.projectVersion = "project version url";
        content.componentVersionsInViolation = 1;

        final PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.policyName = "PolicyViolationName";
        policyInfo.policy = "policyUrl";
        content.policyInfos = Arrays.asList(policyInfo);

        final ComponentVersionStatus componentVersionStatus = new ComponentVersionStatus();
        componentVersionStatus.componentName = "notification test component";
        componentVersionStatus.componentVersionName = "1.2.3";
        componentVersionStatus.component = "component url";
        componentVersionStatus.componentVersion = "component version url";
        componentVersionStatus.componentIssueLink = "issuesLink";
        componentVersionStatus.policies = Arrays.asList(policyInfo.policy);
        componentVersionStatus.bomComponentVersionPolicyStatus = "IN_VIOLATION";
        content.componentVersionStatuses = Arrays.asList(componentVersionStatus);
        final List<NotificationDetailResult> detailList = NotificationGeneratorUtils.createNotificationDetailList(view, content);
        return detailList;
    }
}
