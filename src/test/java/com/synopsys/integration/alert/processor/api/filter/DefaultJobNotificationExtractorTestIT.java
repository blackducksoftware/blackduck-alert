package com.synopsys.integration.alert.processor.api.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
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

    @Autowired
    public JobAccessor jobAccessor;

    @Test
    public void extractJobTest() {
        createDistributionJobModels()
            .stream()
            .forEach(jobAccessor::createJob);

        DefaultJobNotificationExtractor defaultJobNotificationExtractor = new DefaultJobNotificationExtractor(jobAccessor);
        Map<FilteredDistributionJobResponseModel, List<FilterableNotificationWrapper<?>>> jobResponseModelListMap = defaultJobNotificationExtractor.mapJobsToNotifications(createNotificationWrappers(), null);

        assertNotNull(jobResponseModelListMap);
        assertEquals(3, jobResponseModelListMap.size());
        for (List<FilterableNotificationWrapper<?>> jobNotificationList : jobResponseModelListMap.values()) {
            assertTrue(!jobNotificationList.isEmpty());
            assertTrue(jobNotificationList.size() < 4);
        }
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
            new SlackJobDetailsModel("webhook", "channelName", "username")
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
            List.of("policyName")
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
