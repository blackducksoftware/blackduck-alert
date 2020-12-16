package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessorV2;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.job.DistributionJobEntity;
import com.synopsys.integration.alert.database.job.DistributionJobRepository;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsEntity;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

class StaticJobAccessorV2Test {
    private DistributionJobRepository distributionJobRepository;
    private BlackDuckJobDetailsAccessor blackDuckJobDetailsAccessor;
    private JobAccessorV2 jobAccessor;

    private final String jobName = "jobName";

    @BeforeEach
    public void init() {
        distributionJobRepository = Mockito.mock(DistributionJobRepository.class);
        blackDuckJobDetailsAccessor = Mockito.mock(BlackDuckJobDetailsAccessor.class);

        Mockito.when(blackDuckJobDetailsAccessor.retrieveNotificationTypesForJob(Mockito.any())).thenReturn(Collections.emptyList());
        Mockito.when(blackDuckJobDetailsAccessor.retrieveProjectDetailsForJob(Mockito.any())).thenReturn(Collections.emptyList());
        Mockito.when(blackDuckJobDetailsAccessor.retrievePolicyNamesForJob(Mockito.any())).thenReturn(Collections.emptyList());
        Mockito.when(blackDuckJobDetailsAccessor.retrieveVulnerabilitySeverityNamesForJob(Mockito.any())).thenReturn(Collections.emptyList());
        jobAccessor = new StaticJobAccessorV2(distributionJobRepository, blackDuckJobDetailsAccessor, null, null, null, null, null, null, new BlackDuckProviderKey());
    }

    @Test
    void getJobByIdTest() {
        UUID jobId = UUID.randomUUID();

        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(jobId, jobName, true, null, null, null, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

        Mockito.when(distributionJobRepository.findById(Mockito.any())).thenReturn(Optional.of(distributionJobEntity));
        Optional<DistributionJobModel> jobById = jobAccessor.getJobById(jobId);

        assertTrue(jobById.isPresent());
        DistributionJobModel distributionJobModel = jobById.get();
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void getJobByNameTest() {
        UUID jobId = UUID.randomUUID();
        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(jobId, jobName, true, null, null, null, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

        Mockito.when(distributionJobRepository.findByName(Mockito.any())).thenReturn(Optional.of(distributionJobEntity));
        Optional<DistributionJobModel> jobByName = jobAccessor.getJobByName(jobName);

        assertTrue(jobByName.isPresent());
        DistributionJobModel distributionJobModel = jobByName.get();
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void createJobTest() {
        UUID jobId = UUID.randomUUID();
        DistributionJobDetailsModel distributionJobDetailsModel = new DistributionJobDetailsModel(new ChannelKey("test", "channel") {}) {};
        DistributionJobRequestModel distributionJobRequestModel = new DistributionJobRequestModel(
            true,
            jobName,
            FrequencyType.DAILY,
            ProcessingType.DEFAULT,
            "channel.common.name",
            3L,
            true,
            "*",
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            distributionJobDetailsModel
        );

        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(
            jobId,
            distributionJobRequestModel.getName(),
            distributionJobRequestModel.isEnabled(),
            distributionJobRequestModel.getDistributionFrequency().name(),
            distributionJobRequestModel.getProcessingType().name(),
            distributionJobRequestModel.getChannelDescriptorName(),
            DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp()
        );
        BlackDuckJobDetailsEntity blackDuckJobDetailsEntity = new BlackDuckJobDetailsEntity(
            jobId,
            distributionJobRequestModel.getBlackDuckGlobalConfigId(),
            distributionJobRequestModel.isFilterByProject(),
            distributionJobRequestModel.getProjectNamePattern().orElse(null)
        );

        Mockito.when(blackDuckJobDetailsAccessor.saveBlackDuckJobDetails(Mockito.any(), Mockito.any())).thenReturn(blackDuckJobDetailsEntity);
        Mockito.when(distributionJobRepository.save(Mockito.any())).thenReturn(distributionJobEntity);

        DistributionJobModel createdJob = jobAccessor.createJob(distributionJobRequestModel);

        assertEquals(jobId, createdJob.getJobId());
        assertEquals(jobName, createdJob.getName());
    }

    @Test
    void deleteJobTest() {
        UUID jobId = UUID.randomUUID();
        jobAccessor.deleteJob(jobId);
        Mockito.verify(distributionJobRepository).deleteById(Mockito.any());
    }

    @Test
    void updateJobTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        DistributionJobDetailsModel distributionJobDetailsModel = new DistributionJobDetailsModel(new ChannelKey("test", "channel") {}) {};
        DistributionJobRequestModel distributionJobRequestModel = new DistributionJobRequestModel(
            true,
            jobName,
            FrequencyType.DAILY,
            ProcessingType.DEFAULT,
            "channel.common.name",
            3L,
            true,
            "*",
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            distributionJobDetailsModel
        );

        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(
            jobId,
            distributionJobRequestModel.getName(),
            distributionJobRequestModel.isEnabled(),
            distributionJobRequestModel.getDistributionFrequency().name(),
            distributionJobRequestModel.getProcessingType().name(),
            distributionJobRequestModel.getChannelDescriptorName(),
            DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp()
        );
        BlackDuckJobDetailsEntity blackDuckJobDetailsEntity = new BlackDuckJobDetailsEntity(
            jobId,
            distributionJobRequestModel.getBlackDuckGlobalConfigId(),
            distributionJobRequestModel.isFilterByProject(),
            distributionJobRequestModel.getProjectNamePattern().orElse(null)
        );

        Mockito.when(blackDuckJobDetailsAccessor.saveBlackDuckJobDetails(Mockito.any(), Mockito.any())).thenReturn(blackDuckJobDetailsEntity);
        Mockito.when(distributionJobRepository.findById(Mockito.any())).thenReturn(Optional.of(distributionJobEntity));
        Mockito.when(distributionJobRepository.save(Mockito.any())).thenReturn(distributionJobEntity);

        DistributionJobModel updatedJob = jobAccessor.updateJob(jobId, distributionJobRequestModel);

        Mockito.verify(distributionJobRepository).deleteById(jobId);

        assertEquals(jobId, updatedJob.getJobId());
        assertEquals(jobName, updatedJob.getName());
    }

    @Test
    void getJobsByIdTest() {
        UUID jobId = UUID.randomUUID();

        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(jobId, jobName, true, null, null, null, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

        Mockito.when(distributionJobRepository.findAllById(Mockito.any())).thenReturn(List.of(distributionJobEntity));

        List<DistributionJobModel> jobsById = jobAccessor.getJobsById(List.of(jobId));

        assertEquals(1, jobsById.size());
        DistributionJobModel distributionJobModel = jobsById.get(0);
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void getPageOfJobsTest() {
        UUID jobId = UUID.randomUUID();

        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(jobId, jobName, true, null, null, null, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

        Page<DistributionJobEntity> page = new PageImpl<>(List.of(distributionJobEntity));
        Mockito.when(distributionJobRepository.findAll(Mockito.any(PageRequest.class))).thenReturn(page);

        AlertPagedModel<DistributionJobModel> pageOfJobs = jobAccessor.getPageOfJobs(0, 10);

        assertEquals(1, pageOfJobs.getTotalPages());
        List<DistributionJobModel> models = pageOfJobs.getModels();
        assertEquals(1, models.size());
        DistributionJobModel distributionJobModel = models.get(0);
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void getPageOfJobsSearchTest() {
        ProviderKey providerKey = new BlackDuckProviderKey();
        UUID jobId = UUID.randomUUID();

        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(jobId, jobName, true, null, null, providerKey.getUniversalKey(), DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

        Page<DistributionJobEntity> page = new PageImpl<>(List.of(distributionJobEntity));
        Mockito.when(distributionJobRepository.findByChannelDescriptorNamesAndSearchTerm(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(page);

        AlertPagedModel<DistributionJobModel> pageOfJobs = jobAccessor.getPageOfJobs(0, 10, "test-search-term", List.of(providerKey.getUniversalKey()));

        assertEquals(1, pageOfJobs.getTotalPages());
        List<DistributionJobModel> models = pageOfJobs.getModels();
        assertEquals(1, models.size());
        DistributionJobModel distributionJobModel = models.get(0);
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void getPageOfJobsBlankSearchTest() {
        ProviderKey providerKey = new BlackDuckProviderKey();
        UUID jobId = UUID.randomUUID();

        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(jobId, jobName, true, null, null, providerKey.getUniversalKey(), DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

        Page<DistributionJobEntity> page = new PageImpl<>(List.of(distributionJobEntity));
        Mockito.when(distributionJobRepository.findByChannelDescriptorNameIn(Mockito.any(), Mockito.any())).thenReturn(page);

        AlertPagedModel<DistributionJobModel> pageOfJobs = jobAccessor.getPageOfJobs(0, 10, " ", List.of(providerKey.getUniversalKey()));

        assertEquals(1, pageOfJobs.getTotalPages());
        List<DistributionJobModel> models = pageOfJobs.getModels();
        assertEquals(1, models.size());
        DistributionJobModel distributionJobModel = models.get(0);
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void getPageOfJobsExcludedDescriptorTest() {
        AlertPagedModel<DistributionJobModel> pageOfJobs = jobAccessor.getPageOfJobs(0, 10, null, List.of("invalid-descriptor-key"));

        assertEquals(0, pageOfJobs.getTotalPages());
        assertEquals(0, pageOfJobs.getCurrentPage());
        assertEquals(10, pageOfJobs.getPageSize());
        assertEquals(0, pageOfJobs.getModels().size());
    }

    @Test
    void getMatchingEnabledJobsTest() {
        UUID jobId = UUID.randomUUID();
        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(jobId, jobName, true, null, null, null, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

        Mockito.when(distributionJobRepository.findMatchingEnabledJob(Mockito.any(), Mockito.any())).thenReturn(List.of(distributionJobEntity));

        List<DistributionJobModel> matchingEnabledJobs = jobAccessor.getMatchingEnabledJobs(3L, NotificationType.BOM_EDIT);

        assertEquals(1, matchingEnabledJobs.size());
        DistributionJobModel distributionJobModel = matchingEnabledJobs.get(0);
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    @Test
    void getMatchingEnabledJobsWithFrequencyTest() {
        UUID jobId = UUID.randomUUID();
        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(jobId, jobName, true, null, null, null, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));
        
        Mockito.when(distributionJobRepository.findMatchingEnabledJob(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(List.of(distributionJobEntity));

        List<DistributionJobModel> matchingEnabledJobs = jobAccessor.getMatchingEnabledJobs(FrequencyType.DAILY, 3L, NotificationType.BOM_EDIT);

        assertEquals(1, matchingEnabledJobs.size());
        DistributionJobModel distributionJobModel = matchingEnabledJobs.get(0);
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

}
