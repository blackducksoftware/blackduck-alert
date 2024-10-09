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
