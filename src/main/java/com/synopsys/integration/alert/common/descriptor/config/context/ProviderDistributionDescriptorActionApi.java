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
package com.synopsys.integration.alert.common.descriptor.config.context;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

public abstract class ProviderDistributionDescriptorActionApi extends DescriptorActionApi {

    public abstract void validateProviderDistributionConfig(FieldModel fieldModel, Map<String, String> fieldErrors);

    @Override
    public void validateConfig(final FieldModel fieldModel, final Map<String, String> fieldErrors) {
        final String formatType = fieldModel.getField(ProviderDistributionUIConfig.KEY_FORMAT_TYPE).flatMap(field -> field.getValue()).orElse(null);
        if (StringUtils.isBlank(formatType)) {
            fieldErrors.put(ProviderDistributionUIConfig.KEY_FORMAT_TYPE, "You must choose a format.");
        }
        final String frequency = fieldModel.getField(CommonDistributionUIConfig.KEY_FREQUENCY).flatMap(field -> field.getValue()).orElse(null);
        if (StringUtils.isBlank(frequency)) {
            fieldErrors.put(CommonDistributionUIConfig.KEY_FREQUENCY, "Frequency cannot be blank.");
        }
        final Collection<String> notificationTypes = fieldModel.getField(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES).flatMap(field -> Optional.ofNullable(field.getValues())).orElse(Collections.emptyList());
        if (notificationTypes == null || notificationTypes.size() <= 0) {
            fieldErrors.put(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES, "Must have at least one notification type.");
        }

        validateProviderDistributionConfig(fieldModel, fieldErrors);
    }

    @Override
    public void testConfig(final TestConfigModel testConfig) throws IntegrationException {

    }
}
