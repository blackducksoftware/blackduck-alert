/**
 * blackduck-alert
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
package com.synopsys.integration.alert.provider.polaris.descriptor;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.provider.polaris.PolarisProviderKey;

//@Component
public class PolarisDescriptor extends ProviderDescriptor {
    public static final String KEY_POLARIS_URL = "polaris.url";
    public static final String KEY_POLARIS_ACCESS_TOKEN = "polaris.access.token";
    public static final String KEY_POLARIS_TIMEOUT = "polaris.timeout";

    public static final String POLARIS_LABEL = "Polaris";
    public static final String POLARIS_URL_NAME = "polaris";
    public static final String POLARIS_DESCRIPTION = "This is the configuration to connect to the Polaris server. Configuring this will cause Alert to start pulling data from Polaris Issues.";

    @Autowired
    public PolarisDescriptor(PolarisProviderKey polarisProviderKey, PolarisGlobalUIConfig polarisGlobalUIConfig, PolarisDistributionUIConfig polarisDistributionUIConfig) {
        super(polarisProviderKey, polarisGlobalUIConfig, polarisDistributionUIConfig);
    }

}
