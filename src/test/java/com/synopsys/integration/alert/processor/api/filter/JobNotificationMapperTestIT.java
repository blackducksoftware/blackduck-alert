package com.synopsys.integration.alert.processor.api.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.provider.blackduck.processor.model.VulnerabilityUniqueProjectNotificationContent;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.blackduck.api.generated.enumeration.VulnerabilitySeverityType;
import com.synopsys.integration.blackduck.api.manual.component.AffectedProjectVersion;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@AlertIntegrationTest
public class JobNotificationMapperTestIT {
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
    public void testMapping() {
        createJobs(createDistributionJobModels());

        JobNotificationMapper jobNotificationMapper = new JobNotificationMapper(jobAccessor);
        /*
        StatefulAlertPagedModel<FilteredJobNotificationWrapper> mappedNotifications = jobNotificationMapper.mapJobsToNotifications(createNotificationWrappers(), List.of(FrequencyType.REAL_TIME));
        StatefulAlertPagedModel<FilteredJobNotificationWrapper> nextPage = mappedNotifications.retrieveNextPage();
        StatefulAlertPagedModel<FilteredJobNotificationWrapper> nextPage2 = nextPage.retrieveNextPage();
        StatefulAlertPagedModel<FilteredJobNotificationWrapper> lastPage = nextPage2.retrieveNextPage();
         */
        //List<NotificationContentWrapper> notificationContentWrappers = new ArrayList<>();
        Set<NotificationContentWrapper> notificationContentWrappers = new HashSet<>();
        StatefulAlertPagedModel<FilteredJobNotificationWrapper> mappedNotifications = jobNotificationMapper.mapJobsToNotifications(createNotificationWrappers(), List.of(FrequencyType.REAL_TIME));
        do {
            for (FilteredJobNotificationWrapper jobNotificationWrapper : mappedNotifications.getCurrentModels()) {
                notificationContentWrappers.addAll(jobNotificationWrapper.getJobNotifications());
            }
            //assertEquals(10, mappedNotifications.getCurrentModels().size());
            mappedNotifications = mappedNotifications.retrieveNextPage();
        } while (mappedNotifications.hasNextPage());
        //} while (!mappedNotifications.getCurrentModels().isEmpty());

        assertEquals(2, notificationContentWrappers.size());
    }

    private void createJobs(List<DistributionJobRequestModel> jobs) {
        jobs
            .stream()
            .map(jobAccessor::createJob)
            .map(DistributionJobModel::getJobId)
            .forEach(CREATED_JOBS::add);
    }

    private List<DistributionJobRequestModel> createDistributionJobModels() {
        List<DistributionJobRequestModel> jobModels = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DistributionJobRequestModel distributionJobRequestModel = createJobRequestModel(
                FrequencyType.REAL_TIME,
                ProcessingType.DIGEST,
                List.of(),
                List.of(NotificationType.VULNERABILITY.name()),
                List.of(VulnerabilitySeverityType.LOW.name()),
                List.of()
            );
            jobModels.add(distributionJobRequestModel);
        }
        for (int i = 0; i < 10; i++) {
            DistributionJobRequestModel distributionJobRequestModel = createJobRequestModel(
                FrequencyType.REAL_TIME,
                ProcessingType.DIGEST,
                List.of(),
                List.of(NotificationType.VULNERABILITY.name()),
                List.of(VulnerabilitySeverityType.HIGH.name()),
                List.of()
            );
            jobModels.add(distributionJobRequestModel);
        }
        return jobModels;
        /*
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
        */
    }

    //Creates the slack jobs
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

    //Creates the Notifications
    private List<DetailedNotificationContent> createNotificationWrappers() {
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(NotificationType.VULNERABILITY);
        DetailedNotificationContent test_project = DetailedNotificationContent.vulnerability(
            alertNotificationModel,
            createVulnerabilityUniqueProjectNotificationContent(PROJECT_NAME_1),
            PROJECT_NAME_1,
            List.of(VulnerabilitySeverityType.LOW.name())
        );

        //delete this:
        String projectName1 = "test_project1";
        DetailedNotificationContent test_project2 = DetailedNotificationContent.vulnerability(
            alertNotificationModel,
            createVulnerabilityUniqueProjectNotificationContent(projectName1),
            projectName1,
            List.of(VulnerabilitySeverityType.LOW.name(), VulnerabilitySeverityType.HIGH.name())
        );

        return List.of(test_project, test_project2);
        /*
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
         */
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
