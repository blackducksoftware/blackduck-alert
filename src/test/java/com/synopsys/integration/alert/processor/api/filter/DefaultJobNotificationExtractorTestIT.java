package com.synopsys.integration.alert.processor.api.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.processor.api.filter.model.FilterableNotificationWrapper;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.blackduck.api.generated.enumeration.VulnerabilitySeverityType;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@AlertIntegrationTest
public class DefaultJobNotificationExtractorTestIT {
    private static final List<UUID> createdJobs = new LinkedList<>();
    private static final String POLICY_FILTER_NAME = "policyName";

    @Autowired
    public JobAccessor jobAccessor;

    @AfterEach
    public void removeCreatedJobsIfExist() {
        createdJobs.stream().forEach(jobAccessor::deleteJob);
        createdJobs.clear();
    }

    @Test
    public void extractJobTest() {
        createJobs(createDistributionJobModels());

        DefaultJobNotificationExtractor defaultJobNotificationExtractor = new DefaultJobNotificationExtractor(jobAccessor);
        Map<FilteredDistributionJobResponseModel, List<FilterableNotificationWrapper<?>>> jobResponseModelListMap = defaultJobNotificationExtractor.mapJobsToNotifications(createNotificationWrappers(), List.of(FrequencyType.REAL_TIME));

        assertNotNull(jobResponseModelListMap);
        assertEquals(3, jobResponseModelListMap.size());
        for (List<FilterableNotificationWrapper<?>> jobNotificationList : jobResponseModelListMap.values()) {
            assertFalse(jobNotificationList.isEmpty());
            assertTrue(jobNotificationList.size() < 4);
        }
    }

    @Test
    public void extractNoJobsTest() {
        DefaultJobNotificationExtractor defaultJobNotificationExtractor = new DefaultJobNotificationExtractor(jobAccessor);
        Map<FilteredDistributionJobResponseModel, List<FilterableNotificationWrapper<?>>> jobResponseModelListMap = defaultJobNotificationExtractor.mapJobsToNotifications(createNotificationWrappers(), List.of(FrequencyType.REAL_TIME));

        assertTrue(jobResponseModelListMap.isEmpty());
    }

    @Test
    public void extractSingleJob() {
        DistributionJobRequestModel jobRequestModel = createJobRequestModel(
            FrequencyType.REAL_TIME,
            ProcessingType.DIGEST,
            List.of(NotificationType.VULNERABILITY.name(), NotificationType.POLICY_OVERRIDE.name()),
            List.of(),
            List.of()
        );
        testSingleJob(jobRequestModel, 4);
    }

    @Test
    public void extractSingleJobWithMatchingFilter() {
        DistributionJobRequestModel jobRequestModel = createJobRequestModel(
            FrequencyType.REAL_TIME,
            ProcessingType.DIGEST,
            List.of(NotificationType.VULNERABILITY.name(), NotificationType.POLICY_OVERRIDE.name()),
            List.of(),
            List.of(POLICY_FILTER_NAME)
        );
        testSingleJob(jobRequestModel, 4);
    }

    @Test
    public void extractSingleJobWithoutMatchingFilter() {
        DistributionJobRequestModel jobRequestModel = createJobRequestModel(
            FrequencyType.REAL_TIME,
            ProcessingType.DIGEST,
            List.of(NotificationType.VULNERABILITY.name(), NotificationType.POLICY_OVERRIDE.name()),
            List.of(VulnerabilitySeverityType.HIGH.name()),
            List.of("RANDOM")
        );
        testSingleJob(jobRequestModel, 2);
    }

    // TODO add tests to verify project filtering works

    private void testSingleJob(DistributionJobRequestModel jobRequestModel, int expectedMappedNotifications) {
        createJobs(List.of(jobRequestModel));

        DefaultJobNotificationExtractor defaultJobNotificationExtractor = new DefaultJobNotificationExtractor(jobAccessor);
        Map<FilteredDistributionJobResponseModel, List<FilterableNotificationWrapper<?>>> jobResponseModelListMap = defaultJobNotificationExtractor.mapJobsToNotifications(createNotificationWrappers(), List.of(FrequencyType.REAL_TIME));

        assertEquals(1, jobResponseModelListMap.size());

        List<FilterableNotificationWrapper<?>> filterableNotificationWrappers = jobResponseModelListMap.values().stream().findFirst().orElse(List.of());

        assertEquals(expectedMappedNotifications, filterableNotificationWrappers.size());
    }

    private void createJobs(List<DistributionJobRequestModel> jobs) {
        jobs
            .stream()
            .map(jobAccessor::createJob)
            .map(DistributionJobModel::getJobId)
            .forEach(createdJobs::add);
    }

    private List<DistributionJobRequestModel> createDistributionJobModels() {
        DistributionJobRequestModel distributionJobRequestModel1 = createJobRequestModel(
            FrequencyType.REAL_TIME,
            ProcessingType.DIGEST,
            List.of(NotificationType.VULNERABILITY.name()),
            List.of(VulnerabilitySeverityType.LOW.name()),
            List.of()
        );
        DistributionJobRequestModel distributionJobRequestModel2 = createJobRequestModel(
            FrequencyType.REAL_TIME,
            ProcessingType.DIGEST,
            List.of(NotificationType.VULNERABILITY.name()),
            List.of(VulnerabilitySeverityType.HIGH.name(), VulnerabilitySeverityType.LOW.name()),
            List.of()
        );
        DistributionJobRequestModel distributionJobRequestModel3 = createJobRequestModel(
            FrequencyType.REAL_TIME,
            ProcessingType.DIGEST,
            List.of(NotificationType.VULNERABILITY.name()),
            List.of(),
            List.of()
        );
        DistributionJobRequestModel distributionJobRequestModel4 = createJobRequestModel(
            FrequencyType.REAL_TIME,
            ProcessingType.DIGEST,
            List.of(NotificationType.VULNERABILITY.name()),
            List.of(VulnerabilitySeverityType.MEDIUM.name()),
            List.of()
        );

        return List.of(distributionJobRequestModel1, distributionJobRequestModel2, distributionJobRequestModel3, distributionJobRequestModel4);
    }

    private DistributionJobRequestModel createJobRequestModel(
        FrequencyType frequencyType,
        ProcessingType processingType,
        List<String> notificationTypes,
        List<String> vulns,
        List<String> policies
    ) {
        return new DistributionJobRequestModel(
            true,
            "name",
            frequencyType,
            processingType,
            ChannelKeys.SLACK.getUniversalKey(),
            0L,
            false,
            "",
            notificationTypes,
            List.of(),
            policies,
            vulns,
            new SlackJobDetailsModel(null, "webhook", "channelName", "username")
        );
    }

    private List<FilterableNotificationWrapper<?>> createNotificationWrappers() {
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(NotificationType.VULNERABILITY);
        FilterableNotificationWrapper<?> test_project = FilterableNotificationWrapper.vulnerability(
            alertNotificationModel,
            null,
            "test_project",
            List.of(VulnerabilitySeverityType.LOW.name())
        );
        FilterableNotificationWrapper<?> test_project2 = FilterableNotificationWrapper.vulnerability(
            alertNotificationModel,
            null,
            "test_project1",
            List.of(VulnerabilitySeverityType.HIGH.name())
        );
        FilterableNotificationWrapper<?> test_project3 = FilterableNotificationWrapper.vulnerability(
            alertNotificationModel,
            null,
            "test_project2",
            List.of(VulnerabilitySeverityType.LOW.name(), VulnerabilitySeverityType.HIGH.name())
        );
        AlertNotificationModel alertPolicyNotificationModel = createAlertNotificationModel(NotificationType.POLICY_OVERRIDE);
        FilterableNotificationWrapper<?> test_project4 = FilterableNotificationWrapper.policy(
            alertPolicyNotificationModel,
            null,
            "test_project2",
            List.of(POLICY_FILTER_NAME)
        );

        return List.of(test_project, test_project2, test_project3, test_project4);
    }

    public AlertNotificationModel createAlertNotificationModel(NotificationType notificationType) {
        return new AlertNotificationModel(
            0L,
            0L,
            "provider",
            "providerConfigName",
            notificationType.name(),
            "content",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            false
        );
    }

}
