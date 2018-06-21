package com.blackducksoftware.integration.hub.alert.processor.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.alert.mock.notification.NotificationGeneratorUtils;
import com.blackducksoftware.integration.hub.alert.model.NotificationModel;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResult;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.notification.content.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.notification.content.PolicyInfo;
import com.blackducksoftware.integration.hub.notification.content.PolicyOverrideNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationClearedNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetail;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;

public class PolicyNotificationTypeProcessorTest {
    private static final String COMPONENT_ISSUE_URL = "issuesLink";
    private static final String COMPONENT_VERSION_URL = "component version url";
    private static final String COMPONENT_URL = "component url";
    private static final String POLICY_URL = "policyUrl";
    private static final String POLICY_NAME = "PolicyViolation";
    private static final String PROJECT_VERSION_URL = "policy url";
    private static final String PROJECT_NAME = "PolicyProject";
    private static final String COMPONENT_VERSION_NAME = "1.2.3";
    private static final String COMPONENT_NAME = "notification test component";
    private PolicyNotificationTypeProcessor processor;

    @Before
    public void initProcessor() {

        processor = new PolicyNotificationTypeProcessor();
    }

    @Test
    public void testPolicyViolation() {
        final List<NotificationDetailResult> detailList = createPolicyViolationNotification();

        final NotificationDetailResults notificationResults = NotificationGeneratorUtils.createNotificationResults(detailList);
        final HubBucket bucket = notificationResults.getHubBucket();
        final List<NotificationModel> modelList = new ArrayList<>();
        notificationResults.getResults().forEach(detail -> {
            modelList.addAll(processor.process(null, detail, bucket));
        });

        assertEquals(1, modelList.size());
        final NotificationModel model = modelList.get(0);

        final NotificationContentDetail detail = detailList.get(0).getNotificationContentDetails().get(0);
        assertEquals(detail.getContentDetailKey(), model.getEventKey());
        assertEquals(NotificationCategoryEnum.POLICY_VIOLATION, model.getNotificationType());
        assertEquals(detail.getProjectName().get(), model.getProjectName());
        assertEquals(detail.getProjectVersionName().get(), model.getProjectVersion());
        assertEquals(detail.getComponentName().get(), model.getComponentName());
        assertEquals(detail.getComponentVersionName().get(), model.getComponentVersion());
        assertEquals(detail.getPolicyName().get(), model.getPolicyRuleName());
    }

    @Test
    public void testPolicyCleared() {
        final List<NotificationDetailResult> detailList = createPolicyClearedNotification();

        final NotificationDetailResults notificationResults = NotificationGeneratorUtils.createNotificationResults(detailList);
        final HubBucket bucket = notificationResults.getHubBucket();
        final List<NotificationModel> modelList = new ArrayList<>();
        notificationResults.getResults().forEach(detail -> {
            modelList.addAll(processor.process(null, detail, bucket));
        });

        assertEquals(1, modelList.size());
        final NotificationModel model = modelList.get(0);

        final NotificationContentDetail detail = detailList.get(0).getNotificationContentDetails().get(0);
        assertEquals(detail.getContentDetailKey(), model.getEventKey());
        assertEquals(NotificationCategoryEnum.POLICY_VIOLATION_CLEARED, model.getNotificationType());
        assertEquals(detail.getProjectName().get(), model.getProjectName());
        assertEquals(detail.getProjectVersionName().get(), model.getProjectVersion());
        assertEquals(detail.getComponentName().get(), model.getComponentName());
        assertEquals(detail.getComponentVersionName().get(), model.getComponentVersion());
        assertEquals(detail.getPolicyName().get(), model.getPolicyRuleName());
    }

    @Test
    public void testPolicyOverride() {
        final List<NotificationDetailResult> detailList = createPolicyOverrideNotification();

        final NotificationDetailResults notificationResults = NotificationGeneratorUtils.createNotificationResults(detailList);
        final HubBucket bucket = notificationResults.getHubBucket();
        final List<NotificationModel> modelList = new ArrayList<>();
        notificationResults.getResults().forEach(detail -> {
            modelList.addAll(processor.process(null, detail, bucket));
        });

        assertEquals(1, modelList.size());
        final NotificationModel model = modelList.get(0);

        final NotificationContentDetail detail = detailList.get(0).getNotificationContentDetails().get(0);
        assertEquals(detail.getContentDetailKey(), model.getEventKey());
        assertEquals(NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE, model.getNotificationType());
        assertEquals(detail.getProjectName().get(), model.getProjectName());
        assertEquals(detail.getProjectVersionName().get(), model.getProjectVersion());
        assertEquals(detail.getComponentName().get(), model.getComponentName());
        assertEquals(detail.getComponentVersionName().get(), model.getComponentVersion());
        assertEquals(detail.getPolicyName().get(), model.getPolicyRuleName());
    }

    @Test
    public void testPolicyClearedCancel() {
        final List<NotificationDetailResult> violationList = createPolicyViolationNotification();
        final List<NotificationDetailResult> clearedList = createPolicyClearedNotification();
        final List<NotificationDetailResult> detailList = new ArrayList<>();
        detailList.addAll(violationList);
        detailList.addAll(clearedList);
        final NotificationDetailResults notificationResults = NotificationGeneratorUtils.createNotificationResults(detailList);
        final HubBucket bucket = notificationResults.getHubBucket();
        final List<NotificationModel> modelList = new ArrayList<>();
        notificationResults.getResults().forEach(detail -> {
            modelList.addAll(processor.process(null, detail, bucket));
        });

        assertFalse(modelList.isEmpty());
    }

    @Test
    public void testPolicyOverrideCancel() {
        final List<NotificationDetailResult> violationList = createPolicyViolationNotification();
        final List<NotificationDetailResult> overrideList = createPolicyOverrideNotification();
        final List<NotificationDetailResult> detailList = new ArrayList<>();
        detailList.addAll(violationList);
        detailList.addAll(overrideList);

        final NotificationDetailResults notificationResults = NotificationGeneratorUtils.createNotificationResults(detailList);
        final HubBucket bucket = notificationResults.getHubBucket();
        final List<NotificationModel> modelList = new ArrayList<>();
        notificationResults.getResults().forEach(detail -> {
            modelList.addAll(processor.process(null, detail, bucket));
        });

        assertFalse(modelList.isEmpty());
    }

    @Test
    public void testDuplicatePolicyClearedAdded() {
        final List<NotificationDetailResult> violationList = createPolicyViolationNotification();
        final List<NotificationDetailResult> clearedList = createPolicyClearedNotification();
        final List<NotificationDetailResult> duplicateClearedList = createPolicyClearedNotification();
        final List<NotificationDetailResult> detailList = new ArrayList<>();
        detailList.addAll(violationList);
        detailList.addAll(clearedList);
        detailList.addAll(duplicateClearedList);

        final NotificationDetailResults notificationResults = NotificationGeneratorUtils.createNotificationResults(detailList);
        final HubBucket bucket = notificationResults.getHubBucket();
        final List<NotificationModel> modelList = new ArrayList<>();
        notificationResults.getResults().forEach(detail -> {
            modelList.addAll(processor.process(null, detail, bucket));
        });

        assertEquals(3, modelList.size());
    }

    @Test
    public void testDuplicateOverrideAdded() {
        final List<NotificationDetailResult> violationList = createPolicyViolationNotification();
        final List<NotificationDetailResult> overrideList = createPolicyOverrideNotification();
        final List<NotificationDetailResult> duplicateOverrideList = createPolicyOverrideNotification();
        final List<NotificationDetailResult> detailList = new ArrayList<>();
        detailList.addAll(violationList);
        detailList.addAll(overrideList);
        detailList.addAll(duplicateOverrideList);

        final NotificationDetailResults notificationResults = NotificationGeneratorUtils.createNotificationResults(detailList);
        final HubBucket bucket = notificationResults.getHubBucket();
        final List<NotificationModel> modelList = new ArrayList<>();
        notificationResults.getResults().forEach(detail -> {
            modelList.addAll(processor.process(null, detail, bucket));
        });

        assertEquals(3, modelList.size());
    }

    private List<NotificationDetailResult> createPolicyViolationNotification() {
        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.RULE_VIOLATION);
        final RuleViolationNotificationContent content = new RuleViolationNotificationContent();
        content.projectName = PROJECT_NAME;
        content.projectVersionName = COMPONENT_VERSION_NAME;
        content.projectVersion = PROJECT_VERSION_URL;
        content.componentVersionsInViolation = 1;

        final PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.policyName = POLICY_NAME;
        policyInfo.policy = POLICY_URL;
        content.policyInfos = Arrays.asList(policyInfo);

        final ComponentVersionStatus componentVersionStatus = new ComponentVersionStatus();
        componentVersionStatus.componentName = COMPONENT_NAME;
        componentVersionStatus.componentVersionName = COMPONENT_VERSION_NAME;
        componentVersionStatus.component = COMPONENT_URL;
        componentVersionStatus.componentVersion = COMPONENT_VERSION_URL;
        componentVersionStatus.componentIssueLink = COMPONENT_ISSUE_URL;
        componentVersionStatus.policies = Arrays.asList(policyInfo.policy);
        componentVersionStatus.bomComponentVersionPolicyStatus = "IN_VIOLATION";
        content.componentVersionStatuses = Arrays.asList(componentVersionStatus);
        final List<NotificationDetailResult> detailList = NotificationGeneratorUtils.createNotificationDetailList(view, content);
        return detailList;
    }

    private List<NotificationDetailResult> createPolicyClearedNotification() {
        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.RULE_VIOLATION_CLEARED);

        final RuleViolationClearedNotificationContent content = new RuleViolationClearedNotificationContent();
        content.projectName = PROJECT_NAME;
        content.projectVersionName = COMPONENT_VERSION_NAME;
        content.projectVersion = PROJECT_VERSION_URL;
        content.componentVersionsCleared = 1;

        final PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.policyName = POLICY_NAME;
        policyInfo.policy = POLICY_URL;
        content.policyInfos = Arrays.asList(policyInfo);

        final ComponentVersionStatus componentVersionStatus = new ComponentVersionStatus();
        componentVersionStatus.componentName = COMPONENT_NAME;
        componentVersionStatus.componentVersionName = COMPONENT_VERSION_NAME;
        componentVersionStatus.component = COMPONENT_URL;
        componentVersionStatus.componentVersion = COMPONENT_VERSION_URL;
        componentVersionStatus.componentIssueLink = COMPONENT_ISSUE_URL;
        componentVersionStatus.policies = Arrays.asList(policyInfo.policy);
        componentVersionStatus.bomComponentVersionPolicyStatus = "VIOLATION_CLEARED";
        content.componentVersionStatuses = Arrays.asList(componentVersionStatus);
        final List<NotificationDetailResult> detailList = NotificationGeneratorUtils.createNotificationDetailList(view, content);
        return detailList;
    }

    private List<NotificationDetailResult> createPolicyOverrideNotification() {
        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.POLICY_OVERRIDE);
        final PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.policyName = POLICY_NAME;
        policyInfo.policy = POLICY_URL;

        final PolicyOverrideNotificationContent content = new PolicyOverrideNotificationContent();
        content.projectName = PROJECT_NAME;
        content.projectVersionName = COMPONENT_VERSION_NAME;
        content.projectVersion = PROJECT_VERSION_URL;
        content.componentName = COMPONENT_NAME;
        content.componentVersionName = COMPONENT_VERSION_NAME;
        content.componentVersion = COMPONENT_VERSION_URL;
        content.policyInfos = Arrays.asList(policyInfo);
        content.policies = Arrays.asList(policyInfo.policy);
        content.bomComponentVersionPolicyStatus = "POLICY_OVERRIDE";
        final List<NotificationDetailResult> detailList = NotificationGeneratorUtils.createNotificationDetailList(view, content);
        return detailList;
    }
}
