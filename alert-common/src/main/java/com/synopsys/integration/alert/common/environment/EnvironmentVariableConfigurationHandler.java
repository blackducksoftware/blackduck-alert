package com.synopsys.integration.alert.common.environment;

import java.util.Properties;
import java.util.Set;

public interface EnvironmentVariableConfigurationHandler {
    String getName();

    Set<String> getVariableNames();

    Properties updateFromEnvironment();
}
