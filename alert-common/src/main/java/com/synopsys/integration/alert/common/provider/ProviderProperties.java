/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.provider;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;

public abstract class ProviderProperties {
    public static final Long UNKNOWN_CONFIG_ID = -1L;
    private Long configId;
    private boolean configEnabled;
    private String configName;

    public ProviderProperties(Long configId, FieldAccessor fieldAccessor) {
        this.configId = configId;
        this.configEnabled = fieldAccessor.getBooleanOrFalse(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        this.configName = fieldAccessor.getString(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME).orElse("UNKNOWN CONFIGURATION");
    }

    public Long getConfigId() {
        return configId;
    }

    public boolean isConfigEnabled() {
        return configEnabled;
    }

    public String getConfigName() {
        return configName;
    }

}
