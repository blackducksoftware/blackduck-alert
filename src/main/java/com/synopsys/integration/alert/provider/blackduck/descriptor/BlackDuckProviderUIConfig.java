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
package com.synopsys.integration.alert.provider.blackduck.descriptor;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ReadOnlyConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;

@Component
public class BlackDuckProviderUIConfig extends UIConfig {
    private static final String PROXY_SUB_GROUP = "Proxy Configuration";

    public BlackDuckProviderUIConfig() {
        super(BlackDuckDescriptor.BLACKDUCK_LABEL, BlackDuckDescriptor.BLACKDUCK_URL, BlackDuckDescriptor.BLACKDUCK_ICON);
    }

    @Override
    public List<ConfigField> createFields() {
        final ConfigField blackDuckUrl = ReadOnlyConfigField.createRequired(BlackDuckDescriptor.KEY_BLACKDUCK_URL, "Url");
        final ConfigField blackDuckApiKey = PasswordConfigField.createRequired(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, "API Token", this::validateAPIToken);
        final ConfigField blackDuckTimeout = NumberConfigField.createRequired(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT, "Timeout (in seconds)");
        final ConfigField blackDuckProxyHost = ReadOnlyConfigField.createGrouped(BlackDuckDescriptor.KEY_BLACKDUCK_PROXY_HOST, "Host Name", PROXY_SUB_GROUP);
        final ConfigField blackDuckProxyPort = ReadOnlyConfigField.createGrouped(BlackDuckDescriptor.KEY_BLACKDUCK_PROXY_PORT, "Port", PROXY_SUB_GROUP);
        final ConfigField blackDuckProxyUsername = ReadOnlyConfigField.createGrouped(BlackDuckDescriptor.KEY_BLACKDUCK_PROXY_USERNAME, "Username", PROXY_SUB_GROUP);
        final ConfigField blackDuckProxyPassword = ReadOnlyConfigField.createSensitiveGrouped(BlackDuckDescriptor.KEY_BLACKDUCK_PROXY_PASS, "Proxy Password", PROXY_SUB_GROUP);

        return List.of(blackDuckUrl, blackDuckApiKey, blackDuckTimeout, blackDuckProxyHost, blackDuckProxyPort, blackDuckProxyUsername, blackDuckProxyPassword);
    }

    private Collection<String> validateAPIToken(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
        final String apiKey = fieldToValidate.getValue().orElse(null);
        if (apiKey.length() < 64 || apiKey.length() > 256) {
            return List.of("Invalid Black Duck API Token.");
        }
        return List.of();
    }
}
