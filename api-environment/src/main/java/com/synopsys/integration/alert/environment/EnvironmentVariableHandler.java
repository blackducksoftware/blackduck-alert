/*
 * api-environment
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
    private final Supplier<Set<String>> environmentVariableSupplier;
    private final BooleanSupplier configurationMissingCheck;
    private final Supplier<Properties> updateFunction;

    public EnvironmentVariableHandler(String name, Supplier<Set<String>> environmentVariableSupplier, BooleanSupplier configurationMissingCheck, Supplier<Properties> updateFunction) {
        this.name = name;
        this.environmentVariableSupplier = environmentVariableSupplier;
        this.configurationMissingCheck = configurationMissingCheck;
        this.updateFunction = updateFunction;
    }

    public String getName() {
        return name;
    }

    public Set<String> getVariableNames() {
        return environmentVariableSupplier.get();
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
