package com.synopsys.integration.alert.database.api.distribution;

import java.util.UUID;

import com.synopsys.integration.alert.database.distribution.workflow.JobSubTaskRepository;
import com.synopsys.integration.alert.database.distribution.workflow.JobSubTaskStatusEntity;
import com.synopsys.integration.alert.database.distribution.workflow.MockRepositoryContainer;

public class MockJobSubTaskStatusRepository extends MockRepositoryContainer<UUID, JobSubTaskStatusEntity> implements JobSubTaskRepository {
    public MockJobSubTaskStatusRepository() {
        super(JobSubTaskStatusEntity::getId);
    }
}
