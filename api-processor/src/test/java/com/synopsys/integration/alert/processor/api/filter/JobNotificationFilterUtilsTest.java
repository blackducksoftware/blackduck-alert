package com.synopsys.integration.alert.processor.api.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.NotificationContentComponent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public class JobNotificationFilterUtilsTest {
    private static final String PROJECT_NAME = "projectName";
    private static final String PROJECT_VERSION_NAME = "projectVersionName";
    private static final String POLICY_NAME = "policyName";

    @Test
    public void doesNotificationApplyToJobNotificationTypeFailureTest() {
        NotificationContentComponent notificationContent = Mockito.mock(NotificationContentComponent.class);
        AlertNotificationModel notificationModel = createAlertNotificationModel(NotificationType.VULNERABILITY);
        DetailedNotificationContent detailedNotificationContent = DetailedNotificationContent.vulnerability(notificationModel, notificationContent, PROJECT_NAME, PROJECT_VERSION_NAME, List.of("vuln"));
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(NotificationType.POLICY_OVERRIDE.name()), List.of(), List.of(), List.of(), false, "", "");

        assertFalse(JobNotificationFilterUtils.doesNotificationApplyToJob(jobResponseModel, detailedNotificationContent));
    }

    @Test
    public void doesNotificationApplyToJobProjectFailureTest() {
        NotificationContentComponent notificationContent = Mockito.mock(NotificationContentComponent.class);
        AlertNotificationModel notificationModel = createAlertNotificationModel(NotificationType.VULNERABILITY);
        DetailedNotificationContent detailedNotificationContent = DetailedNotificationContent.project(notificationModel, notificationContent, PROJECT_NAME, PROJECT_VERSION_NAME);
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(NotificationType.VULNERABILITY.name()), List.of("projectDoesNotExist"), List.of(), List.of(), true, "", "");

        assertFalse(JobNotificationFilterUtils.doesNotificationApplyToJob(jobResponseModel, detailedNotificationContent));
    }

    @Test
    public void doesNotificationApplyToJobVulnerabilityTest() {
        NotificationContentComponent notificationContent = Mockito.mock(NotificationContentComponent.class);
        AlertNotificationModel notificationModel = createAlertNotificationModel(NotificationType.VULNERABILITY);
        DetailedNotificationContent detailedNotificationContent = DetailedNotificationContent.vulnerability(notificationModel, notificationContent, PROJECT_NAME, PROJECT_VERSION_NAME, List.of("vuln"));
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(NotificationType.VULNERABILITY.name()), List.of(), List.of(), List.of(), false, "", "");

        assertTrue(JobNotificationFilterUtils.doesNotificationApplyToJob(jobResponseModel, detailedNotificationContent));

        FilteredDistributionJobResponseModel jobResponseModelWithFailure = createFilteredDistributionJobResponseModel(List.of(NotificationType.VULNERABILITY.name()), List.of(), List.of(), List.of("vulnDoesNotExist"), false, "", "");
        assertFalse(JobNotificationFilterUtils.doesNotificationApplyToJob(jobResponseModelWithFailure, detailedNotificationContent));
    }

    @Test
    public void doesNotificationApplyToJobPolicyOverrideTest() {
        testPolicyNotifications(NotificationType.POLICY_OVERRIDE);
        testPolicyNotifications(NotificationType.RULE_VIOLATION);
        testPolicyNotifications(NotificationType.RULE_VIOLATION_CLEARED);

        NotificationContentComponent notificationContent = Mockito.mock(NotificationContentComponent.class);
        AlertNotificationModel notificationModel = createAlertNotificationModel(NotificationType.POLICY_OVERRIDE);
        DetailedNotificationContent detailedNotificationContent = DetailedNotificationContent.policy(notificationModel, notificationContent, PROJECT_NAME, PROJECT_VERSION_NAME, POLICY_NAME);
        FilteredDistributionJobResponseModel jobResponseModelWithFailure = createFilteredDistributionJobResponseModel(List.of(NotificationType.POLICY_OVERRIDE.name()), List.of(), List.of("policyDoesNotExist"), List.of(), false, "", "");
        assertFalse(JobNotificationFilterUtils.doesNotificationApplyToJob(jobResponseModelWithFailure, detailedNotificationContent));
    }

    @Test
    public void doesNotificationApplyToJobDefaultTest() {
        NotificationContentComponent notificationContent = Mockito.mock(NotificationContentComponent.class);
        AlertNotificationModel notificationModel = createAlertNotificationModel(NotificationType.LICENSE_LIMIT);
        DetailedNotificationContent detailedNotificationContent = DetailedNotificationContent.projectless(notificationModel, notificationContent);
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(NotificationType.LICENSE_LIMIT.name()), List.of(), List.of(), List.of(), false, "", "");

        assertTrue(JobNotificationFilterUtils.doesNotificationApplyToJob(jobResponseModel, detailedNotificationContent));
    }

    @Test
    public void doesNotificationTypeMatchTest() {
        List<String> notificationTypes = List.of(NotificationType.VULNERABILITY.name(), NotificationType.POLICY_OVERRIDE.name());

        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(notificationTypes, List.of(), List.of(), List.of(), false, "", "");

        assertTrue(JobNotificationFilterUtils.doesNotificationTypeMatch(jobResponseModel, NotificationType.VULNERABILITY.name()));
        assertTrue(JobNotificationFilterUtils.doesNotificationTypeMatch(jobResponseModel, NotificationType.POLICY_OVERRIDE.name()));
        assertFalse(JobNotificationFilterUtils.doesNotificationTypeMatch(jobResponseModel, NotificationType.RULE_VIOLATION.name()));
    }

    @Test
    public void doesProjectApplyToJobTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(NotificationType.VULNERABILITY.name()), List.of(PROJECT_NAME), List.of(), List.of(), false, "", "");
        assertTrue(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, PROJECT_NAME, PROJECT_VERSION_NAME));

        FilteredDistributionJobResponseModel jobResponseModelFilterByProject = createFilteredDistributionJobResponseModel(List.of(NotificationType.VULNERABILITY.name()), List.of(PROJECT_NAME), List.of(), List.of(), true, "", "");
        assertTrue(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModelFilterByProject, PROJECT_NAME, PROJECT_VERSION_NAME));
        assertFalse(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModelFilterByProject, "projectDoesNotExist", PROJECT_VERSION_NAME));
    }

    @Test
    public void doesProjectApplyToJobPatternMatchingTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(NotificationType.VULNERABILITY.name()), List.of(PROJECT_NAME), List.of(), List.of(), true, ".*", "");
        assertTrue(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, PROJECT_NAME, PROJECT_VERSION_NAME));

        FilteredDistributionJobResponseModel jobResponseModelFilterByNamePattern = createFilteredDistributionJobResponseModel(List.of(NotificationType.VULNERABILITY.name()), List.of(PROJECT_NAME), List.of(), List.of(), true, "project*",
            "");
        assertTrue(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModelFilterByNamePattern, PROJECT_NAME, PROJECT_VERSION_NAME));
        assertFalse(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModelFilterByNamePattern, "nonMatchingName", PROJECT_VERSION_NAME));
    }

    @Test
    public void doVulnerabilitySeveritiesApplyToJobTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(NotificationType.VULNERABILITY.name()), List.of(), List.of(), List.of("vuln1", "vuln2"), false, "", "");
        assertTrue(JobNotificationFilterUtils.doVulnerabilitySeveritiesApplyToJob(jobResponseModel, List.of("vuln1", "vuln2")));
        assertFalse(JobNotificationFilterUtils.doVulnerabilitySeveritiesApplyToJob(jobResponseModel, List.of("vulnDoesNotExist")));

        FilteredDistributionJobResponseModel jobResponseModelNoVulnNames = createFilteredDistributionJobResponseModel(List.of(NotificationType.VULNERABILITY.name()), List.of(), List.of(), List.of(), false, "", "");
        assertTrue(JobNotificationFilterUtils.doVulnerabilitySeveritiesApplyToJob(jobResponseModelNoVulnNames, List.of("vuln1")));
    }

    @Test
    public void doesPolicyApplyToJobTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(NotificationType.POLICY_OVERRIDE.name()), List.of(), List.of(POLICY_NAME), List.of(), false, "", "");
        assertTrue(JobNotificationFilterUtils.doesPolicyApplyToJob(jobResponseModel, POLICY_NAME));
        assertFalse(JobNotificationFilterUtils.doesPolicyApplyToJob(jobResponseModel, "policyDoesNotExist"));

        FilteredDistributionJobResponseModel jobResponseModelNoPolicyNames = createFilteredDistributionJobResponseModel(List.of(NotificationType.POLICY_OVERRIDE.name()), List.of(), List.of(), List.of(), false, "", "");
        assertTrue(JobNotificationFilterUtils.doesPolicyApplyToJob(jobResponseModelNoPolicyNames, POLICY_NAME));
    }

    @Test
    public void doesProjectVersionNameMatchTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(), List.of(), List.of(), List.of(), true, "", "1.0.*");
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    public void doesProjectVersionNameNotMatchTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(), List.of(), List.of(), List.of(), true, "", "1.0.*");
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "fakeNews");

        assertFalse(doesProjectApplyToJob);
    }

    @Test
    public void doesProjectVersionNameMatchWithWrongProjectTest() {
        List<String> notValidProjects = List.of("fake project", "project 2", "flippant");
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(), notValidProjects, List.of(), List.of(), true, "", "1.0.*");
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertFalse(doesProjectApplyToJob);
    }

    @Test
    public void doesProjectVersionNameMatchWithProjectTest() {
        String validProject = "projectName";
        List<String> projects = List.of("fake project", "project 2", "flippant", validProject);
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(), projects, List.of(), List.of(), true, "", "1.0.*");
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    public void doesProjectVersionNameMatchWithNoProjectTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(), List.of(), List.of(), List.of(), true, "", "1.0.*");
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    public void doesProjectVersionNameMatchWithProjectPatternMatchTest() {
        List<String> projects = List.of("fake project", "project 2", "flippant");
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(), projects, List.of(), List.of(), true, "projectN.*", "1.0.*");
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "projectName", "1.0.0");

        assertTrue(doesProjectApplyToJob);
    }

    @Test
    public void doesProjectVersionNameMatchWithInvalidProjectPatternMatchTest() {
        List<String> projects = List.of("fake project", "project 2", "flippant");
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(), projects, List.of(), List.of(), true, "projectN.*", "1.0.*");
        boolean doesProjectApplyToJob = JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, "wrong", "1.0.0");

        assertFalse(doesProjectApplyToJob);
    }

    private FilteredDistributionJobResponseModel createFilteredDistributionJobResponseModel(
        List<String> notificationTypes,
        List<String> projectNames,
        List<String> policyNames,
        List<String> vulnerabilitySeverityNames,
        boolean filerByProject,
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
            filerByProject,
            projectNamePattern,
            projectNameVersionPattern);
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

    private void testPolicyNotifications(NotificationType notificationType) {
        NotificationContentComponent notificationContent = Mockito.mock(NotificationContentComponent.class);
        AlertNotificationModel notificationModel = createAlertNotificationModel(notificationType);
        DetailedNotificationContent detailedNotificationContent = DetailedNotificationContent.policy(notificationModel, notificationContent, PROJECT_NAME, POLICY_NAME, PROJECT_VERSION_NAME);
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(notificationType.name()), List.of(), List.of(), List.of(), false, "", "");

        assertTrue(JobNotificationFilterUtils.doesNotificationApplyToJob(jobResponseModel, detailedNotificationContent));
    }

}
