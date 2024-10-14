/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.database.mock;

import java.util.UUID;

import com.blackduck.integration.alert.api.oauth.database.configuration.AlertOAuthConfigurationEntity;
import com.blackduck.integration.alert.api.oauth.database.configuration.AlertOAuthConfigurationRepository;
import com.blackduck.integration.alert.test.common.database.MockRepositoryContainer;

public class MockAlertOAuthConfigurationRepository extends MockRepositoryContainer<UUID, AlertOAuthConfigurationEntity> implements AlertOAuthConfigurationRepository {
    public MockAlertOAuthConfigurationRepository() {
        super(AlertOAuthConfigurationEntity::getId);
    }
}
