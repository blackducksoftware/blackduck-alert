package com.synopsys.integration.alert.api.distribution.mock;

import java.util.UUID;
import java.util.function.Function;

import com.synopsys.integration.alert.database.distribution.workflow.JobSubTaskRepository;
import com.synopsys.integration.alert.database.distribution.workflow.JobSubTaskStatusEntity;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockJobSubTaskRepository extends MockRepositoryContainer<UUID, JobSubTaskStatusEntity> implements JobSubTaskRepository {
    public MockJobSubTaskRepository(Function<JobSubTaskStatusEntity, UUID> idGenerator) {
        super(idGenerator);
    }
}
