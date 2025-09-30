/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.database.job.api.DefaultNotificationAccessor;
import com.blackduck.integration.alert.database.job.api.StaticJobAccessor;
import com.blackduck.integration.alert.util.AlertIntegrationTest;
import com.blackduck.integration.blackduck.api.generated.enumeration.VulnerabilitySeverityType;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;

@AlertIntegrationTest
class DistributionJobRepositoryTestIT {
    // Distribution jobs related
    @Autowired private DistributionJobRepository distributionJobRepository;
    @Autowired private StaticJobAccessor staticJobAccessor;

    @Autowired
    private DefaultNotificationAccessor defaultNotificationAccessor;

    private static final String JOB_NAME = "test job";
    private static final long PROVIDER_AND_BDCONFIG_ID = 1L;  // This is also notification.providerConfigId

    @AfterEach
    public void cleanup() {
        distributionJobRepository.deleteAll();

        PageRequest notificationPages = defaultNotificationAccessor.getPageRequestForNotifications(0, 10, null, null);
        Page<AlertNotificationModel> notifications = defaultNotificationAccessor.findAll(notificationPages, false);
        notifications.get().forEach(defaultNotificationAccessor::deleteNotification);

        AlertPagedModel<DistributionJobModel> jobPages = staticJobAccessor.getPageOfJobs(0, 10);
        jobPages.getModels().stream()
            .map(DistributionJobModel::getJobId)
            .forEach(staticJobAccessor::deleteJob);
    }

    @Test
    void findAndSortReturnsNotificationsOnNoFilters() {
        // Create a JobsDetailEntity and the DistributionJobEntity
        EmailJobDetailsModel emailJobDetailsModel = createEmailJobDetails();
        DistributionJobRequestModel distributionJobModel = createDistributionJobRequestModel(
            List.of(NotificationType.VULNERABILITY.name(), NotificationType.RULE_VIOLATION.name()),
            Collections.emptyList(),
            Collections.emptyList(),
            emailJobDetailsModel
        );

        UUID createdJobId = staticJobAccessor.createJob(distributionJobModel).getJobId();

        // Create the NotificationEntity
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(NotificationType.VULNERABILITY.name());
        List<AlertNotificationModel> alertNotificationModels = defaultNotificationAccessor.saveAllNotifications(
            List.of(alertNotificationModel)
        );

        // Filter and find
        Page<FilteredDistributionJob> filteredDistributionJobPage = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(0).getId(),
            PROVIDER_AND_BDCONFIG_ID,
            List.of(FrequencyType.REAL_TIME.name()),
            Collections.emptySet(),
            null,
            null,
            PageRequest.of(0, 10)
        );

        assertEquals(1L, filteredDistributionJobPage.getTotalElements());
        assertFilteredPageMatchesJobAndNotificationId(filteredDistributionJobPage, createdJobId, alertNotificationModels);
    }

    @Test
    void findAndSortReturnsNotificationsOnPolicyFilter() {
        List<String> policyFilterNames = List.of("Policy1");

        // Create a JobsDetailEntity and the DistributionJobEntity
        EmailJobDetailsModel emailJobDetailsModel = createEmailJobDetails();
        DistributionJobRequestModel distributionJobModel = createDistributionJobRequestModel(
            List.of(NotificationType.RULE_VIOLATION.name()),
            policyFilterNames,
            Collections.emptyList(),
            emailJobDetailsModel
        );

        UUID createdJobId = staticJobAccessor.createJob(distributionJobModel).getJobId();

        // Create the NotificationEntity
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(NotificationType.RULE_VIOLATION.name());

        List<AlertNotificationModel> alertNotificationModels = defaultNotificationAccessor.saveAllNotifications(
            List.of(alertNotificationModel)
        );

        // Filter and find with matching policy
        Page<FilteredDistributionJob> filteredPageWithWantedPolicy = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(0).getId(),
            PROVIDER_AND_BDCONFIG_ID,
            List.of(FrequencyType.REAL_TIME.name()),
            Collections.emptySet(),
            new HashSet<>(policyFilterNames),
            null,
            PageRequest.of(0, 10)
        );

        assertEquals(1L, filteredPageWithWantedPolicy.getTotalElements());
        assertFilteredPageMatchesJobAndNotificationId(filteredPageWithWantedPolicy, createdJobId, alertNotificationModels);

        // Filter and find with non-matching policy
        Page<FilteredDistributionJob> filteredPageWithUnwantedPolicy = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(0).getId(),
            PROVIDER_AND_BDCONFIG_ID,
            List.of(FrequencyType.REAL_TIME.name()),
            Collections.emptySet(),
            new HashSet<>(List.of("Policy2")),
            null,
            PageRequest.of(0, 10)
        );

        assertEquals(0L, filteredPageWithUnwantedPolicy.getTotalElements());
    }

    @Test
    void findAndSortReturnsNotificationsOnSeverityFilter() {
        List<String> vulnerabilitySeverityFilterNames = List.of(VulnerabilitySeverityType.CRITICAL.name());

        // Create a JobsDetailEntity and the DistributionJobEntity
        EmailJobDetailsModel emailJobDetailsModel = createEmailJobDetails();
        DistributionJobRequestModel distributionJobModel = createDistributionJobRequestModel(
            List.of(NotificationType.VULNERABILITY.name()),
            Collections.emptyList(),
            vulnerabilitySeverityFilterNames,
            emailJobDetailsModel
        );

        UUID createdJobId = staticJobAccessor.createJob(distributionJobModel).getJobId();

        // Create the NotificationEntity
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(NotificationType.VULNERABILITY.name());

        List<AlertNotificationModel> alertNotificationModels = defaultNotificationAccessor.saveAllNotifications(
            List.of(alertNotificationModel)
        );

        // Filter and find with matching severity
        Page<FilteredDistributionJob> filteredPageWithWantedSeverity = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(0).getId(),
            PROVIDER_AND_BDCONFIG_ID,
            List.of(FrequencyType.REAL_TIME.name()),
            Collections.emptySet(),
            null,
            new HashSet<>(vulnerabilitySeverityFilterNames),
            PageRequest.of(0, 10)
        );
        assertEquals(1L, filteredPageWithWantedSeverity.getTotalElements());
        assertFilteredPageMatchesJobAndNotificationId(filteredPageWithWantedSeverity, createdJobId, alertNotificationModels);

        // Filter and find with non-matching severity
        Page<FilteredDistributionJob> filteredPageWithUnwantedSeverity = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(0).getId(),
            PROVIDER_AND_BDCONFIG_ID,
            List.of(FrequencyType.REAL_TIME.name()),
            Collections.emptySet(),
            null,
            new HashSet<>(List.of(VulnerabilitySeverityType.LOW.name())),
            PageRequest.of(0, 10)
        );
        assertEquals(0L, filteredPageWithUnwantedSeverity.getTotalElements());
    }

    @Test
    void findAndSortReturnsNotificationsOnSeverityAndPolicyFilter() {
        List<String> policyFilterNames = List.of("Policy1");
        List<String> vulnerabilitySeverityFilterNames = List.of(VulnerabilitySeverityType.CRITICAL.name());

        // Create a JobsDetailEntity and the DistributionJobEntity
        EmailJobDetailsModel emailJobDetailsModel = createEmailJobDetails();
        DistributionJobRequestModel distributionJobModel = createDistributionJobRequestModel(
            List.of(NotificationType.VULNERABILITY.name(), NotificationType.RULE_VIOLATION.name()),
            policyFilterNames,
            vulnerabilitySeverityFilterNames,
            emailJobDetailsModel
        );

        UUID createdJobId = staticJobAccessor.createJob(distributionJobModel).getJobId();

        // Create the NotificationEntity - 2 here to simulate policy AND severity violation
        AlertNotificationModel alertNotificationModel1 = createAlertNotificationModel(NotificationType.VULNERABILITY.name());
        AlertNotificationModel alertNotificationModel2 = createAlertNotificationModel(NotificationType.RULE_VIOLATION.name());

        List<AlertNotificationModel> alertNotificationModels = defaultNotificationAccessor.saveAllNotifications(
            List.of(alertNotificationModel1, alertNotificationModel2)
        );

        // Filter and find with matching severity and policy on a notification
        Page<FilteredDistributionJob> filteredPageWithWantedSeverityAndPolicy = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(0).getId(), // Notification 0
            PROVIDER_AND_BDCONFIG_ID,
            List.of(FrequencyType.REAL_TIME.name()),
            Collections.emptySet(),
            new HashSet<>(policyFilterNames),
            new HashSet<>(vulnerabilitySeverityFilterNames),
            PageRequest.of(0, 10)
        );
        assertEquals(1L, filteredPageWithWantedSeverityAndPolicy.getTotalElements());
        assertFilteredPageMatchesJobAndNotificationId(filteredPageWithWantedSeverityAndPolicy, createdJobId, alertNotificationModels);

        // Filter and find with non-matching severity and policy on the other notification
        Page<FilteredDistributionJob> filteredPageWithUnwantedSeverityAndPolicy = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(1).getId(), // Notification 1
            PROVIDER_AND_BDCONFIG_ID,
            List.of(FrequencyType.REAL_TIME.name()),
            Collections.emptySet(),
            new HashSet<>(List.of("Policy2")),
            new HashSet<>(List.of(VulnerabilitySeverityType.LOW.name())),
            PageRequest.of(0, 10)
        );
        assertEquals(0L, filteredPageWithUnwantedSeverityAndPolicy.getTotalElements());

        // Extra scenarios checks here for policy and sev dependency along with non-existence and emptiness
        Page<FilteredDistributionJob> filteredPageWithWantedSeverityAndNullPolicy = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(0).getId(), // Notification 0
            PROVIDER_AND_BDCONFIG_ID,
            List.of(FrequencyType.REAL_TIME.name()),
            Collections.emptySet(),
            null,
            new HashSet<>(vulnerabilitySeverityFilterNames),
            PageRequest.of(0, 10)
        );
        assertEquals(1L, filteredPageWithWantedSeverityAndNullPolicy.getTotalElements());

        Page<FilteredDistributionJob> filteredPageWithEmptySeverityAndWantedPolicy = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(1).getId(), // Notification 1
            PROVIDER_AND_BDCONFIG_ID,
            List.of(FrequencyType.REAL_TIME.name()),
            Collections.emptySet(),
            new HashSet<>(policyFilterNames),
            null,
            PageRequest.of(0, 10)
        );
        assertEquals(1L, filteredPageWithEmptySeverityAndWantedPolicy.getTotalElements());
    }

    private EmailJobDetailsModel createEmailJobDetails() {
        return new EmailJobDetailsModel(
            null,
            "subjectLine",
            false,
            false,
            "attachmentFileType",
            List.of()
        );
    }

    private DistributionJobRequestModel createDistributionJobRequestModel(List<String> notificationTypes, List<String> policyFilterPolicyNames, List<String> vulnerabilityFilterSeverityNames, DistributionJobDetailsModel distributionJobDetails) {
        return new DistributionJobRequestModel(
            true,
            JOB_NAME,
            FrequencyType.REAL_TIME,
            ProcessingType.DEFAULT,
            ChannelKeys.EMAIL.getUniversalKey(),
            UUID.randomUUID(),
            PROVIDER_AND_BDCONFIG_ID,
            false,
            null,
            null,
            notificationTypes,
            Collections.emptyList(),
            policyFilterPolicyNames,
            vulnerabilityFilterSeverityNames,
            distributionJobDetails
        );
    }

    private AlertNotificationModel createAlertNotificationModel(String notificationType) {
        OffsetDateTime currentDateTimestamp = DateUtils.createCurrentDateTimestamp();
        return new AlertNotificationModel(
            null,
            PROVIDER_AND_BDCONFIG_ID,
            "provider",
            "providerConfigName",
            notificationType,
            "{content: \"content is here...\"}",
            currentDateTimestamp,
            currentDateTimestamp,
            false,
            String.format("content-id-%s", UUID.randomUUID()),
            false
        );
    }

    private void assertFilteredPageMatchesJobAndNotificationId(Page<FilteredDistributionJob> filteredPage, UUID createdJobId, List<AlertNotificationModel> alertNotificationModels) {
        FilteredDistributionJob firstFilteredDistributionJob = filteredPage.stream().findFirst().orElseThrow();
        assertEquals(createdJobId, firstFilteredDistributionJob.getJobId());
        assertEquals(alertNotificationModels.get(0).getId(), firstFilteredDistributionJob.getNotificationId());
    }
}