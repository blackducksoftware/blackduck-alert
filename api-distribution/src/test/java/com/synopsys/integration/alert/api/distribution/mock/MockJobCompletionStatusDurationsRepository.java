package com.synopsys.integration.alert.api.distribution.mock;

import java.util.UUID;

import com.synopsys.integration.alert.database.job.execution.JobCompletionDurationsRepository;
import com.synopsys.integration.alert.database.job.execution.JobCompletionStatusDurationsEntity;
import com.blackduck.integration.alert.test.common.database.MockRepositoryContainer;

public class MockJobCompletionStatusDurationsRepository extends MockRepositoryContainer<UUID, JobCompletionStatusDurationsEntity> implements JobCompletionDurationsRepository {
    public MockJobCompletionStatusDurationsRepository() {
        super(JobCompletionStatusDurationsEntity::getJobConfigId);
    }
}
