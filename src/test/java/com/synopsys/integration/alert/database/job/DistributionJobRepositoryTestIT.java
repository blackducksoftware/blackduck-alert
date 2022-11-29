package com.synopsys.integration.alert.database.job;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.DefaultNotificationAccessor;
import com.synopsys.integration.alert.database.api.StaticJobAccessor;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.blackduck.api.generated.enumeration.VulnerabilitySeverityType;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@AlertIntegrationTest
class DistributionJobRepositoryTestIT {
    // Distribution jobs related
    @Autowired private DistributionJobRepository distributionJobRepository;
    @Autowired private StaticJobAccessor staticJobAccessor;

    @Autowired
    private DefaultNotificationAccessor defaultNotificationAccessor;

    @AfterEach
    public void cleanup() {
        distributionJobRepository.deleteAll();
    }

    @Test
    @Transactional
    void findAndSortReturnsNotificationsOnNoFilters() {
        long providerAndBDConfigId = 1L;  // This is also notification.providerConfigId

        // Create a JobsDetailEntity and the DistributionJobEntity
        FrequencyType frequency = FrequencyType.REAL_TIME;
        EmailJobDetailsModel emailJobDetailsModel = createEmailJobDetails();
        DistributionJobRequestModel distributionJobModel = createDistributionJobRequestModel(
            frequency,
            providerAndBDConfigId,
            List.of(NotificationType.VULNERABILITY.name(), NotificationType.RULE_VIOLATION.name()),
            Collections.emptyList(),
            Collections.emptyList(),
            emailJobDetailsModel
        );

        DistributionJobModel createdJob = staticJobAccessor.createJob(distributionJobModel);
        UUID createdJobId = createdJob.getJobId();

        // Create the NotificationEntity
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(providerAndBDConfigId, NotificationType.VULNERABILITY.name());

        List<AlertNotificationModel> alertNotificationModels = defaultNotificationAccessor.saveAllNotifications(
            List.of(alertNotificationModel)
        );

        // Filter and find
        Page<FilteredDistributionJob> filteredDistributionJobPage = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(0).getId(),
            providerAndBDConfigId,
            List.of(frequency.name()),
            Collections.emptySet(),
            null,
            null,
            PageRequest.of(0, 10)
        );

        assertEquals(1L, filteredDistributionJobPage.getTotalElements());
        FilteredDistributionJob firstFilteredDistributionJob = filteredDistributionJobPage.stream().findFirst().orElseThrow();
        assertEquals(createdJobId, firstFilteredDistributionJob.getJobId());
        assertEquals(alertNotificationModels.get(0).getId(), firstFilteredDistributionJob.getNotificationId());
    }

    @Test
    @Transactional
    void findAndSortReturnsNotificationsOnPolicyFilter() {
        long providerAndBDConfigId = 1L;
        List<String> policyFilterNames = List.of("Policy1");

        // Create a JobsDetailEntity and the DistributionJobEntity
        FrequencyType frequency = FrequencyType.REAL_TIME;
        EmailJobDetailsModel emailJobDetailsModel = createEmailJobDetails();
        DistributionJobRequestModel distributionJobModel = createDistributionJobRequestModel(
            frequency,
            providerAndBDConfigId,
            List.of(NotificationType.VULNERABILITY.name(), NotificationType.RULE_VIOLATION.name()),
            policyFilterNames,
            Collections.emptyList(),
            emailJobDetailsModel
        );

        DistributionJobModel createdJob = staticJobAccessor.createJob(distributionJobModel);
        UUID createdJobId = createdJob.getJobId();

        // Create the NotificationEntity
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(providerAndBDConfigId, NotificationType.RULE_VIOLATION.name());

        List<AlertNotificationModel> alertNotificationModels = defaultNotificationAccessor.saveAllNotifications(
            List.of(alertNotificationModel)
        );

        // Filter and find with matching policy
        Page<FilteredDistributionJob> filteredPageWithWantedPolicy = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(0).getId(),
            providerAndBDConfigId,
            List.of(frequency.name()),
            Collections.emptySet(),
            new HashSet<>(policyFilterNames),
            null,
            PageRequest.of(0, 10)
        );

        assertEquals(1L, filteredPageWithWantedPolicy.getTotalElements());
        FilteredDistributionJob firstFilteredDistributionJob = filteredPageWithWantedPolicy.stream().findFirst().orElseThrow();
        assertEquals(createdJobId, firstFilteredDistributionJob.getJobId());
        assertEquals(alertNotificationModels.get(0).getId(), firstFilteredDistributionJob.getNotificationId());

        // Filter and find with non-matching policy
        Page<FilteredDistributionJob> filteredPageWithUnwantedPolicy = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(0).getId(),
            providerAndBDConfigId,
            List.of(frequency.name()),
            Collections.emptySet(),
            new HashSet<>(List.of("Policy2")),
            null,
            PageRequest.of(0, 10)
        );

        assertEquals(0L, filteredPageWithUnwantedPolicy.getTotalElements());
    }

    @Test
    @Transactional
    void findAndSortReturnsNotificationsOnSeverityFilter() {
        long providerAndBDConfigId = 1L;
        List<String> vulnerabilitySeverityFilterNames = List.of(VulnerabilitySeverityType.CRITICAL.name());

        // Create a JobsDetailEntity and the DistributionJobEntity
        FrequencyType frequency = FrequencyType.REAL_TIME;
        EmailJobDetailsModel emailJobDetailsModel = createEmailJobDetails();
        DistributionJobRequestModel distributionJobModel = createDistributionJobRequestModel(
            frequency,
            providerAndBDConfigId,
            List.of(NotificationType.VULNERABILITY.name(), NotificationType.RULE_VIOLATION.name()),
            Collections.emptyList(),
            vulnerabilitySeverityFilterNames,
            emailJobDetailsModel
        );

        DistributionJobModel createdJob = staticJobAccessor.createJob(distributionJobModel);
        UUID createdJobId = createdJob.getJobId();

        // Create the NotificationEntity
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(providerAndBDConfigId, NotificationType.VULNERABILITY.name());

        List<AlertNotificationModel> alertNotificationModels = defaultNotificationAccessor.saveAllNotifications(
            List.of(alertNotificationModel)
        );

        // Filter and find with matching severity
        Page<FilteredDistributionJob> filteredPageWithWantedSeverity = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(0).getId(),
            providerAndBDConfigId,
            List.of(frequency.name()),
            Collections.emptySet(),
            null,
            new HashSet<>(vulnerabilitySeverityFilterNames),
            PageRequest.of(0, 10)
        );

        assertEquals(1L, filteredPageWithWantedSeverity.getTotalElements());
        FilteredDistributionJob firstFilteredDistributionJob = filteredPageWithWantedSeverity.stream().findFirst().orElseThrow();
        assertEquals(createdJobId, firstFilteredDistributionJob.getJobId());
        assertEquals(alertNotificationModels.get(0).getId(), firstFilteredDistributionJob.getNotificationId());

        // Filter and find with non-matching severity
        Page<FilteredDistributionJob> filteredPageWithUnwantedSeverity = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(0).getId(),
            providerAndBDConfigId,
            List.of(frequency.name()),
            Collections.emptySet(),
            null,
            new HashSet<>(List.of(VulnerabilitySeverityType.LOW.name())),
            PageRequest.of(0, 10)
        );

        assertEquals(0L, filteredPageWithUnwantedSeverity.getTotalElements());
    }

    @Test
    @Transactional
    void findAndSortReturnsNotificationsOnSeverityAndPolicyFilter() {
        long providerAndBDConfigId = 1L;
        List<String> policyFilterNames = List.of("Policy1");
        List<String> vulnerabilitySeverityFilterNames = List.of(VulnerabilitySeverityType.CRITICAL.name());

        // Create a JobsDetailEntity and the DistributionJobEntity
        FrequencyType frequency = FrequencyType.REAL_TIME;
        EmailJobDetailsModel emailJobDetailsModel = createEmailJobDetails();
        DistributionJobRequestModel distributionJobModel = createDistributionJobRequestModel(
            frequency,
            providerAndBDConfigId,
            List.of(NotificationType.VULNERABILITY.name(), NotificationType.RULE_VIOLATION.name()),
            policyFilterNames,
            vulnerabilitySeverityFilterNames,
            emailJobDetailsModel
        );

        DistributionJobModel createdJob = staticJobAccessor.createJob(distributionJobModel);
        UUID createdJobId = createdJob.getJobId();

        // Create the NotificationEntity - 2 here to simulate policy AND severity violation
        AlertNotificationModel alertNotificationModel1 = createAlertNotificationModel(providerAndBDConfigId, NotificationType.VULNERABILITY.name());
        AlertNotificationModel alertNotificationModel2 = createAlertNotificationModel(providerAndBDConfigId, NotificationType.RULE_VIOLATION.name());

        List<AlertNotificationModel> alertNotificationModels = defaultNotificationAccessor.saveAllNotifications(
            List.of(alertNotificationModel1, alertNotificationModel2)
        );

        // Filter and find with matching severity and policy on a notification
        Page<FilteredDistributionJob> filteredPageWithWantedSeverityAndPolicy = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(0).getId(), // Notification 0
            providerAndBDConfigId,
            List.of(frequency.name()),
            Collections.emptySet(),
            new HashSet<>(policyFilterNames),
            new HashSet<>(vulnerabilitySeverityFilterNames),
            PageRequest.of(0, 10)
        );

        assertEquals(1L, filteredPageWithWantedSeverityAndPolicy.getTotalElements());
        FilteredDistributionJob firstFilteredDistributionJob = filteredPageWithWantedSeverityAndPolicy.stream().findFirst().orElseThrow();
        assertEquals(createdJobId, firstFilteredDistributionJob.getJobId());
        assertEquals(alertNotificationModels.get(0).getId(), firstFilteredDistributionJob.getNotificationId());

        // Filter and find with non-matching severity and policy on the other notification
        Page<FilteredDistributionJob> filteredPageWithUnwantedSeverityAndPolicy = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(1).getId(), // Notification 1
            providerAndBDConfigId,
            List.of(frequency.name()),
            Collections.emptySet(),
            new HashSet<>(List.of("Policy2")),
            new HashSet<>(List.of(VulnerabilitySeverityType.LOW.name())),
            PageRequest.of(0, 10)
        );

        assertEquals(0L, filteredPageWithUnwantedSeverityAndPolicy.getTotalElements());

        // Extra scenarios checks here for policy and sev dependency along with non-existence and emptiness
        Page<FilteredDistributionJob> filteredPageWithWantedSeverityAndNullPolicy = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(0).getId(), // Notification 0
            providerAndBDConfigId,
            List.of(frequency.name()),
            Collections.emptySet(),
            null,
            new HashSet<>(vulnerabilitySeverityFilterNames),
            PageRequest.of(0, 10)
        );

        assertEquals(1L, filteredPageWithWantedSeverityAndNullPolicy.getTotalElements());

        Page<FilteredDistributionJob> filteredPageWithEmptySeverityAndWantedPolicy = distributionJobRepository.findAndSortMatchingEnabledJobsByFilteredNotification(
            alertNotificationModels.get(1).getId(), // Notification 1
            providerAndBDConfigId,
            List.of(frequency.name()),
            Collections.emptySet(),
            new HashSet<>(policyFilterNames),
            new HashSet<>(),
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

    private DistributionJobRequestModel createDistributionJobRequestModel(FrequencyType frequency, long providerAndBDConfigId, List<String> notificationTypes, List<String> policyFilterPolicyNames, List<String> vulnerabilityFilterSeverityNames, DistributionJobDetailsModel distributionJobDetails) {
        return new DistributionJobRequestModel(
            true,
            "test job",
            frequency,
            ProcessingType.DEFAULT,
            ChannelKeys.EMAIL.getUniversalKey(),
            UUID.randomUUID(),
            providerAndBDConfigId,
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

    private AlertNotificationModel createAlertNotificationModel(long providerAndBDConfigId, String notificationType) {
        OffsetDateTime currentDateTimestamp = DateUtils.createCurrentDateTimestamp();
        return new AlertNotificationModel(
            null,
            providerAndBDConfigId,
            "provider",
            "providerConfigName",
            notificationType,
            "{content: \"content is here...\"}",
            currentDateTimestamp,
            currentDateTimestamp,
            false
        );
    }
}