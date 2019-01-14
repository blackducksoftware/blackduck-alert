/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;

@Component
public class PolarisGlobalUIConfig extends UIConfig {
    public static final String LABEL_POLARIS_URL = "Url";
    public static final String LABEL_POLARIS_ACCESS_TOKEN = "Access Token";
    public static final String LABEL_POLARIS_TIMEOUT = "Timeout";

    public PolarisGlobalUIConfig() {
        super(PolarisDescriptor.POLARIS_LABEL, PolarisDescriptor.POLARIS_URL_NAME, PolarisDescriptor.POLARIS_ICON);
    }

    @Override
    public List<ConfigField> createFields() {
        final ConfigField polarisUrl = TextInputConfigField.createRequired(PolarisDescriptor.KEY_POLARIS_URL, LABEL_POLARIS_URL);
        final ConfigField polarisAccessToken = PasswordConfigField.createRequired(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, LABEL_POLARIS_ACCESS_TOKEN);
        final ConfigField polarisTimeout = NumberConfigField.createRequired(PolarisDescriptor.KEY_POLARIS_TIMEOUT, LABEL_POLARIS_TIMEOUT);

        return List.of(polarisUrl, polarisAccessToken, polarisTimeout);
    }
}
