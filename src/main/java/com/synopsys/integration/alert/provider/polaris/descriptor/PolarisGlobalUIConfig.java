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

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;

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
        final ConfigField polarisAccessToken = PasswordConfigField.createRequired(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, LABEL_POLARIS_ACCESS_TOKEN, this::validateAPIToken);
        final ConfigField polarisTimeout = NumberConfigField.createRequired(PolarisDescriptor.KEY_POLARIS_TIMEOUT, LABEL_POLARIS_TIMEOUT, this::validateTimeout);

        return List.of(polarisUrl, polarisAccessToken, polarisTimeout);
    }

    private Collection<String> validateAPIToken(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
        final String accessToken = fieldToValidate.getValue().orElse(null);
        if (accessToken.length() < 32 || accessToken.length() > 64) {
            return List.of("Invalid Polaris Access Token.");
        }
        return List.of();
    }

    private Collection<String> validateTimeout(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
        final String polarisTimeout = fieldToValidate.getValue().orElse(null);
        if (!StringUtils.isNumeric(polarisTimeout) || NumberUtils.toInt(polarisTimeout.trim()) < 0) {
            return List.of("Must be an Integer greater than zero (0).");
        }
        return List.of();
    }
}
