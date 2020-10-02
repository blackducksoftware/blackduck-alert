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
package com.synopsys.integration.alert.provider.blackduck.descriptor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.URLInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.EncryptionSettingsValidator;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.ValidationResult;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderGlobalUIConfig;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;

@Component
public class BlackDuckProviderUIConfig extends ProviderGlobalUIConfig {
    private static final String LABEL_URL = "Url";
    private static final String LABEL_API_TOKEN = "API Token";
    private static final String LABEL_TIMEOUT = "Timeout";

    private static final String BLACKDUCK_URL_DESCRIPTION = "The URL of the Black Duck server.";
    private static final String BLACKDUCK_API_KEY_DESCRIPTION = "The API token used to retrieve data from the Black Duck server. The API token should be for a super user.";
    private static final String BLACKDUCK_TIMEOUT_DESCRIPTION = "The timeout in seconds for all connections to the Black Duck server.";
    private final EncryptionSettingsValidator encryptionValidator;

    @Autowired
    public BlackDuckProviderUIConfig(BlackDuckProviderKey blackDuckProviderKey, EncryptionSettingsValidator encryptionValidator, ConfigurationAccessor configurationAccessor) {
        super(blackDuckProviderKey, BlackDuckDescriptor.BLACKDUCK_LABEL, BlackDuckDescriptor.BLACKDUCK_DESCRIPTION, BlackDuckDescriptor.BLACKDUCK_URL, configurationAccessor);
        this.encryptionValidator = encryptionValidator;
    }

    @Override
    public List<ConfigField> createProviderGlobalFields() {
        ConfigField blackDuckUrl = new URLInputConfigField(BlackDuckDescriptor.KEY_BLACKDUCK_URL, LABEL_URL, BLACKDUCK_URL_DESCRIPTION).applyRequired(true);
        ConfigField blackDuckApiKey = new PasswordConfigField(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, LABEL_API_TOKEN, BLACKDUCK_API_KEY_DESCRIPTION, encryptionValidator)
                                          .applyRequired(true)
                                          .applyValidationFunctions(this::validateAPIToken);
        String defaultTimeout = String.valueOf(BlackDuckProperties.DEFAULT_TIMEOUT);
        ConfigField blackDuckTimeout = new NumberConfigField(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT, LABEL_TIMEOUT, BLACKDUCK_TIMEOUT_DESCRIPTION)
                                           .applyRequired(true)
                                           .applyValidationFunctions(this::validateTimeout)
                                           .applyDefaultValue(defaultTimeout);

        return List.of(blackDuckUrl, blackDuckApiKey, blackDuckTimeout);
    }

    private ValidationResult validateAPIToken(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        String apiKey = fieldToValidate.getValue().orElse("");
        if (StringUtils.isNotBlank(apiKey) && (apiKey.length() < 64 || apiKey.length() > 256)) {
            return ValidationResult.errors("Invalid Black Duck API Token.");
        }
        return ValidationResult.success();
    }

    private ValidationResult validateTimeout(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        int timeoutInt = fieldToValidate.getValue()
                             .map(NumberUtils::toInt)
                             .orElse(BlackDuckProperties.DEFAULT_TIMEOUT);
        if (timeoutInt < 1) {
            return ValidationResult.errors("Invalid timeout: The timeout must be a positive integer");
        } else if (timeoutInt > 300) {
            ValidationResult.warnings("The provided timeout is greater than five minutes. Please ensure this is the desired behavior.");
        }
        return ValidationResult.success();
    }

}
