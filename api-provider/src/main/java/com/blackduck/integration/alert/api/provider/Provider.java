/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.provider;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.model.ProviderKey;
import com.blackduck.integration.alert.api.provider.state.StatefulProvider;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;

public abstract class Provider {
    private final ProviderKey key;

    protected Provider(ProviderKey key) {
        this.key = key;
    }

    public ProviderKey getKey() {
        return key;
    }

    public abstract boolean validate(ConfigurationModel configurationModel);

    public abstract StatefulProvider createStatefulProvider(ConfigurationModel configurationModel) throws AlertException;

}
