package com.synopsys.integration.alert.channel.azure.boards.distribution.event.mock;

import java.util.UUID;

import com.synopsys.integration.alert.database.distribution.workflow.JobSubTaskRepository;
import com.synopsys.integration.alert.database.distribution.workflow.JobSubTaskStatusEntity;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockJobSubTaskStatusRepository extends MockRepositoryContainer<UUID, JobSubTaskStatusEntity> implements JobSubTaskRepository {
    public MockJobSubTaskStatusRepository() {
        super(JobSubTaskStatusEntity::getId);
    }
}