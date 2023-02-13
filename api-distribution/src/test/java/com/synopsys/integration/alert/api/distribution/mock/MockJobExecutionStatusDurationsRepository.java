package com.synopsys.integration.alert.api.distribution.mock;

import java.util.UUID;

import com.synopsys.integration.alert.database.job.execution.JobExecutionDurationsRepository;
import com.synopsys.integration.alert.database.job.execution.JobExecutionStatusDurationsEntity;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockJobExecutionStatusDurationsRepository extends MockRepositoryContainer<UUID, JobExecutionStatusDurationsEntity> implements JobExecutionDurationsRepository {
    public MockJobExecutionStatusDurationsRepository() {
        super(JobExecutionStatusDurationsEntity::getJobConfigId);
    }
}
