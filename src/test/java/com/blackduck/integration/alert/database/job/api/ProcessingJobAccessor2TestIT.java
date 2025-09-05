/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.processor.mapping.JobNotificationMapper2;
import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.JobAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.JobNotificationMappingAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.ProcessingJobAccessor2;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.blackduck.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.blackduck.integration.alert.common.persistence.model.job.JobToNotificationMappingModel;
import com.blackduck.integration.alert.common.persistence.model.job.SimpleFilteredDistributionJobResponseModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.rest.model.AlertPagedDetails;
import com.blackduck.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.database.job.DistributionJobRepository;
import com.blackduck.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.blackduck.integration.alert.util.AlertIntegrationTest;
import com.blackduck.integration.blackduck.api.generated.enumeration.VulnerabilitySeverityType;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;

@AlertIntegrationTest
class ProcessingJobAccessor2TestIT {
    private static final List<UUID> CREATED_JOBS = new LinkedList<>();
    private static final Set<Long> CREATED_NOTIFICATIONS = new LinkedHashSet<>();
    private static final String PROJECT_NAME_1 = "testProject1";
    private static final String PROJECT_NAME_2 = "testProject2";
    private static final String PROJECT_VERSION_NAME_1 = "version";
    private static final String PROJECT_VERSION_NAME_2 = "1.1.0";
    private static final UUID CORRELATION_ID = UUID.randomUUID();
    private static final AtomicLong CURRENT_NOTIFICATION_ID = new AtomicLong(0L);
    public static final String PROVIDER_CONFIG_NAME = "My Black Duck Config";
    private Long providerConfigId;

    @Autowired
    public JobAccessor jobAccessor;
    @Autowired
    public NotificationAccessor notificationAccessor;
    @Autowired
    public ProcessingJobAccessor2 processingJobAccessor;
    @Autowired
    public ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    @Autowired
    public DistributionJobRepository distributionJobRepository;
    @Autowired
    public JobNotificationMappingAccessor jobNotificationMappingAccessor;
    @Autowired
    public JobNotificationMapper2 jobNotificationMapper;
    @Autowired
    public EncryptionUtility encryptionUtility;

    @BeforeEach
    public void createProvider() {
        ConfigurationFieldModel providerConfigEnabled = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        providerConfigEnabled.setFieldValue("true");
        ConfigurationFieldModel providerConfigName = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        providerConfigName.setFieldValue(PROVIDER_CONFIG_NAME);

        ConfigurationFieldModel blackduckUrl = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        blackduckUrl.setFieldValue("https://a-blackduck-server");
        ConfigurationFieldModel blackduckApiKey = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        blackduckApiKey.setFieldValue(encryptionUtility.encrypt("123456789012345678901234567890123456789012345678901234567890"));
        ConfigurationFieldModel blackduckTimeout = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT);
        blackduckTimeout.setFieldValue("300");

        List<ConfigurationFieldModel> providerConfigFields = List.of(providerConfigEnabled, providerConfigName, blackduckUrl, blackduckApiKey, blackduckTimeout);
        providerConfigId = configurationModelConfigurationAccessor.createConfiguration(new BlackDuckProviderKey(), ConfigContextEnum.GLOBAL, providerConfigFields)
            .getConfigurationId();
    }

    @AfterEach
    public void removeCreatedJobsIfExist() {
        for (UUID jobId : CREATED_JOBS) {
            jobNotificationMappingAccessor.removeJobMapping(CORRELATION_ID, jobId);
            jobAccessor.deleteJob(jobId);
        }

        for (Long notificationId : CREATED_NOTIFICATIONS) {
            Optional<AlertNotificationModel> notification = notificationAccessor.findById(notificationId);
            notification.ifPresent(notificationAccessor::deleteNotification);
        }
        CREATED_NOTIFICATIONS.clear();
        CREATED_JOBS.clear();
        configurationModelConfigurationAccessor.deleteConfiguration(providerConfigId);
    }

    @Test
    void testMatchingEnabledJobsUniqueJobsPerPage() throws InterruptedException {
        int expectedNumOfJobs = 1000;
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

        List<AlertNotificationModel> notifications = new ArrayList<>();
        notifications.add(createAlertNotificationModel(NotificationType.VULNERABILITY));
        notifications.add(createAlertNotificationModel(NotificationType.VULNERABILITY));

        notifications = saveNotifications(notifications);

        Consumer<FilteredDistributionJobRequestModel> configureRequest = (request) -> {
            request.addProjectName(PROJECT_NAME_1);
            request.addProjectName(PROJECT_NAME_2);
            request.addNotificationType(NotificationType.VULNERABILITY.name());
            request.addVulnerabilitySeverities(List.of(VulnerabilitySeverityType.LOW.name(), VulnerabilitySeverityType.HIGH.name()));
        };

        Collection<FilteredDistributionJobRequestModel> requests = createMappingRequests(expectedNumOfJobs, CORRELATION_ID, notifications, configureRequest);
        createJobMappings(CORRELATION_ID, requests);
        Set<UUID> previousJobIdSet = processJobMappings(requests);
        assertEquals(expectedNumOfJobs, previousJobIdSet.size());
        assertTrue(jobNotificationMappingAccessor.hasJobMappings(CORRELATION_ID));
    }

    @Test
    void testMatchingEnabledJobsVulnerabilitiesSeverityMissing() throws InterruptedException {
        int expectedNumOfJobs = 1000;
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

        List<AlertNotificationModel> notifications = new ArrayList<>();
        notifications.add(createAlertNotificationModel(NotificationType.VULNERABILITY));
        notifications.add(createAlertNotificationModel(NotificationType.VULNERABILITY));

        notifications = saveNotifications(notifications);

        Consumer<FilteredDistributionJobRequestModel> configureRequest = (request) -> {
            request.addProjectName(PROJECT_NAME_1);
            request.addProjectName(PROJECT_NAME_2);
            request.addNotificationType(NotificationType.VULNERABILITY.name());
        };

        Collection<FilteredDistributionJobRequestModel> requests = createMappingRequests(expectedNumOfJobs, CORRELATION_ID, notifications, configureRequest);
        createJobMappings(CORRELATION_ID, requests);
        Set<UUID> previousJobIdSet = processJobMappings(requests);
        assertEquals(expectedNumOfJobs, previousJobIdSet.size());
        assertTrue(jobNotificationMappingAccessor.hasJobMappings(CORRELATION_ID));
    }

    @Test
    void testMatchingEnabledJobsMultipleCorrelationIds() throws InterruptedException {
        int expectedNumOfJobs = 100;
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 10));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name(), VulnerabilitySeverityType.HIGH.name()), 10));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 10));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 10));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 10));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 10));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 10));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 10));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 10));
        createJobs(createDistributionJobModels(List.of(VulnerabilitySeverityType.LOW.name()), 10));

        List<AlertNotificationModel> notifications = new ArrayList<>();
        notifications.add(createAlertNotificationModel(NotificationType.VULNERABILITY));
        notifications.add(createAlertNotificationModel(NotificationType.VULNERABILITY));

        notifications = saveNotifications(notifications);

        Consumer<FilteredDistributionJobRequestModel> configureRequest = (request) -> {
            request.addProjectName(PROJECT_NAME_1);
            request.addProjectName(PROJECT_NAME_2);
            request.addNotificationType(NotificationType.VULNERABILITY.name());
        };

        Collection<FilteredDistributionJobRequestModel> requests = createMappingRequests(expectedNumOfJobs, CORRELATION_ID, notifications, configureRequest);
        createJobMappings(CORRELATION_ID, requests);
        UUID nonMappedCorrelationId = UUID.randomUUID();
        requests.addAll(createMappingRequests(expectedNumOfJobs, nonMappedCorrelationId, notifications, configureRequest));

        Set<UUID> previousJobIdSet = processJobMappings(requests);
        assertEquals(0, jobNotificationMappingAccessor.getUniqueJobIds(nonMappedCorrelationId).size());
        assertFalse(jobNotificationMappingAccessor.hasJobMappings(nonMappedCorrelationId));
        assertEquals(expectedNumOfJobs, jobNotificationMappingAccessor.getUniqueJobIds(CORRELATION_ID).size());
        assertTrue(jobNotificationMappingAccessor.hasJobMappings(CORRELATION_ID));
        assertEquals(expectedNumOfJobs, previousJobIdSet.size());
    }

    private Collection<FilteredDistributionJobRequestModel> createMappingRequests(
        int expectedNumOfJobs,
        UUID correlationId,
        List<AlertNotificationModel> notifications,
        Consumer<FilteredDistributionJobRequestModel> requestConfigurer
    ) {
        List<FilteredDistributionJobRequestModel> requests = new ArrayList<>();
        for (AlertNotificationModel notification : notifications) {
            FilteredDistributionJobRequestModel filteredDistributionJobRequestModel = new FilteredDistributionJobRequestModel(
                providerConfigId,
                notification.getId(),
                List.of(FrequencyType.REAL_TIME)
            );
            requestConfigurer.accept(filteredDistributionJobRequestModel);
            requests.add(filteredDistributionJobRequestModel);
        }
        return requests;
    }

    private void createJobMappings(UUID correlationId, Collection<FilteredDistributionJobRequestModel> requests) {
        List<JobToNotificationMappingModel> jobMappings = new ArrayList<>(CREATED_JOBS.size());
        for (UUID jobId : CREATED_JOBS) {
            for (FilteredDistributionJobRequestModel requestModel : requests) {
                jobMappings.add(new JobToNotificationMappingModel(correlationId, jobId, requestModel.getNotificationId().orElse(null)));
            }
        }
        jobNotificationMappingAccessor.addJobMappings(jobMappings);
    }

    private Set<UUID> processJobMappings(Collection<FilteredDistributionJobRequestModel> requests) {
        Set<UUID> previousJobIdSet = new HashSet<>();
        int currentPage = 0;
        for (FilteredDistributionJobRequestModel filteredDistributionJobRequestModel : requests) {
            AlertPagedDetails<SimpleFilteredDistributionJobResponseModel> jobs = processingJobAccessor.getMatchingEnabledJobsForNotifications(
                filteredDistributionJobRequestModel,
                currentPage,
                100
            );
            while (currentPage < jobs.getTotalPages()) {
                currentPage++;
                for (SimpleFilteredDistributionJobResponseModel jobResponseModel : jobs.getModels()) {
                    // this will update the job id such that last updated time will change.
                    // need to make sure the subsequent page requests don't return duplicate jobs.
                    UUID jobId = jobResponseModel.getJobId();
                    // cannot find the same job id in subsequent page requests for jobs.
                    assertFalse(previousJobIdSet.contains(jobId), String.format("Job id: %s found in set of previously mapped job ids.", jobId));
                    previousJobIdSet.add(jobId);
                }
                jobs = processingJobAccessor.getMatchingEnabledJobsForNotifications(filteredDistributionJobRequestModel, currentPage, 100);
            }
        }
        return previousJobIdSet;
    }

    private List<AlertNotificationModel> saveNotifications(List<AlertNotificationModel> notifications) {
        List<AlertNotificationModel> savedNotifications = notificationAccessor.saveAllNotifications(notifications);
        savedNotifications.stream()
            .map(AlertNotificationModel::getId)
            .forEach(CREATED_NOTIFICATIONS::add);
        return savedNotifications;

    }

    private AlertNotificationModel createAlertNotificationModel(NotificationType notificationType) {
        return new AlertNotificationModel(
            CURRENT_NOTIFICATION_ID.incrementAndGet(),
            providerConfigId,
            "provider",
            PROVIDER_CONFIG_NAME,
            notificationType.name(),
            "content",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            false,
            String.format("content-id-%s", UUID.randomUUID()),
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
            UUID.randomUUID(),
            providerConfigId,
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
}
