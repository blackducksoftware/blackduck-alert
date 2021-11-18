package com.synopsys.integration.alert.channel.email.environment;

import java.util.Properties;
import java.util.Set;

import com.synopsys.integration.alert.common.environment.EnvironmentVariableConfigurationHandler;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

public class EmailEnvironmentVariableHandler implements EnvironmentVariableConfigurationHandler {

    @Override
    public String getName() {
        return ChannelKeys.EMAIL.getDisplayName();
    }

    @Override
    public Set<String> getVariableNames() {
        return Set.of();
    }

    @Override
    public Properties updateFromEnvironment() {
        return null;
    }
}
