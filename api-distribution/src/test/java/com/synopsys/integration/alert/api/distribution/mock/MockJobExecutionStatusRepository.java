package com.synopsys.integration.alert.api.distribution.mock;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.synopsys.integration.alert.database.job.execution.JobExecutionDurationsRepository;
import com.synopsys.integration.alert.database.job.execution.JobExecutionRepository;
import com.synopsys.integration.alert.database.job.execution.JobExecutionStatusEntity;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockJobExecutionStatusRepository extends MockRepositoryContainer<UUID, JobExecutionStatusEntity> implements JobExecutionRepository {

    private final JobExecutionDurationsRepository jobExecutionStatusDurationsRepository;

    public MockJobExecutionStatusRepository(JobExecutionDurationsRepository jobExecutionStatusDurationsRepository) {
        super(JobExecutionStatusEntity::getJobConfigId);
        this.jobExecutionStatusDurationsRepository = jobExecutionStatusDurationsRepository;
    }
    
    @Override
    public Page<JobExecutionStatusEntity> findBySearchTerm(String searchTerm, Pageable pageable) {
        return Page.empty();
    }

}
