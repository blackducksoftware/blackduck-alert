/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.persistence.accessor.JobAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.ProcessingJobAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.provider.blackduck.processor.model.VulnerabilityUniqueProjectNotificationContent;
import com.blackduck.integration.alert.util.AlertIntegrationTest;
import com.blackduck.integration.blackduck.api.generated.enumeration.VulnerabilitySeverityType;
import com.blackduck.integration.blackduck.api.manual.component.AffectedProjectVersion;
import com.blackduck.integration.blackduck.api.manual.component.VulnerabilityNotificationContent;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;

@Transactional
@AlertIntegrationTest
public class JobNotificationMapperTestIT {
    private static final List<UUID> CREATED_JOBS = new LinkedList<>();
    private static final String PROJECT_NAME_1 = "test_project";
    private static final String PROJECT_VERSION_NAME_1 = "first_version";
    private static final String POLICY_FILTER_NAME = "policyName";

    @Autowired
    public JobAccessor jobAccessor;
    @Autowired
    public ProcessingJobAccessor processingJobAccessor;

    @AfterEach
    public void removeCreatedJobsIfExist() {
        CREATED_JOBS.forEach(jobAccessor::deleteJob);
        CREATED_JOBS.clear();
    }

    @Test
    public void test2Notifications15JobsMultiSeverity() {
        //Test the case where a job may have one or multiple types of vulnerability severities across multiple pages.
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 100));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name(), VulnerabilitySeverityType.HIGH.name()), 15));
        List<DetailedNotificationContent> notifications = new ArrayList<>();
        notifications.addAll(createVulnerabilityNotificationWrappers(List.of(VulnerabilitySeverityType.LOW.name()), "testProject1", null));
        notifications.addAll(createVulnerabilityNotificationWrappers(List.of(VulnerabilitySeverityType.HIGH.name()), "testProject2", null));

        runTest(notifications, 115);
    }

    @Test
    public void test3Notifications30JobsMultiSeverity() {
        //Test the case where a multiple jobs of differing vulnerability severities appear across multiple pages.
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 115));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.HIGH.name()), 100));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.CRITICAL.name()), 50));

        List<DetailedNotificationContent> notifications = new ArrayList<>();
        notifications.addAll(createVulnerabilityNotificationWrappers(List.of(VulnerabilitySeverityType.LOW.name()), "testProject1", null));
        notifications.addAll(createVulnerabilityNotificationWrappers(List.of(VulnerabilitySeverityType.HIGH.name()), "testProject2", null));
        notifications.addAll(createVulnerabilityNotificationWrappers(List.of(VulnerabilitySeverityType.CRITICAL.name()), "testProject3", null));

        runTest(notifications, 265);
    }

    private void runTest(List<DetailedNotificationContent> notifications, int expectedNumOfJobs) {
        JobNotificationMapper jobNotificationMapper = new JobNotificationMapper(processingJobAccessor);

        Set<FilteredJobNotificationWrapper> mappedNotifications = jobNotificationMapper.mapJobsToNotifications(notifications, List.of(FrequencyType.REAL_TIME));

        assertEquals(expectedNumOfJobs, mappedNotifications.size());
    }

    @Test
    public void mapJobsTest() {
        createJobs(createDistributionJobModels());

        JobNotificationMapper jobNotificationMapper = new JobNotificationMapper(processingJobAccessor);
        Set<FilteredJobNotificationWrapper> mappedNotifications = jobNotificationMapper.mapJobsToNotifications(
            createNotificationWrappers(),
            List.of(FrequencyType.REAL_TIME)
        );

        assertEquals(3, mappedNotifications.size());
        for (FilteredJobNotificationWrapper mappedJobNotifications : mappedNotifications) {
            List<NotificationContentWrapper> jobNotifications = mappedJobNotifications.getJobNotifications();
            assertFalse(jobNotifications.isEmpty(), "Expected the list not to be empty");
            assertTrue(jobNotifications.size() < 4, "Expected the list to contain fewer elements");
        }
    }

    @Test
    public void mapNoJobsTest() {
        JobNotificationMapper jobNotificationMapper = new JobNotificationMapper(processingJobAccessor);
        Set<FilteredJobNotificationWrapper> mappedNotifications = jobNotificationMapper.mapJobsToNotifications(
            createNotificationWrappers(),
            List.of(FrequencyType.REAL_TIME)
        );
        assertEquals(0, mappedNotifications.size(), "Expected the list to be empty");
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
                UUID.randomUUID(),
                0L,
                true,
                // Regex to verify we retrieve notifications without a number in the name (PROJECT_NAME_1)
                "^([^0-9]*)$",
                null,
                List.of(NotificationType.VULNERABILITY.name()),
                List.of(),
                List.of(),
                List.of(),
                new SlackJobDetailsModel(null, "webhook", "username")
            ))
        );

        testProjectJob();
    }

    @Test
    public void extractJobsWithMatchingProjectVersionNamePatternFilter() {
        createJobs(List.of(
            new DistributionJobRequestModel(
                true,
                "name",
                FrequencyType.REAL_TIME,
                ProcessingType.DIGEST,
                ChannelKeys.SLACK.getUniversalKey(),
                UUID.randomUUID(),
                0L,
                true,
                null,
                // Regex to verify we retrieve notifications without a number in the name (PROJECT_VERSION_NAME_1)
                "^([^0-9]*)$",
                List.of(NotificationType.VULNERABILITY.name()),
                List.of(),
                List.of(),
                List.of(),
                new SlackJobDetailsModel(null, "webhook", "username")
            ))
        );

        testProjectJob();
    }

    private void testSingleJob(DistributionJobRequestModel jobRequestModel, int expectedMappedNotifications) {
        createJobs(List.of(jobRequestModel));

        JobNotificationMapper jobNotificationMapper = new JobNotificationMapper(processingJobAccessor);
        Set<FilteredJobNotificationWrapper> pageMappedNotifications = jobNotificationMapper.mapJobsToNotifications(
            createNotificationWrappers(),
            List.of(FrequencyType.REAL_TIME)
        );

        assertEquals(1, pageMappedNotifications.size());
        FilteredJobNotificationWrapper jobNotificationWrapper = pageMappedNotifications.stream().findFirst().orElse(null);

        List<NotificationContentWrapper> filterableNotificationWrappers = jobNotificationWrapper.getJobNotifications();
        assertEquals(expectedMappedNotifications, filterableNotificationWrappers.size());
    }

    private void testProjectJob() {
        JobNotificationMapper defaultJobNotificationExtractor = new JobNotificationMapper(processingJobAccessor);
        List<DetailedNotificationContent> notificationWrappers = createNotificationWrappers();
        Set<FilteredJobNotificationWrapper> pageMappedNotifications = defaultJobNotificationExtractor.mapJobsToNotifications(
            notificationWrappers,
            List.of(FrequencyType.REAL_TIME)
        );
        assertEquals(1, pageMappedNotifications.size());

        List<NotificationContentWrapper> filterableNotificationWrappers = pageMappedNotifications.stream().findFirst().orElse(null).getJobNotifications();
        assertEquals(1, filterableNotificationWrappers.size());

        NotificationContentWrapper filterableNotificationWrapper = filterableNotificationWrappers.get(0);

        assertEquals(NotificationType.VULNERABILITY.name(), filterableNotificationWrapper.extractNotificationType());
        VulnerabilityUniqueProjectNotificationContent vulnerabilityUniqueProjectNotificationContent = (VulnerabilityUniqueProjectNotificationContent) filterableNotificationWrapper.getNotificationContent();

        assertEquals(PROJECT_NAME_1, vulnerabilityUniqueProjectNotificationContent.getAffectedProjectVersion().getProjectName());

    }

    private void createJobs(List<DistributionJobRequestModel> jobs) {
        jobs
            .stream()
            .map(jobAccessor::createJob)
            .map(DistributionJobModel::getJobId)
            .forEach(CREATED_JOBS::add);
    }

    private List<DistributionJobRequestModel> createDistributionJobModels(List<String> vulnTypes, int numberOfJobs) {
        List<DistributionJobRequestModel> jobModels = new ArrayList<>();
        for (int i = 0; i < numberOfJobs; i++) {
            DistributionJobRequestModel distributionJobRequestModel = createJobRequestModel(
                FrequencyType.REAL_TIME,
                ProcessingType.DIGEST,
                List.of(),
                List.of(NotificationType.VULNERABILITY.name()),
                vulnTypes,
                List.of()
            );
            jobModels.add(distributionJobRequestModel);
        }
        return jobModels;
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
            UUID.randomUUID(),
            0L,
            projectNames != null && !projectNames.isEmpty(),
            null,
            null,
            notificationTypes,
            blackDuckProjectDetailsModels,
            policies,
            vulns,
            new SlackJobDetailsModel(null, "webhook", "username")
        );
    }

    private VulnerabilityUniqueProjectNotificationContent createVulnerabilityUniqueProjectNotificationContent(String projectName) {
        AffectedProjectVersion affectedProjectVersion = new AffectedProjectVersion();
        affectedProjectVersion.setProjectName(projectName);
        return new VulnerabilityUniqueProjectNotificationContent(new VulnerabilityNotificationContent(), affectedProjectVersion);
    }

    private List<DetailedNotificationContent> createVulnerabilityNotificationWrappers(List<String> vulnerabilitySeverities, String projectName, String projectVersionName) {
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(NotificationType.VULNERABILITY);
        DetailedNotificationContent test_project = DetailedNotificationContent.vulnerability(
            alertNotificationModel,
            createVulnerabilityUniqueProjectNotificationContent(projectName),
            projectName,
            projectVersionName,
            vulnerabilitySeverities
        );
        return List.of(test_project);
    }

    private List<DetailedNotificationContent> createNotificationWrappers() {
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(NotificationType.VULNERABILITY);
        DetailedNotificationContent test_project = DetailedNotificationContent.vulnerability(
            alertNotificationModel,
            createVulnerabilityUniqueProjectNotificationContent(PROJECT_NAME_1),
            PROJECT_NAME_1,
            PROJECT_VERSION_NAME_1,
            List.of(VulnerabilitySeverityType.LOW.name())
        );
        String projectName1 = "test_project1";
        DetailedNotificationContent test_project2 = DetailedNotificationContent.vulnerability(
            alertNotificationModel,
            createVulnerabilityUniqueProjectNotificationContent(projectName1),
            projectName1,
            "version1",
            List.of(VulnerabilitySeverityType.HIGH.name())
        );
        String projectName2 = "test_project2";
        DetailedNotificationContent test_project3 = DetailedNotificationContent.vulnerability(
            alertNotificationModel,
            createVulnerabilityUniqueProjectNotificationContent(projectName2),
            projectName2,
            "version2",
            List.of(VulnerabilitySeverityType.LOW.name(), VulnerabilitySeverityType.HIGH.name())
        );
        AlertNotificationModel alertPolicyNotificationModel = createAlertNotificationModel(NotificationType.POLICY_OVERRIDE);
        DetailedNotificationContent test_project4 = DetailedNotificationContent.policy(
            alertPolicyNotificationModel,
            createVulnerabilityUniqueProjectNotificationContent(projectName2),
            projectName2,
            "1.0.0",
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
            false,
            String.format("content-id-%s", UUID.randomUUID())
        );
    }

}
