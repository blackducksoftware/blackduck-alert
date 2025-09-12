/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.blackduck.integration.alert.common.persistence.model.job.SimpleFilteredDistributionJobResponseModel;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.blackduck.api.manual.component.NotificationContentComponent;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;

class JobNotificationFilterUtilsTest {
    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_VERSION_NAME = "projectVersionName";
    private static final String POLICY_NAME = "policyName";

    @Test
    void doesNotificationApplyToJobNotificationTypeFailureTest() {
        NotificationContentComponent notificationContent = Mockito.mock(NotificationContentComponent.class);
        AlertNotificationModel notificationModel = createAlertNotificationModel(NotificationType.VULNERABILITY);
        DetailedNotificationContent detailedNotificationContent = DetailedNotificationContent.vulnerability(
            notificationModel,
            notificationContent,
            PROJECT_NAME,
            PROJECT_VERSION_NAME,
            List.of("vuln")
        );
        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            List.of(NotificationType.POLICY_OVERRIDE.name()),
            List.of(),
            List.of(),
            List.of(),
            false,
            "",
            ""
        );

        assertFalse(JobNotificationFilterUtils.doesNotificationApplyToJob(jobResponseModel, detailedNotificationContent));
    }

    @Test
    void doesNotificationApplyToJobProjectFailureTest() {
        NotificationContentComponent notificationContent = Mockito.mock(NotificationContentComponent.class);
        AlertNotificationModel notificationModel = createAlertNotificationModel(NotificationType.VULNERABILITY);
        DetailedNotificationContent detailedNotificationContent = DetailedNotificationContent.project(notificationModel, notificationContent, PROJECT_NAME, PROJECT_VERSION_NAME);
        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            List.of(NotificationType.VULNERABILITY.name()),
            List.of("projectDoesNotExist"),
            List.of(),
            List.of(),
            true,
            "",
            ""
        );

        assertFalse(JobNotificationFilterUtils.doesNotificationApplyToJob(jobResponseModel, detailedNotificationContent));
    }

    @Test
    void doesNotificationApplyToJobVulnerabilityTest() {
        NotificationContentComponent notificationContent = Mockito.mock(NotificationContentComponent.class);
        AlertNotificationModel notificationModel = createAlertNotificationModel(NotificationType.VULNERABILITY);
        DetailedNotificationContent detailedNotificationContent = DetailedNotificationContent.vulnerability(
            notificationModel,
            notificationContent,
            PROJECT_NAME,
            PROJECT_VERSION_NAME,
            List.of("vuln")
        );
        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            List.of(NotificationType.VULNERABILITY.name()),
            List.of(),
            List.of(),
            List.of(),
            false,
            "",
            ""
        );

        assertTrue(JobNotificationFilterUtils.doesNotificationApplyToJob(jobResponseModel, detailedNotificationContent));

        FilteredDistributionJobResponseModel jobResponseModelWithFailure = createSimpleFilteredDistributionJobResponseModel(
            List.of(NotificationType.VULNERABILITY.name()),
            List.of(),
            List.of(),
            List.of("vulnDoesNotExist"),
            false,
            "",
            ""
        );
        assertFalse(JobNotificationFilterUtils.doesNotificationApplyToJob(jobResponseModelWithFailure, detailedNotificationContent));
    }

    @Test
    void doesNotificationApplyToJobPolicyOverrideTest() {
        testPolicyNotifications(NotificationType.POLICY_OVERRIDE);
        testPolicyNotifications(NotificationType.RULE_VIOLATION);
        testPolicyNotifications(NotificationType.RULE_VIOLATION_CLEARED);

        NotificationContentComponent notificationContent = Mockito.mock(NotificationContentComponent.class);
        AlertNotificationModel notificationModel = createAlertNotificationModel(NotificationType.POLICY_OVERRIDE);
        DetailedNotificationContent detailedNotificationContent = DetailedNotificationContent.policy(
            notificationModel,
            notificationContent,
            PROJECT_NAME,
            PROJECT_VERSION_NAME,
            POLICY_NAME
        );
        FilteredDistributionJobResponseModel jobResponseModelWithFailure = createSimpleFilteredDistributionJobResponseModel(
            List.of(NotificationType.POLICY_OVERRIDE.name()),
            List.of(),
            List.of("policyDoesNotExist"),
            List.of(),
            false,
            "",
            ""
        );
        assertFalse(JobNotificationFilterUtils.doesNotificationApplyToJob(jobResponseModelWithFailure, detailedNotificationContent));
    }

    @Test
    void doesNotificationApplyToJobDefaultTest() {
        NotificationContentComponent notificationContent = Mockito.mock(NotificationContentComponent.class);
        AlertNotificationModel notificationModel = createAlertNotificationModel(NotificationType.LICENSE_LIMIT);
        DetailedNotificationContent detailedNotificationContent = DetailedNotificationContent.projectless(notificationModel, notificationContent);
        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            List.of(NotificationType.LICENSE_LIMIT.name()),
            List.of(),
            List.of(),
            List.of(),
            false,
            "",
            ""
        );

        assertTrue(JobNotificationFilterUtils.doesNotificationApplyToJob(jobResponseModel, detailedNotificationContent));
    }

    @Test
    void doesNotificationTypeMatchTest() {
        List<String> notificationTypes = List.of(NotificationType.VULNERABILITY.name(), NotificationType.POLICY_OVERRIDE.name());

        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(notificationTypes, List.of(), List.of(), List.of(), false, "", "");

        assertTrue(JobNotificationFilterUtils.doesNotificationTypeMatch(jobResponseModel, NotificationType.VULNERABILITY.name()));
        assertTrue(JobNotificationFilterUtils.doesNotificationTypeMatch(jobResponseModel, NotificationType.POLICY_OVERRIDE.name()));
        assertFalse(JobNotificationFilterUtils.doesNotificationTypeMatch(jobResponseModel, NotificationType.RULE_VIOLATION.name()));
    }

    @Test
    void doesProjectApplyToJobTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            List.of(NotificationType.VULNERABILITY.name()),
            List.of(PROJECT_NAME),
            List.of(),
            List.of(),
            false,
            "",
            ""
        );
        assertTrue(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, PROJECT_NAME, PROJECT_VERSION_NAME));

        FilteredDistributionJobResponseModel jobResponseModelFilterByProject = createSimpleFilteredDistributionJobResponseModel(
            List.of(NotificationType.VULNERABILITY.name()),
            List.of(PROJECT_NAME),
            List.of(),
            List.of(),
            true,
            "",
            ""
        );
        assertTrue(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModelFilterByProject, PROJECT_NAME, PROJECT_VERSION_NAME));
        assertFalse(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModelFilterByProject, "projectDoesNotExist", PROJECT_VERSION_NAME));
    }

    @Test
    void doesProjectApplyToJobPatternMatchingTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            List.of(NotificationType.VULNERABILITY.name()),
            List.of(PROJECT_NAME),
            List.of(),
            List.of(),
            true,
            ".*",
            ""
        );
        assertTrue(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, PROJECT_NAME, PROJECT_VERSION_NAME));

        FilteredDistributionJobResponseModel jobResponseModelFilterByNamePattern = createSimpleFilteredDistributionJobResponseModel(
            List.of(NotificationType.VULNERABILITY.name()),
            List.of(PROJECT_NAME),
            List.of(),
            List.of(),
            true,
            "project*",
            ""
        );
        assertTrue(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModelFilterByNamePattern, PROJECT_NAME, PROJECT_VERSION_NAME));
        assertFalse(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModelFilterByNamePattern, "nonMatchingName", PROJECT_VERSION_NAME));

        jobResponseModelFilterByNamePattern = createSimpleFilteredDistributionJobResponseModel(
            List.of(NotificationType.VULNERABILITY.name()),
            List.of("AlertPerformanceProject-100"),
            List.of(),
            List.of(),
            true,
            "Alert\\w*Project*-\\d+",
            ""
        );
        assertTrue(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModelFilterByNamePattern, "AlertPerformanceProject-100", PROJECT_VERSION_NAME));
    }

    @Test
    void doVulnerabilitySeveritiesApplyToJobTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            List.of(NotificationType.VULNERABILITY.name()),
            List.of(),
            List.of(),
            List.of("vuln1", "vuln2"),
            false,
            "",
            ""
        );
        assertTrue(JobNotificationFilterUtils.doVulnerabilitySeveritiesApplyToJob(jobResponseModel, List.of("vuln1", "vuln2")));
        assertFalse(JobNotificationFilterUtils.doVulnerabilitySeveritiesApplyToJob(jobResponseModel, List.of("vulnDoesNotExist")));

        FilteredDistributionJobResponseModel jobResponseModelNoVulnNames = createSimpleFilteredDistributionJobResponseModel(
            List.of(NotificationType.VULNERABILITY.name()),
            List.of(),
            List.of(),
            List.of(),
            false,
            "",
            ""
        );
        assertTrue(JobNotificationFilterUtils.doVulnerabilitySeveritiesApplyToJob(jobResponseModelNoVulnNames, List.of("vuln1")));
    }

    @Test
    void doesPolicyApplyToJobTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            List.of(NotificationType.POLICY_OVERRIDE.name()),
            List.of(),
            List.of(POLICY_NAME),
            List.of(),
            false,
            "",
            ""
        );
        assertTrue(JobNotificationFilterUtils.doesPolicyApplyToJob(jobResponseModel, POLICY_NAME));
        assertFalse(JobNotificationFilterUtils.doesPolicyApplyToJob(jobResponseModel, "policyDoesNotExist"));

        FilteredDistributionJobResponseModel jobResponseModelNoPolicyNames = createSimpleFilteredDistributionJobResponseModel(
            List.of(NotificationType.POLICY_OVERRIDE.name()),
            List.of(),
            List.of(),
            List.of(),
            false,
            "",
            ""
        );
        assertTrue(JobNotificationFilterUtils.doesPolicyApplyToJob(jobResponseModelNoPolicyNames, POLICY_NAME));
    }

    @Test
    void doesProjectVersionNameMatchTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(List.of(), List.of(), List.of(), List.of(), true, "", "1.0.*");
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameNotMatchTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(List.of(), List.of(), List.of(), List.of(), true, "", "1.0.*");
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "fakeNews");

        assertFalse(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameMatchWithWrongProjectTest() {
        List<String> notValidProjects = List.of("fake project", "project 2", "flippant");
        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            List.of(),
            notValidProjects,
            List.of(),
            List.of(),
            true,
            "",
            "1.0.*"
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertFalse(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameMatchWithProjectTest() {
        String validProject = "projectName";
        List<String> projects = List.of("fake project", "project 2", "flippant", validProject);
        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(List.of(), projects, List.of(), List.of(), true, "", "1.0.*");
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameMatchWithNoProjectTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(List.of(), List.of(), List.of(), List.of(), true, "", "1.0.*");
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameMatchWithProjectPatternMatchTest() {
        List<String> projects = List.of("fake project", "project 2", "flippant");
        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            List.of(),
            projects,
            List.of(),
            List.of(),
            true,
            "projectN.*",
            "1.0.*"
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameMatchWithInvalidProjectPatternMatchTest() {
        List<String> projects = List.of("fake project", "project 2", "flippant");
        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            List.of(),
            projects,
            List.of(),
            List.of(),
            true,
            "projectN.*",
            "1.0.*"
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "wrong", "1.0.0");

        assertFalse(doesProjectApplyToJob);
    }

    @Test
    void verifyJobDoesNotMatchProjectNamePatternTest() {
        FilteredDistributionJobResponseModel filteredDistributionJobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            true,
            "BM.*",
            "^a\\d$"
        );

        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(filteredDistributionJobResponseModel, "Won'tMatch", "a1");

        assertFalse(doesProjectApplyToJob);
    }

    @Test
    void verifyJobDoesNotFilterByProjectSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel filteredDistributionJobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            false,
            false,
            "",
            ""
        );

        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(filteredDistributionJobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void verifyJobDoesNotMatchProjectAndPatternsEmptySimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel filteredDistributionJobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            false,
            "",
            ""
        );

        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(filteredDistributionJobResponseModel, "projectName", "1.0.0");

        assertFalse(doesProjectApplyToJob);
    }

    @Test
    void verifyJobDoesNotMatchProjectNamePatternSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel filteredDistributionJobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            false,
            "BM.*",
            "^a\\d$"
        );

        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(filteredDistributionJobResponseModel, "Won'tMatch", "a1");

        assertFalse(doesProjectApplyToJob);
    }

    @Test
    void doesProjectNameMatchWithProjectSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            true,
            "projectN*",
            ""
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void doesProjectNameMatchNoProjectSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            false,
            "projectN.*",
            ""
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameMatchSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            false,
            "",
            "1.0.*"
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameMatchSelectedProjectSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            true,
            "",
            "1.0.*"
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameNotMatchSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            false,
            "",
            "1.0.*"
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "fakeNews");

        assertFalse(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameMatchWithProjectSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            true,
            "",
            "1.0.*"
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameMatchWithProjectPatternMatchSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            true,
            false,
            "projectN.*",
            "1.0.*"
        );
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameMatchWithInvalidProjectPatternMatchSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(true, false, "projectN.*", "1.0.*");
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "wrong", "1.0.0");

        assertFalse(doesProjectApplyToJob);
    }

    @Test
    void doesProjectVersionNameNotMatchWithValidProjectPatternMatchSimpleResponseTest() {
        SimpleFilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(true, false, "projectN.*", "1.0.*");
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "wrong");

        assertFalse(doesProjectApplyToJob);
    }

    private FilteredDistributionJobResponseModel createSimpleFilteredDistributionJobResponseModel(
        List<String> notificationTypes,
        List<String> projectNames,
        List<String> policyNames,
        List<String> vulnerabilitySeverityNames,
        boolean filterByProject,
        String projectNamePattern,
        String projectNameVersionPattern
    ) {
        List<BlackDuckProjectDetailsModel> blackDuckProjectDetailsModel = projectNames.stream()
            .map(projectName -> new BlackDuckProjectDetailsModel(projectName, "href"))
            .collect(Collectors.toList());
        return new FilteredDistributionJobResponseModel(
            UUID.randomUUID(),
            ProcessingType.DEFAULT,
            "channelName",
            "jobName",
            notificationTypes,
            blackDuckProjectDetailsModel,
            policyNames,
            vulnerabilitySeverityNames,
            filterByProject,
            projectNamePattern,
            projectNameVersionPattern
        );
    }

    private SimpleFilteredDistributionJobResponseModel createSimpleFilteredDistributionJobResponseModel(
        boolean filterByProject,
        boolean hasProjects,
        String projectNamePattern,
        String projectNameVersionPattern
    ) {
        return new SimpleFilteredDistributionJobResponseModel(
            1L,
            UUID.randomUUID(),
            filterByProject,
            projectNamePattern,
            projectNameVersionPattern,
            hasProjects
        );
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
            String.format("content-id-%s", UUID.randomUUID()),
            false
        );
    }

    private void testPolicyNotifications(NotificationType notificationType) {
        NotificationContentComponent notificationContent = Mockito.mock(NotificationContentComponent.class);
        AlertNotificationModel notificationModel = createAlertNotificationModel(notificationType);
        DetailedNotificationContent detailedNotificationContent = DetailedNotificationContent.policy(
            notificationModel,
            notificationContent,
            PROJECT_NAME,
            POLICY_NAME,
            PROJECT_VERSION_NAME
        );
        FilteredDistributionJobResponseModel jobResponseModel = createSimpleFilteredDistributionJobResponseModel(
            List.of(notificationType.name()),
            List.of(),
            List.of(),
            List.of(),
            false,
            "",
            ""
        );

        assertTrue(JobNotificationFilterUtils.doesNotificationApplyToJob(jobResponseModel, detailedNotificationContent));
    }

}
