/*
 * api-environment
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.environment;

import com.synopsys.integration.alert.api.common.model.Obfuscated;

public interface EnvironmentVariableHandlerFactory2<T extends Obfuscated<T>> {

    EnvironmentVariableHandler2<T> build();
}
