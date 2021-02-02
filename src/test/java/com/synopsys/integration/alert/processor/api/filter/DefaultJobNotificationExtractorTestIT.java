package com.synopsys.integration.alert.processor.api.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.processor.api.filter.extractor.VulnerabilityUniqueProjectNotificationContent;
import com.synopsys.integration.alert.processor.api.filter.model.DetailedNotificationContent;
import com.synopsys.integration.alert.processor.api.filter.model.FilteredJobNotificationWrapper;
import com.synopsys.integration.alert.processor.api.filter.model.NotificationContentWrapper;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.blackduck.api.generated.enumeration.VulnerabilitySeverityType;
import com.synopsys.integration.blackduck.api.manual.component.AffectedProjectVersion;
import com.synopsys.integration.blackduck.api.manual.component.PolicyOverrideNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@AlertIntegrationTest
public class DefaultJobNotificationExtractorTestIT {
    private static final List<UUID> CREATED_JOBS = new LinkedList<>();
    private static final String PROJECT_NAME_1 = "test_project";
    private static final String POLICY_FILTER_NAME = "policyName";

    @Autowired
    public JobAccessor jobAccessor;

    @AfterEach
    public void removeCreatedJobsIfExist() {
        CREATED_JOBS.forEach(jobAccessor::deleteJob);
        CREATED_JOBS.clear();
    }

    @Test
    public void mapJobsTest() {
        createJobs(createDistributionJobModels());

        JobNotificationMapper jobNotificationMapper = new JobNotificationMapper(jobAccessor);
        List<FilteredJobNotificationWrapper> mappedNotifications = jobNotificationMapper.mapJobsToNotifications(createNotificationWrappers(), List.of(FrequencyType.REAL_TIME));

        assertNotNull(mappedNotifications);
        assertEquals(3, mappedNotifications.size());
        for (FilteredJobNotificationWrapper mappedJobNotifications : mappedNotifications) {
            List<NotificationContentWrapper> jobNotifications = mappedJobNotifications.getJobNotifications();
            assertFalse(jobNotifications.isEmpty(), "Expected the list not to be empty");
            assertTrue(jobNotifications.size() < 4, "Expected the list to contain fewer elements");
        }
    }

    @Test
    public void mapNoJobsTest() {
        JobNotificationMapper jobNotificationMapper = new JobNotificationMapper(jobAccessor);
        List<FilteredJobNotificationWrapper> mappedNotifications = jobNotificationMapper.mapJobsToNotifications(createNotificationWrappers(), List.of(FrequencyType.REAL_TIME));
        assertTrue(mappedNotifications.isEmpty(), "Expected the list to be empty");
    }

    @Test
    public void extractSingleJob() {
        DistributionJobRequestModel jobRequestModel = createJobRequestModel(
            FrequencyType.REAL_TIME,
            ProcessingType.DIGEST,
            List.of(),
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
            List.of(),
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
            List.of(),
            List.of(NotificationType.VULNERABILITY.name(), NotificationType.POLICY_OVERRIDE.name()),
            List.of(VulnerabilitySeverityType.HIGH.name()),
            List.of("RANDOM")
        );
        testSingleJob(jobRequestModel, 2);
    }

    @Test
    public void extractJobsWithMatchingProjectsFilter() {
        createJobs(List.of(
            createJobRequestModel(
                FrequencyType.REAL_TIME,
                ProcessingType.DIGEST,
                List.of(PROJECT_NAME_1),
                List.of(NotificationType.VULNERABILITY.name()),
                List.of(),
                List.of()
            ))
        );

        testProjectJob();
    }

    @Test
    public void extractJobsWithMatchingProjectNamePatternFilter() {
        createJobs(List.of(
            new DistributionJobRequestModel(
                true,
                "name",
                FrequencyType.REAL_TIME,
                ProcessingType.DIGEST,
                ChannelKeys.SLACK.getUniversalKey(),
                0L,
                true,
                // Regex to verify we retrieve notifications without a number in the name (PROJECT_NAME_1)
                "^([^0-9]*)$",
                List.of(NotificationType.VULNERABILITY.name()),
                List.of(),
                List.of(),
                List.of(),
                new SlackJobDetailsModel(null, "webhook", "channelName", "username")
            ))
        );

        testProjectJob();
    }

    private void testProjectJob() {
        JobNotificationMapper defaultJobNotificationExtractor = new JobNotificationMapper(jobAccessor);
        List<DetailedNotificationContent> notificationWrappers = createNotificationWrappers();
        List<FilteredJobNotificationWrapper> filteredJobNotificationWrappers = defaultJobNotificationExtractor.mapJobsToNotifications(notificationWrappers, List.of(FrequencyType.REAL_TIME));

        assertEquals(1, filteredJobNotificationWrappers.size());

        List<NotificationContentWrapper> filterableNotificationWrappers = filteredJobNotificationWrappers.get(0).getJobNotifications();
        assertEquals(1, filterableNotificationWrappers.size());

        NotificationContentWrapper filterableNotificationWrapper = filterableNotificationWrappers.get(0);

        assertEquals(NotificationType.VULNERABILITY.name(), filterableNotificationWrapper.extractNotificationType());
        VulnerabilityUniqueProjectNotificationContent vulnerabilityUniqueProjectNotificationContent = (VulnerabilityUniqueProjectNotificationContent) filterableNotificationWrapper.getNotificationContent();

        assertEquals(PROJECT_NAME_1, vulnerabilityUniqueProjectNotificationContent.getAffectedProjectVersion().getProjectName());

    }

    private void testSingleJob(DistributionJobRequestModel jobRequestModel, int expectedMappedNotifications) {
        createJobs(List.of(jobRequestModel));

        JobNotificationMapper jobNotificationMapper = new JobNotificationMapper(jobAccessor);
        List<FilteredJobNotificationWrapper> mappedNotifications = jobNotificationMapper.mapJobsToNotifications(createNotificationWrappers(), List.of(FrequencyType.REAL_TIME));

        assertEquals(1, mappedNotifications.size());
        FilteredJobNotificationWrapper jobNotificationWrapper = mappedNotifications.get(0);

        List<NotificationContentWrapper> filterableNotificationWrappers = jobNotificationWrapper.getJobNotifications();
        assertEquals(expectedMappedNotifications, filterableNotificationWrappers.size());
    }

    private void createJobs(List<DistributionJobRequestModel> jobs) {
        jobs
            .stream()
            .map(jobAccessor::createJob)
            .map(DistributionJobModel::getJobId)
            .forEach(CREATED_JOBS::add);
    }

    private List<DistributionJobRequestModel> createDistributionJobModels() {
        DistributionJobRequestModel distributionJobRequestModel1 = createJobRequestModel(
            FrequencyType.REAL_TIME,
            ProcessingType.DIGEST,
            List.of(),
            List.of(NotificationType.VULNERABILITY.name()),
            List.of(VulnerabilitySeverityType.LOW.name()),
            List.of()
        );
        DistributionJobRequestModel distributionJobRequestModel2 = createJobRequestModel(
            FrequencyType.REAL_TIME,
            ProcessingType.DIGEST,
            List.of(),
            List.of(NotificationType.VULNERABILITY.name()),
            List.of(VulnerabilitySeverityType.HIGH.name(), VulnerabilitySeverityType.LOW.name()),
            List.of()
        );
        DistributionJobRequestModel distributionJobRequestModel3 = createJobRequestModel(
            FrequencyType.REAL_TIME,
            ProcessingType.DIGEST,
            List.of(),
            List.of(NotificationType.VULNERABILITY.name()),
            List.of(),
            List.of()
        );
        DistributionJobRequestModel distributionJobRequestModel4 = createJobRequestModel(
            FrequencyType.REAL_TIME,
            ProcessingType.DIGEST,
            List.of(),
            List.of(NotificationType.VULNERABILITY.name()),
            List.of(VulnerabilitySeverityType.MEDIUM.name()),
            List.of()
        );

        return List.of(distributionJobRequestModel1, distributionJobRequestModel2, distributionJobRequestModel3, distributionJobRequestModel4);
    }

    private DistributionJobRequestModel createJobRequestModel(
        FrequencyType frequencyType,
        ProcessingType processingType,
        List<String> projectNames,
        List<String> notificationTypes,
        List<String> vulns,
        List<String> policies
    ) {
        List<BlackDuckProjectDetailsModel> blackDuckProjectDetailsModels = projectNames.stream()
                                                                               .map(projectName -> new BlackDuckProjectDetailsModel(projectName, "href"))
                                                                               .collect(Collectors.toList());
        return new DistributionJobRequestModel(
            true,
            "name",
            frequencyType,
            processingType,
            ChannelKeys.SLACK.getUniversalKey(),
            0L,
            projectNames != null && !projectNames.isEmpty(),
            null,
            notificationTypes,
            blackDuckProjectDetailsModels,
            policies,
            vulns,
            new SlackJobDetailsModel(null, "webhook", "channelName", "username")
        );
    }

    private VulnerabilityUniqueProjectNotificationContent createVulnerabilityUniqueProjectNotificationContent(String projectName) {
        VulnerabilityUniqueProjectNotificationContent vulnerabilityUniqueProjectNotificationContent = Mockito.mock(VulnerabilityUniqueProjectNotificationContent.class);
        AffectedProjectVersion affectedProjectVersion = new AffectedProjectVersion();
        affectedProjectVersion.setProjectName(projectName);
        Mockito.when(vulnerabilityUniqueProjectNotificationContent.getAffectedProjectVersion()).thenReturn(affectedProjectVersion);
        return vulnerabilityUniqueProjectNotificationContent;
    }

    private List<DetailedNotificationContent> createNotificationWrappers() {
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(NotificationType.VULNERABILITY);
        DetailedNotificationContent test_project = DetailedNotificationContent.vulnerability(
            alertNotificationModel,
            createVulnerabilityUniqueProjectNotificationContent(PROJECT_NAME_1),
            PROJECT_NAME_1,
            List.of(VulnerabilitySeverityType.LOW.name())
        );
        String projectName1 = "test_project1";
        DetailedNotificationContent test_project2 = DetailedNotificationContent.vulnerability(
            alertNotificationModel,
            createVulnerabilityUniqueProjectNotificationContent(projectName1),
            projectName1,
            List.of(VulnerabilitySeverityType.HIGH.name())
        );
        String projectName2 = "test_project2";
        DetailedNotificationContent test_project3 = DetailedNotificationContent.vulnerability(
            alertNotificationModel,
            createVulnerabilityUniqueProjectNotificationContent(projectName2),
            projectName2,
            List.of(VulnerabilitySeverityType.LOW.name(), VulnerabilitySeverityType.HIGH.name())
        );
        PolicyOverrideNotificationContent policyOverrideNotificationContent = Mockito.mock(PolicyOverrideNotificationContent.class);
        AlertNotificationModel alertPolicyNotificationModel = createAlertNotificationModel(NotificationType.POLICY_OVERRIDE);
        DetailedNotificationContent test_project4 = DetailedNotificationContent.policy(
            alertPolicyNotificationModel,
            createVulnerabilityUniqueProjectNotificationContent(projectName2),
            projectName2,
            POLICY_FILTER_NAME
        );

        return List.of(test_project, test_project2, test_project3, test_project4);
    }

    private AlertNotificationModel createAlertNotificationModel(NotificationType notificationType) {
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
