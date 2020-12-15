package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessorV2;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.job.DistributionJobEntity;
import com.synopsys.integration.alert.database.job.DistributionJobRepository;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsAccessor;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsEntity;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

class StaticJobAccessorV2Test {
    private DistributionJobRepository distributionJobRepository;
    private BlackDuckJobDetailsAccessor blackDuckJobDetailsAccessor;

    private final String jobName = "jobName";

    @BeforeEach
    public void init() {
        distributionJobRepository = Mockito.mock(DistributionJobRepository.class);
        blackDuckJobDetailsAccessor = Mockito.mock(BlackDuckJobDetailsAccessor.class);
    }

    @Test
    void getJobByIdTest() {
        UUID jobId = UUID.randomUUID();

        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(jobId, jobName, true, null, null, null, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

        Mockito.when(distributionJobRepository.findById(Mockito.any())).thenReturn(Optional.of(distributionJobEntity));
        JobAccessorV2 jobAccessor = createJobAccessor();
        Optional<DistributionJobModel> jobById = jobAccessor.getJobById(jobId);

        assertTrue(jobById.isPresent());
        DistributionJobModel distributionJobModel = jobById.get();
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    // TODO: We may not need null ID tests
    @Test
    void getJobByIdNullTest() {
        JobAccessorV2 jobAccessor = createJobAccessor();
        Optional<DistributionJobModel> jobById = jobAccessor.getJobById(null);
        assertTrue(jobById.isEmpty(), "Expected no job with a null id to be found");
    }

    @Test
    void getJobByNameTest() {
        UUID jobId = UUID.randomUUID();
        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(jobId, jobName, true, null, null, null, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());
        distributionJobEntity.setBlackDuckJobDetails(new BlackDuckJobDetailsEntity(jobId, 3L, true, "*"));

        JobAccessorV2 jobAccessor = createJobAccessor();
        Mockito.when(distributionJobRepository.findByName(Mockito.any())).thenReturn(Optional.of(distributionJobEntity));
        Optional<DistributionJobModel> jobByName = jobAccessor.getJobByName(jobName);

        assertTrue(jobByName.isPresent());
        DistributionJobModel distributionJobModel = jobByName.get();
        assertEquals(jobId, distributionJobModel.getJobId());
        assertEquals(jobName, distributionJobModel.getName());
    }

    // TODO: We may not need null ID tests
    @Test
    void getJobByNameNullTest() {
        JobAccessorV2 jobAccessor = createJobAccessor();
        Optional<DistributionJobModel> jobByName = jobAccessor.getJobByName(null);
        assertTrue(jobByName.isEmpty(), "Expected no job with a null id to be found");
    }

    @Test
    void createJobTest() throws Exception {
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

        JobAccessorV2 jobAccessor = createJobAccessor();
        Mockito.when(distributionJobRepository.save(Mockito.any())).thenReturn(distributionJobEntity);

        DistributionJobModel createdJob = jobAccessor.createJob(distributionJobRequestModel);

        assertEquals(jobId, createdJob.getJobId());
        assertEquals(jobName, createdJob.getName());
    }

    @Test
    void deleteJobTest() throws AlertConfigurationException {
        JobAccessorV2 jobAccessor = createJobAccessor();
        UUID jobId = UUID.randomUUID();
        jobAccessor.deleteJob(jobId);
        Mockito.verify(distributionJobRepository).deleteById(Mockito.any());
    }

    // TODO: We may not need null ID tests
    @Test
    void deleteJobNullIdTest() {
        JobAccessorV2 jobAccessor = createJobAccessor();
        try {
            jobAccessor.deleteJob(null);
            fail("Null jobId did not throw expected AlertConfigurationException.");
        } catch (AlertConfigurationException e) {
            assertNotNull(e);
        }
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

        JobAccessorV2 jobAccessor = createJobAccessor();
        DistributionJobModel updatedJob = jobAccessor.updateJob(jobId, distributionJobRequestModel);

        Mockito.verify(distributionJobRepository).deleteById(jobId);

        assertEquals(jobId, updatedJob.getJobId());
        assertEquals(jobName, updatedJob.getName());
    }

    // TODO: We may not need null ID tests
    @Test
    void updateJobNullIdTest() {
        JobAccessorV2 jobAccessor = createJobAccessor();
        Mockito.when(distributionJobRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        try {
            jobAccessor.updateJob(null, null);
            fail("Null jobId did not throw expected AlertConfigurationException.");
        } catch (AlertConfigurationException e) {
            assertNotNull(e);
        }
    }

    private JobAccessorV2 createJobAccessor() {
        Mockito.when(blackDuckJobDetailsAccessor.retrieveNotificationTypesForJob(Mockito.any())).thenReturn(Collections.emptyList());
        Mockito.when(blackDuckJobDetailsAccessor.retrieveProjectDetailsForJob(Mockito.any())).thenReturn(Collections.emptyList());
        Mockito.when(blackDuckJobDetailsAccessor.retrievePolicyNamesForJob(Mockito.any())).thenReturn(Collections.emptyList());
        Mockito.when(blackDuckJobDetailsAccessor.retrieveVulnerabilitySeverityNamesForJob(Mockito.any())).thenReturn(Collections.emptyList());
        return new StaticJobAccessorV2(distributionJobRepository, blackDuckJobDetailsAccessor, null, null, null, null, null, null, new BlackDuckProviderKey());
    }

}
