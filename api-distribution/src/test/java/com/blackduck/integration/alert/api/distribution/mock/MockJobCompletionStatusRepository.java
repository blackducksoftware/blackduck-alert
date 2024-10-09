package com.blackduck.integration.alert.api.distribution.mock;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.blackduck.integration.alert.database.job.execution.JobCompletionDurationsRepository;
import com.blackduck.integration.alert.database.job.execution.JobCompletionRepository;
import com.blackduck.integration.alert.database.job.execution.JobCompletionStatusEntity;
import com.blackduck.integration.alert.test.common.database.MockRepositoryContainer;

public class MockJobCompletionStatusRepository extends MockRepositoryContainer<UUID, JobCompletionStatusEntity> implements JobCompletionRepository {

    private final JobCompletionDurationsRepository jobExecutionStatusDurationsRepository;

    public MockJobCompletionStatusRepository(JobCompletionDurationsRepository jobExecutionStatusDurationsRepository) {
        super(JobCompletionStatusEntity::getJobConfigId);
        this.jobExecutionStatusDurationsRepository = jobExecutionStatusDurationsRepository;
    }

    @Override
    public Page<JobCompletionStatusEntity> findBySearchTerm(String searchTerm, Pageable pageable) {
        return Page.empty();
    }

}
