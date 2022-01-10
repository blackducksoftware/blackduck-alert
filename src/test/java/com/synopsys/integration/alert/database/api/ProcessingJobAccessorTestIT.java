package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingJobAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;
import com.synopsys.integration.alert.database.job.DistributionJobRepository;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.provider.blackduck.processor.model.VulnerabilityUniqueProjectNotificationContent;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.blackduck.api.generated.enumeration.VulnerabilitySeverityType;
import com.synopsys.integration.blackduck.api.manual.component.AffectedProjectVersion;
import com.synopsys.integration.blackduck.api.manual.component.VulnerabilityNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@AlertIntegrationTest
public class ProcessingJobAccessorTestIT {
    private static final List<UUID> CREATED_JOBS = new LinkedList<>();
    private static final String PROJECT_NAME_1 = "testProject1";
    private static final String PROJECT_NAME_2 = "testProject2";
    private static final String PROJECT_VERSION_NAME_1 = "version";
    private static final String PROJECT_VERSION_NAME_2 = "1.1.0";
    private Long providerConfigId;

    @Autowired
    public JobAccessor jobAccessor;
    @Autowired
    public ProcessingJobAccessor processingJobAccessor;
    @Autowired
    public ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    @Autowired
    public DistributionJobRepository distributionJobRepository;

    @BeforeEach
    public void createProvider() {
        ConfigurationFieldModel providerConfigEnabled = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        providerConfigEnabled.setFieldValue("true");
        ConfigurationFieldModel providerConfigName = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        providerConfigName.setFieldValue("My Black Duck Config");

        ConfigurationFieldModel blackduckUrl = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        blackduckUrl.setFieldValue("https://a-blackduck-server");
        ConfigurationFieldModel blackduckApiKey = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        blackduckApiKey.setFieldValue("123456789012345678901234567890123456789012345678901234567890");
        ConfigurationFieldModel blackduckTimeout = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT);
        blackduckTimeout.setFieldValue("300");

        List<ConfigurationFieldModel> providerConfigFields = List.of(providerConfigEnabled, providerConfigName, blackduckUrl, blackduckApiKey, blackduckTimeout);
        providerConfigId = configurationModelConfigurationAccessor.createConfiguration(new BlackDuckProviderKey(), ConfigContextEnum.GLOBAL, providerConfigFields).getConfigurationId();
    }

    @AfterEach
    public void removeCreatedJobsIfExist() {
        CREATED_JOBS.forEach(jobAccessor::deleteJob);
        CREATED_JOBS.clear();
        configurationModelConfigurationAccessor.deleteConfiguration(providerConfigId);
    }

    @Test
    public void testMatchingEnabledJobsUniqueJobsPerPage() throws InterruptedException {
        int expectedNumOfJobs = 1000;
        Set<UUID> previousJobIdSet = new HashSet<>();
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 100));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name(), VulnerabilitySeverityType.HIGH.name()), 100));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 100));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 100));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 100));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 100));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 100));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 100));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 100));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 100));

        List<DetailedNotificationContent> notifications = new ArrayList<>();
        notifications.addAll(createVulnerabilityNotificationWrappers(List.of(VulnerabilitySeverityType.LOW.name()), PROJECT_NAME_1, PROJECT_VERSION_NAME_1));
        notifications.addAll(createVulnerabilityNotificationWrappers(List.of(VulnerabilitySeverityType.HIGH.name()), PROJECT_NAME_2, PROJECT_VERSION_NAME_2));

        int currentPage = 0;
        FilteredDistributionJobRequestModel filteredDistributionJobRequestModel = new FilteredDistributionJobRequestModel(providerConfigId, List.of(FrequencyType.REAL_TIME));
        filteredDistributionJobRequestModel.addProjectName(PROJECT_NAME_1);
        filteredDistributionJobRequestModel.addProjectName(PROJECT_NAME_2);
        filteredDistributionJobRequestModel.addNotificationType(NotificationType.VULNERABILITY.name());
        filteredDistributionJobRequestModel.addVulnerabilitySeverities(List.of(VulnerabilitySeverityType.LOW.name(), VulnerabilitySeverityType.HIGH.name()));
        AlertPagedDetails<FilteredDistributionJobResponseModel> jobs = processingJobAccessor.getMatchingEnabledJobsByFilteredNotifications(filteredDistributionJobRequestModel, currentPage, 100);
        while (currentPage < jobs.getTotalPages()) {
            currentPage++;
            for (FilteredDistributionJobResponseModel jobResponseModel : jobs.getModels()) {
                // this will update the job id such that last updated time will change.
                // need to make sure the subsequent page requests don't return duplicate jobs.
                UUID jobId = jobResponseModel.getId();
                // cannot find the same job id in subsequent page requests for jobs.
                assertFalse(previousJobIdSet.contains(jobId), String.format("Job id: %s found in set of previously mapped job ids.", jobId));
                previousJobIdSet.add(jobId);
            }
            jobs = processingJobAccessor.getMatchingEnabledJobsByFilteredNotifications(filteredDistributionJobRequestModel, currentPage, 100);
        }

        assertEquals(expectedNumOfJobs, previousJobIdSet.size());
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

    private VulnerabilityUniqueProjectNotificationContent createVulnerabilityUniqueProjectNotificationContent(String projectName) {
        AffectedProjectVersion affectedProjectVersion = new AffectedProjectVersion();
        affectedProjectVersion.setProjectName(projectName);
        return new VulnerabilityUniqueProjectNotificationContent(new VulnerabilityNotificationContent(), affectedProjectVersion);
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
            providerConfigId,
            projectNames != null && !projectNames.isEmpty(),
            null,
            null,
            notificationTypes,
            blackDuckProjectDetailsModels,
            policies,
            vulns,
            new SlackJobDetailsModel(null, "webhook", "channelName", "username")
        );
    }
}
