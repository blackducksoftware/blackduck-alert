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

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.provider.polaris.PolarisProperties;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.polaris.common.rest.AccessTokenRestConnection;
import com.synopsys.integration.rest.request.Response;

@Component
public class PolarisGlobalDescriptorActionApi extends DescriptorActionApi {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PolarisProperties polarisProperties;

    @Autowired
    public PolarisGlobalDescriptorActionApi(final PolarisProperties polarisProperties) {
        this.polarisProperties = polarisProperties;
    }

    @Override
    public void validateConfig(final FieldModel fieldModel, final Map<String, String> fieldErrors) {
        final String accessToken = fieldModel.getField(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN).flatMap(field -> field.getValue()).orElse(null);
        if (StringUtils.isNotBlank(accessToken)) {
            if (accessToken.length() < 32 || accessToken.length() > 64) {
                fieldErrors.put(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN, "Invalid Polaris Access Token.");
            }
        }

        final String polarisTimeout = fieldModel.getField(PolarisDescriptor.KEY_POLARIS_TIMEOUT).flatMap(field -> field.getValue()).orElse(null);
        if (StringUtils.isNotBlank(polarisTimeout)) {
            if (!StringUtils.isNumeric(polarisTimeout) || NumberUtils.toInt(polarisTimeout.trim()) < 0) {
                fieldErrors.put(PolarisDescriptor.KEY_POLARIS_TIMEOUT, "Must be an Integer greater than zero (0).");
            }
        }
    }

    @Override
    public void testConfig(final TestConfigModel testConfig) throws IntegrationException {
        final Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);

        final FieldModel fieldModel = testConfig.getFieldModel();
        final FieldAccessor fieldAccessor = fieldModel.convertToFieldAccessor();
        validateFieldFormatting(fieldModel);

        final String errorMessageFormat = "The field %s is required";
        final String url = fieldAccessor
                               .getString(PolarisDescriptor.KEY_POLARIS_URL)
                               .orElseThrow(() -> new AlertException(String.format(errorMessageFormat, PolarisGlobalUIConfig.LABEL_POLARIS_URL)));
        final String accessToken = fieldAccessor
                                       .getString(PolarisDescriptor.KEY_POLARIS_ACCESS_TOKEN)
                                       .orElseThrow(() -> new AlertException(String.format(errorMessageFormat, PolarisGlobalUIConfig.LABEL_POLARIS_ACCESS_TOKEN)));
        final Integer timeout = fieldAccessor
                                    .getInteger(PolarisDescriptor.KEY_POLARIS_TIMEOUT)
                                    .orElseThrow(() -> new AlertException(String.format(errorMessageFormat, PolarisGlobalUIConfig.LABEL_POLARIS_TIMEOUT)));

        final AccessTokenRestConnection accessTokenRestConnection = polarisProperties.createRestConnection(intLogger, url, accessToken, timeout);
        try (final Response response = accessTokenRestConnection.attemptAuthentication()) {
            response.throwExceptionForError();
        } catch (final IOException ioException) {
            throw new AlertException(ioException);
        }
    }
}
