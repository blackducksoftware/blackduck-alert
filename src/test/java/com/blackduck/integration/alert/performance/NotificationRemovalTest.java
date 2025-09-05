/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.component.scheduling.descriptor.SchedulingDescriptorKey;
import com.blackduck.integration.alert.component.scheduling.workflow.PurgeTask;
import com.blackduck.integration.alert.performance.utility.IntegrationPerformanceTestRunnerLegacy;
import com.blackduck.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.blackduck.integration.alert.test.common.TestTags;
import com.blackduck.integration.alert.util.AlertIntegrationTest;
import com.blackduck.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;
import com.blackduck.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.blackduck.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.blackduck.integration.blackduck.api.manual.component.PolicyInfo;
import com.blackduck.integration.blackduck.api.manual.component.RuleViolationNotificationContent;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.blackduck.api.manual.view.RuleViolationNotificationView;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.Slf4jIntLogger;
import com.blackduck.integration.wait.ResilientJobConfig;
import com.blackduck.integration.wait.WaitJob;
import com.blackduck.integration.wait.WaitJobCondition;
import com.blackduck.integration.wait.tracker.WaitIntervalTracker;
import com.blackduck.integration.wait.tracker.WaitIntervalTrackerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Tag(TestTags.DEFAULT_PERFORMANCE)
@AlertIntegrationTest
class NotificationRemovalTest {
    private static final IntLogger LOGGER = new Slf4jIntLogger(LoggerFactory.getLogger(NotificationRemovalTest.class));
    private static final Gson GSON = new GsonBuilder().create();
    private static final String PROJECT_NAME = "1234 - Test Project";
    private static final UUID JOB_ID = UUID.randomUUID();
    private static final int BATCH_SIZE = 1000;
    private static final String BLACKDUCK_PROVIDER_URL = "https://blackduck.server.example.com";

    @Autowired
    private NotificationAccessor notificationAccessor;
    @Autowired
    private ProcessingAuditAccessor processingAuditAccessor;
    @Autowired
    private SchedulingDescriptorKey schedulingDescriptorKey;
    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private SystemMessageAccessor systemMessageAccessor;
    @Autowired
    private TaskManager taskManager;
    @Autowired
    private ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    @Autowired
    private EventManager eventManager;

    private PurgeTask purgeTask;
    private ConfigurationModel providerConfig;
    private final DateTimeFormatter dateTimeFormatter = IntegrationPerformanceTestRunnerLegacy.createDateTimeFormatter();

    @AfterEach
    public void cleanup() {
        if (purgeTask != null) {
            taskManager.unregisterTask(purgeTask.getTaskName());
        }
        notificationAccessor.deleteNotificationsCreatedBefore(OffsetDateTime.now());

        if (providerConfig != null) {
            configurationModelConfigurationAccessor.deleteConfiguration(providerConfig.getConfigurationId());
        }
    }

    @Test
    @Ignore // performance test
    @Disabled
    void testDeletion() throws IntegrationException, InterruptedException {
        providerConfig = createBlackDuckConfiguration();
        OffsetDateTime testStartTime = OffsetDateTime.now();
        OffsetDateTime notificationCreatedAtTime = OffsetDateTime.now();
        // create 1000 processed notifications not for removal
        createABatchOfNotifications(providerConfig, testStartTime, true);
        // create 9000 for removal with varying dates and processed flags
        for (int index = 0; index < 9; index++) {
            boolean processed = index % 2 == 0 ? true : false;
            // update the createdAt time to be 1 month older
            notificationCreatedAtTime = notificationCreatedAtTime.minusMonths(1);
            createABatchOfNotifications(providerConfig, notificationCreatedAtTime, processed);
        }
        OffsetDateTime oldestNotificationCreationTime = notificationCreatedAtTime;
        purgeTask = new PurgeTask(schedulingDescriptorKey, taskScheduler, notificationAccessor, systemMessageAccessor, taskManager, configurationModelConfigurationAccessor);
        LocalDateTime startTime = LocalDateTime.now();
        purgeTask.runTask();

        WaitJobCondition waitJobCondition = () -> {
            List<AlertNotificationModel> notificationsInDatabase = getAllNotificationsInDatabase(oldestNotificationCreationTime, testStartTime);
            return notificationsInDatabase.size() == BATCH_SIZE && notificationsInDatabase.stream()
                .allMatch(AlertNotificationModel::getProcessed);
        };
        WaitIntervalTracker waitIntervalTracker = WaitIntervalTrackerFactory.createConstant(600, 1);
        ResilientJobConfig resilientJobConfig = new ResilientJobConfig(LOGGER, startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), waitIntervalTracker);
        boolean isComplete = WaitJob.waitFor(resilientJobConfig, waitJobCondition, "int performance test runner notification wait");

        logTimeElapsedWithMessage("Purge of notifications duration: %s", startTime, LocalDateTime.now());
        List<AlertNotificationModel> remainingNotifications = getAllNotificationsInDatabase(oldestNotificationCreationTime, testStartTime);

        assertTrue(isComplete);
        assertEquals(BATCH_SIZE, remainingNotifications.size());
    }

    // ==============
    // Helper methods
    // ==============

    public void logTimeElapsedWithMessage(String messageFormat, LocalDateTime start, LocalDateTime end) {
        //TODO log timing to a file
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        LOGGER.info(String.format(messageFormat, durationFormatted));
        LOGGER.info(String.format("Current time %s.", dateTimeFormatter.format(end)));
    }

    private List<AlertNotificationModel> getAllNotificationsInDatabase(OffsetDateTime oldestNotificationCreationTime, OffsetDateTime testStartTime) {
        List<AlertNotificationModel> notifications = new LinkedList<>();
        int pageSize = 100;
        AlertPagedModel<AlertNotificationModel> page = notificationAccessor
            .findByCreatedAtBetween(oldestNotificationCreationTime, testStartTime, AlertPagedModel.DEFAULT_PAGE_NUMBER, pageSize);
        int currentPage = page.getCurrentPage();
        int totalPages = page.getTotalPages();
        notifications.addAll(page.getModels());
        while (!page.getModels().isEmpty() || currentPage < totalPages) {
            page = notificationAccessor.findByCreatedAtBetween(oldestNotificationCreationTime, testStartTime, currentPage + 1, pageSize);
            currentPage = page.getCurrentPage();
            totalPages = page.getTotalPages();
            notifications.addAll(page.getModels());
        }
        return notifications;
    }

    private void createABatchOfNotifications(ConfigurationModel providerConfig, OffsetDateTime notificationCreationTime, boolean batchOfProcessedNotifications) {
        int count = BATCH_SIZE;
        List<AlertNotificationModel> notifications = new ArrayList<>(BATCH_SIZE);

        ProviderDetails providerDetails = new ProviderDetails(providerConfig.getConfigurationId(), new LinkableItem("Black Duck", "bd-server", BLACKDUCK_PROVIDER_URL));
        for (int index = 0; index < count; index++) {
            RuleViolationNotificationView ruleViolationNotificationView = createRuleViolationNotificationView(PROJECT_NAME);

            String notificationContentString = GSON.toJson(ruleViolationNotificationView);
            notifications.add(createNotification(providerDetails,
                NotificationType.RULE_VIOLATION.name(),
                notificationContentString,
                notificationCreationTime,
                batchOfProcessedNotifications
            ));
        }
        notifications = notificationAccessor.saveAllNotifications(notifications);
        createAuditEntries(notifications);
    }

    private ConfigurationModel createBlackDuckConfiguration() {
        ConfigurationFieldModel blackDuckURLField = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        blackDuckURLField.setFieldValue(BLACKDUCK_PROVIDER_URL);

        ConfigurationFieldModel blackDuckAPITokenField = ConfigurationFieldModel.createSensitive(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        blackDuckAPITokenField.setFieldValue("");

        ConfigurationFieldModel blackDuckTimeoutField = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT);
        blackDuckTimeoutField.setFieldValue("300");

        BlackDuckProviderKey blackDuckProviderKey = new BlackDuckProviderKey();
        return configurationModelConfigurationAccessor
            .createConfiguration(blackDuckProviderKey, ConfigContextEnum.GLOBAL, List.of(blackDuckURLField, blackDuckAPITokenField, blackDuckTimeoutField));
    }

    private void createAuditEntries(List<AlertNotificationModel> notifications) {
        Set<Long> notificationIds = notifications.stream()
            .map(AlertNotificationModel::getId)
            .collect(Collectors.toSet());
        processingAuditAccessor.createOrUpdatePendingAuditEntryForJob(JOB_ID, notificationIds);
    }

    private RuleViolationNotificationView createRuleViolationNotificationView(String projectName) {
        RuleViolationNotificationContent notificationContent = new RuleViolationNotificationContent();
        notificationContent.setProjectName(projectName);
        notificationContent.setProjectVersionName("a-project-version");
        notificationContent.setProjectVersion("https://a-project-version");
        notificationContent.setComponentVersionsInViolation(1);

        PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.setPolicy("https://a-policy");
        policyInfo.setPolicyName("a policy");
        policyInfo.setSeverity(PolicyRuleSeverityType.MAJOR.name());
        notificationContent.setPolicyInfos(List.of(policyInfo));

        ComponentVersionStatus componentVersionStatus = new ComponentVersionStatus();
        componentVersionStatus.setBomComponent("https://bom-component");
        componentVersionStatus.setComponentName("component name");
        componentVersionStatus.setComponent("https://component");
        componentVersionStatus.setComponentVersionName("component-version name");
        componentVersionStatus.setComponentVersion("https://component-version");
        componentVersionStatus.setPolicies(List.of(policyInfo.getPolicy()));
        componentVersionStatus.setBomComponentVersionPolicyStatus(ProjectVersionComponentPolicyStatusType.IN_VIOLATION.name());
        componentVersionStatus.setComponentIssueLink("https://component-issues");
        notificationContent.setComponentVersionStatuses(List.of(componentVersionStatus));

        RuleViolationNotificationView notificationView = new RuleViolationNotificationView();
        notificationView.setContent(notificationContent);
        notificationView.setType(NotificationType.RULE_VIOLATION);

        return notificationView;
    }

    private AlertNotificationModel createNotification(
        ProviderDetails providerDetails,
        String notificationType,
        String notificationContent,
        OffsetDateTime notificationCreationTime,
        boolean processed
    ) {
        return new AlertNotificationModel(
            null,
            providerDetails.getProviderConfigId(),
            providerDetails.getProvider().getLabel(),
            providerDetails.getProvider().getValue(),
            notificationType,
            notificationContent,
            notificationCreationTime,
            notificationCreationTime,
            processed,
            String.format("content-id-%s", UUID.randomUUID()),
            false
        );
    }
}
