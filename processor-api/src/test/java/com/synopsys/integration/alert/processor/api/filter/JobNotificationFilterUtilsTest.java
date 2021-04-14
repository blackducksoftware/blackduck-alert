package com.synopsys.integration.alert.processor.api.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public class JobNotificationFilterUtilsTest {
    private static final String PROJECT_NAME = "projectName";
    private static final String POLICY_NAME = "policyName";

    @Test
    public void doesNotificationApplyToJobTest() {
        AlertNotificationModel notificationModel = createAlertNotificationModel(NotificationType.VULNERABILITY);
        //TODO:  May not need notificationContent for the unit test, see if a null would work fine here
        DetailedNotificationContent detailedNotificationContent = DetailedNotificationContent.vulnerability(notificationModel, null, PROJECT_NAME, List.of("vuln"));
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(NotificationType.VULNERABILITY.name()), List.of(), List.of(), List.of(), false, "");

        //assertTrue();
    }

    @Test
    public void doesNotificationTypeMatchTest() {
        List<String> notificationTypes = List.of(NotificationType.VULNERABILITY.name(), NotificationType.POLICY_OVERRIDE.name());

        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(notificationTypes, List.of(), List.of(), List.of(), false, "");

        assertTrue(JobNotificationFilterUtils.doesNotificationTypeMatch(jobResponseModel, NotificationType.VULNERABILITY.name()));
        assertTrue(JobNotificationFilterUtils.doesNotificationTypeMatch(jobResponseModel, NotificationType.POLICY_OVERRIDE.name()));
        assertFalse(JobNotificationFilterUtils.doesNotificationTypeMatch(jobResponseModel, NotificationType.RULE_VIOLATION.name()));
    }

    @Test
    public void doesProjectApplyToJobTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(NotificationType.VULNERABILITY.name()), List.of(PROJECT_NAME), List.of(), List.of(), false, "");
        assertTrue(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, PROJECT_NAME));

        FilteredDistributionJobResponseModel jobResponseModelFilterByProject = createFilteredDistributionJobResponseModel(List.of(NotificationType.VULNERABILITY.name()), List.of(PROJECT_NAME), List.of(), List.of(), true, "");
        assertTrue(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModelFilterByProject, PROJECT_NAME));
        assertFalse(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModelFilterByProject, "projectDoesNotExist"));
    }

    @Test
    public void doesProjectApplyToJobPatternMatchingTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(NotificationType.VULNERABILITY.name()), List.of(PROJECT_NAME), List.of(), List.of(), true, ".*");
        assertTrue(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModel, PROJECT_NAME));

        FilteredDistributionJobResponseModel jobResponseModelFilterByNamePattern = createFilteredDistributionJobResponseModel(List.of(NotificationType.VULNERABILITY.name()), List.of(PROJECT_NAME), List.of(), List.of(), true, "project*");
        assertTrue(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModelFilterByNamePattern, PROJECT_NAME));
        assertFalse(JobNotificationFilterUtils.doesProjectApplyToJob(jobResponseModelFilterByNamePattern, "nonMatchingName"));
    }

    @Test
    public void doVulnerabilitySeveritiesApplyToJobTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(NotificationType.VULNERABILITY.name()), List.of(), List.of(), List.of("vuln1", "vuln2"), false, "");
        assertTrue(JobNotificationFilterUtils.doVulnerabilitySeveritiesApplyToJob(jobResponseModel, List.of("vuln1", "vuln2")));
        assertFalse(JobNotificationFilterUtils.doVulnerabilitySeveritiesApplyToJob(jobResponseModel, List.of("vulnDoesNotExist")));

        FilteredDistributionJobResponseModel jobResponseModelNoVulnNames = createFilteredDistributionJobResponseModel(List.of(NotificationType.VULNERABILITY.name()), List.of(), List.of(), List.of(), false, "");
        assertTrue(JobNotificationFilterUtils.doVulnerabilitySeveritiesApplyToJob(jobResponseModelNoVulnNames, List.of("vuln1")));
    }

    @Test
    public void doesPolicyApplyToJobTest() {
        FilteredDistributionJobResponseModel jobResponseModel = createFilteredDistributionJobResponseModel(List.of(NotificationType.POLICY_OVERRIDE.name()), List.of(), List.of(POLICY_NAME), List.of(), false, "");
        assertTrue(JobNotificationFilterUtils.doesPolicyApplyToJob(jobResponseModel, POLICY_NAME));
        assertFalse(JobNotificationFilterUtils.doesPolicyApplyToJob(jobResponseModel, "policyDoesNotExist"));

        FilteredDistributionJobResponseModel jobResponseModelNoPolicyNames = createFilteredDistributionJobResponseModel(List.of(NotificationType.POLICY_OVERRIDE.name()), List.of(), List.of(), List.of(), false, "");
        assertTrue(JobNotificationFilterUtils.doesPolicyApplyToJob(jobResponseModelNoPolicyNames, POLICY_NAME));
    }

    private FilteredDistributionJobResponseModel createFilteredDistributionJobResponseModel(
        List<String> notificationTypes,
        List<String> projectNames,
        List<String> policyNames,
        List<String> vulnerabilitySeverityNames,
        boolean filerByProject,
        String projectNamePattern
    ) {
        List<BlackDuckProjectDetailsModel> blackDuckProjectDetailsModel = projectNames.stream()
                                                                              .map(projectName -> new BlackDuckProjectDetailsModel(projectName, "href"))
                                                                              .collect(Collectors.toList());
        return new FilteredDistributionJobResponseModel(
            UUID.randomUUID(),
            ProcessingType.DEFAULT,
            "channelName",
            notificationTypes,
            blackDuckProjectDetailsModel,
            policyNames,
            vulnerabilitySeverityNames,
            filerByProject,
            projectNamePattern);
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
