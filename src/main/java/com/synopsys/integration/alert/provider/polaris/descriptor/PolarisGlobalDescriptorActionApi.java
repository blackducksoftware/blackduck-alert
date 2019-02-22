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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.alert.common.descriptor.action.DescriptorActionApi;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.provider.polaris.PolarisProperties;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.request.Response;

// TODO exclude for now: @Component
public class PolarisGlobalDescriptorActionApi extends DescriptorActionApi {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PolarisProperties polarisProperties;

    // TODO exclude for now: @Autowired
    public PolarisGlobalDescriptorActionApi(final PolarisProperties polarisProperties) {
        this.polarisProperties = polarisProperties;
    }

    @Override
    public void testConfig(final TestConfigModel testConfig) throws IntegrationException {
        final Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);

        final FieldAccessor fieldAccessor = testConfig.getFieldAccessor();
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

        final AccessTokenPolarisHttpClient accessTokenPolarisHttpClient = polarisProperties.createPolarisHttpClient(intLogger, url, accessToken, timeout);
        try (final Response response = accessTokenPolarisHttpClient.attemptAuthentication()) {
            response.throwExceptionForError();
        } catch (final IOException ioException) {
            throw new AlertException(ioException);
        }
    }
}
