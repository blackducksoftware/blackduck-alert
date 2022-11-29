package com.synopsys.integration.alert.channel.azure.boards.database.mock;

import java.util.UUID;

import com.synopsys.integration.alert.api.oauth.database.configuration.AlertOAuthConfigurationEntity;
import com.synopsys.integration.alert.api.oauth.database.configuration.AlertOAuthConfigurationRepository;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockAlertOAuthConfigurationRepository extends MockRepositoryContainer<UUID, AlertOAuthConfigurationEntity> implements AlertOAuthConfigurationRepository {
    public MockAlertOAuthConfigurationRepository() {
        super(AlertOAuthConfigurationEntity::getId);
    }
}
