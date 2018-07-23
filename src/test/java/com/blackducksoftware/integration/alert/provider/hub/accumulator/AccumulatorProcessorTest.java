package com.blackducksoftware.integration.alert.provider.hub.accumulator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.event.AlertEvent;
import com.blackducksoftware.integration.alert.common.model.NotificationModel;
import com.blackducksoftware.integration.alert.common.model.NotificationModels;
import com.blackducksoftware.integration.alert.config.GlobalProperties;
import com.blackducksoftware.integration.alert.mock.notification.NotificationGeneratorUtils;
import com.blackducksoftware.integration.alert.workflow.processor.NotificationTypeProcessor;
import com.blackducksoftware.integration.alert.workflow.processor.policy.PolicyNotificationTypeProcessor;
import com.blackducksoftware.integration.alert.workflow.processor.vulnerability.VulnerabilityNotificationTypeProcessor;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResult;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.notification.content.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.notification.content.PolicyInfo;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;
import com.google.gson.Gson;

public class AccumulatorProcessorTest {

    private Gson gson;
    private ContentConverter contentConverter;

    @Before
    public void init() {
        gson = new Gson();
        contentConverter = new ContentConverter(gson, new DefaultConversionService());
    }

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
        final HubAccumulatorProcessor hubAccumulatorProcessor = new HubAccumulatorProcessor(globalProperties, processorList, contentConverter);

        final AlertEvent storeEvent = hubAccumulatorProcessor.process(notificationData);

        assertNotNull(storeEvent);
        final Optional<NotificationModels> optionalModel = Optional.ofNullable(contentConverter.getJsonContent(storeEvent.getContent(), NotificationModels.class));
        final List<NotificationModel> notifications = optionalModel.get().getNotificationModelList();

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
        final HubAccumulatorProcessor hubAccumulatorProcessor = new HubAccumulatorProcessor(globalProperties, null, contentConverter);

        final AlertEvent storeEventNull = hubAccumulatorProcessor.process(notificationData);
        assertNotNull(storeEventNull);
        final Optional<NotificationModels> optionalModel = Optional.ofNullable(contentConverter.getJsonContent(storeEventNull.getContent(), NotificationModels.class));
        assertTrue(optionalModel.get().getNotificationModelList().isEmpty());
    }

    @Test
    public void testProcessNullList() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final HubAccumulatorProcessor hubAccumulatorProcessor = new HubAccumulatorProcessor(globalProperties, null, contentConverter);
        final AlertEvent nullStoreEvent = hubAccumulatorProcessor.process(null);
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
        final HubAccumulatorProcessor hubAccumulatorProcessor = new HubAccumulatorProcessor(globalProperties, processorList, contentConverter);

        final AlertEvent storeEvent = hubAccumulatorProcessor.process(notificationData);
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
