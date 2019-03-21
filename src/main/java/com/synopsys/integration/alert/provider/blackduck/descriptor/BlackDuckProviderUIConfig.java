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

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class BlackDuckProviderUIConfig extends UIConfig {
    private static final String LABEL_URL = "Url";
    private static final String LABEL_API_TOKEN = "API Token";
    private static final String LABEL_TIMEOUT = "Timeout";

    private static final String BLACKDUCK_URL_DESCRIPTION = "The URL of the Black Duck server.";
    private static final String BLACKDUCK_API_KEY_DESCRIPTION = "The API token used to retrieve data from the Black Duck server. The API token should be for a super user.";
    private static final String BLACKDUCK_TIMEOUT_DESCRIPTION = "The timeout in seconds for all connections to the Black Duck server. Default: 300.";

    public BlackDuckProviderUIConfig() {
        super(BlackDuckDescriptor.BLACKDUCK_LABEL, BlackDuckDescriptor.BLACKDUCK_DESCRIPTION, BlackDuckDescriptor.BLACKDUCK_URL, BlackDuckDescriptor.BLACKDUCK_ICON);
    }

    @Override
    public List<ConfigField> createFields() {
        final ConfigField blackDuckUrl = TextInputConfigField.createRequired(BlackDuckDescriptor.KEY_BLACKDUCK_URL, LABEL_URL, BLACKDUCK_URL_DESCRIPTION);
        final ConfigField blackDuckApiKey = PasswordConfigField.createRequired(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, LABEL_API_TOKEN, BLACKDUCK_API_KEY_DESCRIPTION, this::validateAPIToken);
        final ConfigField blackDuckTimeout = NumberConfigField.createRequired(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT, LABEL_TIMEOUT, BLACKDUCK_TIMEOUT_DESCRIPTION);

        return List.of(blackDuckUrl, blackDuckApiKey, blackDuckTimeout);
    }

    private Collection<String> validateAPIToken(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
        final String apiKey = fieldToValidate.getValue().orElse("");
        if (StringUtils.isNotBlank(apiKey) && (apiKey.length() < 64 || apiKey.length() > 256)) {
            return List.of("Invalid Black Duck API Token.");
        }
        return List.of();
    }
}