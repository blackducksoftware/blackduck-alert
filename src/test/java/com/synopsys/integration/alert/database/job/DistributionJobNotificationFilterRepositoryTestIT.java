package com.synopsys.integration.alert.database.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.MsTeamsKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

@AlertIntegrationTest
public class DistributionJobNotificationFilterRepositoryTestIT {
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();
    private static final MsTeamsKey MS_TEAMS_KEY = new MsTeamsKey();

    @Autowired
    private DistributionJobNotificationFilterRepository jobNotificationFilterRepository;
    @Autowired
    private DistributionJobRepository distributionJobRepository;
    @Autowired
    private JobAccessor jobAccessor;
    @Autowired
    private ConfigurationAccessor configurationAccessor;

    private Long blackDuckConfigId = null;

    @BeforeEach
    public void init() {
        ConfigurationModel blackDuckConfig = createBlackDuckConfig();
        blackDuckConfigId = blackDuckConfig.getConfigurationId();
    }

    @AfterEach
    public void cleanup() {
        distributionJobRepository.deleteAll();
        if (null != blackDuckConfigId) {
            configurationAccessor.deleteConfiguration(blackDuckConfigId);
            blackDuckConfigId = null;
        }
    }

    @Test
    public void findAndSortEnabledJobsMatchingFilters_ValidateSyntaxTest() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        Page<DistributionJobEntity> foundJobs = jobNotificationFilterRepository.findAndSortEnabledJobsMatchingFilters(blackDuckConfigId, Set.of(), Set.of(), Set.of(), Set.of(), Set.of(), pageRequest);
        assertEquals(0, foundJobs.getContent().size());
    }

    @Test
    public void findAndSortEnabledJobsMatchingFilters_ValidateFilteringTest() {
        int page = 0;

        String testProjectName1 = "integration-common";
        BlackDuckProjectDetailsModel project1Details = new BlackDuckProjectDetailsModel(testProjectName1, "https://project-1");
        String testProjectName2 = "int-jira-common";
        BlackDuckProjectDetailsModel project2Details = new BlackDuckProjectDetailsModel(testProjectName2, "https://project-2");

        DistributionJobModel job1 = initJob("Job 1", false, null, List.of(project1Details, project2Details));
        DistributionJobModel job2 = initJob("Job 2", true, "int[A-Za-z0-9\\-]{0,}-common", List.of());

        Set<String> frequencyFilterSet = Set.of(job1.getDistributionFrequency().name());
        Set<String> notificationTypesFilterSet = new HashSet<>(job1.getNotificationTypes());
        Set<String> projectFilter = Set.of(testProjectName1, testProjectName2);

        PageRequest firstPageRequest = PageRequest.of(page, 1);
        Page<DistributionJobEntity> firstPageOfJobs = jobNotificationFilterRepository.findAndSortEnabledJobsMatchingFilters(blackDuckConfigId, frequencyFilterSet, notificationTypesFilterSet, projectFilter, Set.of(), Set.of(),
            firstPageRequest);
        assertEquals(1, firstPageOfJobs.getContent().size());
        DistributionJobEntity firstJob = firstPageOfJobs.stream().findFirst().orElseThrow();
        assertEquals(job1.getName(), firstJob.getName());

        PageRequest secondPageRequest = PageRequest.of(++page, 1);
        Page<DistributionJobEntity> secondPageOfJobs = jobNotificationFilterRepository.findAndSortEnabledJobsMatchingFilters(blackDuckConfigId, frequencyFilterSet, notificationTypesFilterSet, projectFilter, Set.of(), Set.of(),
            secondPageRequest);
        assertEquals(1, secondPageOfJobs.getContent().size());
        DistributionJobEntity secondJob = secondPageOfJobs.stream().findFirst().orElseThrow();
        assertEquals(job2.getName(), secondJob.getName());
    }

    private ConfigurationModel createBlackDuckConfig() {
        return configurationAccessor.createConfiguration(BLACK_DUCK_PROVIDER_KEY, ConfigContextEnum.GLOBAL, Set.of());
    }

    private DistributionJobModel initJob(String name, boolean filterByProject, String projectNamePattern, List<BlackDuckProjectDetailsModel> projectFilterDetails) {
        DistributionJobRequestModel requestModel = new DistributionJobRequestModel(
            true,
            name,
            FrequencyType.REAL_TIME,
            ProcessingType.SUMMARY,
            MS_TEAMS_KEY.getUniversalKey(),
            blackDuckConfigId,
            filterByProject,
            projectNamePattern,
            List.of(NotificationType.VULNERABILITY.name()),
            projectFilterDetails,
            List.of(),
            List.of(),
            new MSTeamsJobDetailsModel(null, "https://webhook")
        );
        return jobAccessor.createJob(requestModel);
    }

}
