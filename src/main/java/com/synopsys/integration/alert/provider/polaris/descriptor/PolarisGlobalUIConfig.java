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

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.EncryptionSettingsValidator;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

//@Component
public class PolarisGlobalUIConfig extends UIConfig {
    public static final String LABEL_POLARIS_URL = "Url";
    public static final String LABEL_POLARIS_ACCESS_TOKEN = "Access Token";
    public static final String LABEL_POLARIS_TIMEOUT = "Timeout";

    private static final String DESCRIPTION_POLARIS_URL = "The URL of the Polaris server.";
    private static final String DESCRIPTION_POLARIS_ACCESS_TOKEN = "The Access token used to retrieve data from the Polaris server.";
    private static final String DESCRIPTION_POLARIS_TIMEOUT = "The timeout in seconds for all connections to the Polaris server.";

    private EncryptionSettingsValidator encryptionValidator;

    public PolarisGlobalUIConfig(EncryptionSettingsValidator encryptionValidator) {
        super(PolarisDescriptor.POLARIS_LABEL, PolarisDescriptor.POLARIS_DESCRIPTION, PolarisDescriptor.POLARIS_URL_NAME);
        this.encryptionValidator = encryptionValidator;
    }

    @Override
    public List<ConfigField> createFields() {
        ConfigField polarisUrl = new TextInputConfigField(PolarisDescriptor.KEY_POLARIS_URL, LABEL_POLARIS_URL, DESCRIPTION_POLARIS_URL).applyRequired(true);
        ConfigField polarisAccessToken = new PasswordConfigField(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, LABEL_POLARIS_ACCESS_TOKEN, DESCRIPTION_POLARIS_ACCESS_TOKEN, encryptionValidator)
                                             .applyRequired(true)
                                             .applyValidationFunctions(this::validateAPIToken);
        ConfigField polarisTimeout = new NumberConfigField(PolarisDescriptor.KEY_POLARIS_TIMEOUT, LABEL_POLARIS_TIMEOUT, DESCRIPTION_POLARIS_TIMEOUT)
                                         .applyRequired(true)
                                         .applyValidationFunctions(this::validateTimeout);

        return List.of(polarisUrl, polarisAccessToken, polarisTimeout);
    }

    private Collection<String> validateAPIToken(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        String accessToken = fieldToValidate.getValue().orElse("");
        if (StringUtils.isNotBlank(accessToken) && (accessToken.length() < 32 || accessToken.length() > 64)) {
            return List.of("Invalid Polaris Access Token.");
        }
        return List.of();
    }

    private Collection<String> validateTimeout(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        String polarisTimeout = fieldToValidate.getValue().orElse("");
        if (!StringUtils.isNumeric(polarisTimeout) || NumberUtils.toInt(polarisTimeout.trim()) < 0) {
            return List.of("Must be an Integer greater than zero (0).");
        }
        return List.of();
    }
}
