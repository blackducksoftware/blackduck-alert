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

public interface EnvironmentVariableHandler {
    String getName();

    Set<String> getVariableNames();

    Properties updateFromEnvironment();
}
