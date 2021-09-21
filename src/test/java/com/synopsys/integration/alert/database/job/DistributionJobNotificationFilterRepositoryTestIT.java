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

    private static final String PROJECT_MATCHING_PATTERN_1 = "integration-common";
    private static final String PROJECT_MATCHING_PATTERN_2 = "int-jira-common";
    private static final String PROJECT_MATCHING_PATTERN_3 = "int-azureboards-common";
    private static final String PROJECT_NOT_MATCHING_PATTERN = "_0123456789 x non matching project abc";
    private static final String PATTERN_FOR_TEST_PROJECT_NAMES = "int[A-Za-z0-9\\-]{0,}-common";

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

        BlackDuckProjectDetailsModel project1Details = new BlackDuckProjectDetailsModel(PROJECT_MATCHING_PATTERN_1, "https://project-1");
        BlackDuckProjectDetailsModel project2Details = new BlackDuckProjectDetailsModel(PROJECT_MATCHING_PATTERN_2, "https://project-2");

        DistributionJobModel job1 = initJob("Job 1", true, null, List.of(project1Details, project2Details));
        DistributionJobModel job2 = initJob("Job 2", true, PATTERN_FOR_TEST_PROJECT_NAMES, List.of());

        Set<String> frequencyFilter = Set.of(job1.getDistributionFrequency().name());
        Set<String> notificationTypesFilter = new HashSet<>(job1.getNotificationTypes());
        Set<String> projectFilter = Set.of(PROJECT_MATCHING_PATTERN_1, PROJECT_MATCHING_PATTERN_2);

        PageRequest firstPageRequest = PageRequest.of(page, 1);
        Page<DistributionJobEntity> firstPageOfJobs = jobNotificationFilterRepository.findAndSortEnabledJobsMatchingFilters(blackDuckConfigId, frequencyFilter, notificationTypesFilter, projectFilter, Set.of(), Set.of(), firstPageRequest);
        assertFilteredPage(firstPageOfJobs, job1);

        PageRequest secondPageRequest = PageRequest.of(++page, 1);
        Page<DistributionJobEntity> secondPageOfJobs = jobNotificationFilterRepository.findAndSortEnabledJobsMatchingFilters(blackDuckConfigId, frequencyFilter, notificationTypesFilter, projectFilter, Set.of(), Set.of(), secondPageRequest);
        assertFilteredPage(secondPageOfJobs, job2);
    }

    @Test
    public void findAndSortEnabledJobsMatchingFilters_ValidateProjectNameParamManipulationTest() {
        BlackDuckProjectDetailsModel project1Details = new BlackDuckProjectDetailsModel(PROJECT_MATCHING_PATTERN_1, "https://project-1");
        BlackDuckProjectDetailsModel project2Details = new BlackDuckProjectDetailsModel(PROJECT_MATCHING_PATTERN_2, "https://project-2");
        BlackDuckProjectDetailsModel project3Details = new BlackDuckProjectDetailsModel(PROJECT_NOT_MATCHING_PATTERN, "https://project-3");

        DistributionJobModel job1 = initJob("Job 1", false, null, List.of());
        DistributionJobModel job2 = initJob("Job 2", true, null, List.of(project1Details, project2Details));
        DistributionJobModel job3 = initJob("Job 3", true, PATTERN_FOR_TEST_PROJECT_NAMES, List.of());
        DistributionJobModel job4 = initJob("Job 4", true, null, List.of(project3Details));

        Set<String> frequencyFilter = Set.of(job1.getDistributionFrequency().name());
        Set<String> notifTypesFilter = new HashSet<>(job1.getNotificationTypes());

        PageRequest pageRequest = PageRequest.of(0, 10);

        // Should match Job 1 (no filter-by-project)
        Set<String> noProjectsFilter = Set.of();
        Page<DistributionJobEntity> noProjectsPage = jobNotificationFilterRepository.findAndSortEnabledJobsMatchingFilters(blackDuckConfigId, frequencyFilter, notifTypesFilter, noProjectsFilter, Set.of(), Set.of(), pageRequest);
        assertFilteredPage(noProjectsPage, job1);

        // Should match Job 3 (pattern)
        Set<String> oneProjectFilter = Set.of(PROJECT_MATCHING_PATTERN_3);
        Page<DistributionJobEntity> oneProjectPage = jobNotificationFilterRepository.findAndSortEnabledJobsMatchingFilters(blackDuckConfigId, frequencyFilter, notifTypesFilter, oneProjectFilter, Set.of(), Set.of(), pageRequest);
        assertFilteredPage(oneProjectPage, job3);

        // Should match Job 2 (exact), Job 3 (pattern), and Job 4 (exact)
        Set<String> twoProjectsFilter = Set.of(PROJECT_MATCHING_PATTERN_1, PROJECT_NOT_MATCHING_PATTERN);
        Page<DistributionJobEntity> twoProjectsPage = jobNotificationFilterRepository.findAndSortEnabledJobsMatchingFilters(blackDuckConfigId, frequencyFilter, notifTypesFilter, twoProjectsFilter, Set.of(), Set.of(), pageRequest);
        assertEquals(3, twoProjectsPage.getContent().size());

        // Should match Job 4 (exact)
        Set<String> nonMatchingProjectFilter = Set.of(PROJECT_NOT_MATCHING_PATTERN);
        Page<DistributionJobEntity> nonMatchingProjectsPage =
            jobNotificationFilterRepository.findAndSortEnabledJobsMatchingFilters(blackDuckConfigId, frequencyFilter, notifTypesFilter, nonMatchingProjectFilter, Set.of(), Set.of(), pageRequest);
        assertFilteredPage(nonMatchingProjectsPage, job4);
    }

    private static void assertFilteredPage(Page<DistributionJobEntity> page, DistributionJobModel expectedJob) {
        assertEquals(1, page.getContent().size());
        DistributionJobEntity job = page.stream().findFirst().orElseThrow();
        assertEquals(expectedJob.getName(), job.getName());
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
