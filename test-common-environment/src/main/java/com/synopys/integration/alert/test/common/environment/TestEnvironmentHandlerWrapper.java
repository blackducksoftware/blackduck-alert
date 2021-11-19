/*
 * test-common-environment
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopys.integration.alert.test.common.environment;

import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;

public class TestEnvironmentHandlerWrapper implements EnvironmentVariableHandler {
    private EnvironmentVariableHandler wrappedHandler;
    private Properties updatedProperties;

    public TestEnvironmentHandlerWrapper(EnvironmentVariableHandler wrappedHandler) {
        this.wrappedHandler = wrappedHandler;
    }

    public boolean hasUpdateOccurred() {
        return getUpdatedProperties().stream()
            .anyMatch(Predicate.not(Properties::isEmpty));
    }

    public Optional<Properties> getUpdatedProperties() {
        return Optional.ofNullable(updatedProperties);
    }

    @Override
    public String getName() {
        return wrappedHandler.getName();
    }

    @Override
    public Set<String> getVariableNames() {
        return wrappedHandler.getVariableNames();
    }

    @Override
    public Properties updateFromEnvironment() {
        updatedProperties = wrappedHandler.updateFromEnvironment();
        return updatedProperties;
    }
}
