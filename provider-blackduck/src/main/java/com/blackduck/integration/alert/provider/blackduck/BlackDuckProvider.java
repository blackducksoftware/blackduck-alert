/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.provider.Provider;
import com.blackduck.integration.alert.api.provider.lifecycle.ProviderTask;
import com.blackduck.integration.alert.api.provider.state.StatefulProvider;
import com.blackduck.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.blackduck.integration.alert.provider.blackduck.factory.BlackDuckTaskFactory;
import com.blackduck.integration.alert.provider.blackduck.validator.BlackDuckSystemValidator;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;

@Component
public class BlackDuckProvider extends Provider {
    private final BlackDuckPropertiesFactory propertiesFactory;
    private final BlackDuckSystemValidator validator;
    private final BlackDuckTaskFactory taskFactory;

    @Autowired
    public BlackDuckProvider(BlackDuckProviderKey blackDuckProviderKey, BlackDuckPropertiesFactory propertiesFactory, BlackDuckSystemValidator validator, BlackDuckTaskFactory taskFactory) {
        super(blackDuckProviderKey);
        this.propertiesFactory = propertiesFactory;
        this.validator = validator;
        this.taskFactory = taskFactory;
    }

    @Override
    public boolean validate(ConfigurationModel configurationModel) {
        BlackDuckProperties blackDuckProperties = propertiesFactory.createProperties(configurationModel);
        return validator.validate(blackDuckProperties);
    }

    @Override
    public StatefulProvider createStatefulProvider(ConfigurationModel configurationModel) throws AlertException {
        BlackDuckProperties blackDuckProperties = propertiesFactory.createProperties(configurationModel);
        List<ProviderTask> tasks = taskFactory.createTasks(blackDuckProperties);

        return StatefulProvider.create(getKey(), configurationModel, tasks, blackDuckProperties);
    }

}
