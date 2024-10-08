package com.blackduck.integration.alert.channel.azure.boards.distribution.event.mock;

import java.util.UUID;

import com.synopsys.integration.alert.database.job.azure.boards.AzureBoardsJobDetailsEntity;
import com.synopsys.integration.alert.database.job.azure.boards.AzureBoardsJobDetailsRepository;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockAzureBoardsJobDetailsRepository extends MockRepositoryContainer<UUID, AzureBoardsJobDetailsEntity> implements AzureBoardsJobDetailsRepository {
    public MockAzureBoardsJobDetailsRepository() {
        super(AzureBoardsJobDetailsEntity::getJobId);
    }
}
