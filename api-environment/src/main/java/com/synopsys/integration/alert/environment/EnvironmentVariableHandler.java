/*
 * api-environment
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.environment;

import java.util.Properties;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class EnvironmentVariableHandler {

    private final String name;
    private final Set<String> environmentVariableNames;
    private final BooleanSupplier configurationMissingCheck;
    private final Supplier<Properties> updateFunction;

    public EnvironmentVariableHandler(String name, Set<String> environmentVariableNames, BooleanSupplier configurationMissingCheck, Supplier<Properties> updateFunction) {
        this.name = name;
        this.environmentVariableNames = environmentVariableNames;
        this.configurationMissingCheck = configurationMissingCheck;
        this.updateFunction = updateFunction;
    }

    public String getName() {
        return name;
    }

    public Set<String> getVariableNames() {
        return environmentVariableNames;
    }

    public boolean isConfigurationMissing() {
        return configurationMissingCheck.getAsBoolean();
    }

    public Properties updateFromEnvironment() {
        boolean configurationMissing = configurationMissingCheck.getAsBoolean();
        if (configurationMissing) {
            return updateFunction.get();
        }

        return new Properties();
    }
}
