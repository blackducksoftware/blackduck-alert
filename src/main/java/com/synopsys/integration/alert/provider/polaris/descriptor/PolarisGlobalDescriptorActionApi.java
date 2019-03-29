/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.action.DescriptorActionApi;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.TestConfigModel;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.polaris.PolarisProperties;
import com.synopsys.integration.alert.provider.polaris.tasks.PolarisProjectSyncTask;
import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfigBuilder;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.request.Response;

@Component
public class PolarisGlobalDescriptorActionApi extends DescriptorActionApi {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PolarisProperties polarisProperties;
    private final TaskManager taskManager;

    @Autowired
    public PolarisGlobalDescriptorActionApi(final PolarisProperties polarisProperties, final TaskManager taskManager) {
        this.polarisProperties = polarisProperties;
        this.taskManager = taskManager;
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

        final PolarisServerConfigBuilder configBuilder = polarisProperties.createInitialPolarisServerConfigBuilder(intLogger);
        configBuilder.setUrl(url);
        configBuilder.setAccessToken(accessToken);
        configBuilder.setTimeoutInSeconds(timeout);

        final BuilderStatus builderStatus = configBuilder.validateAndGetBuilderStatus();
        if (!builderStatus.isValid()) {
            throw new AlertException(builderStatus.getFullErrorMessage());
        }

        final PolarisServerConfig polarisServerConfig = configBuilder.build();
        final AccessTokenPolarisHttpClient accessTokenPolarisHttpClient = polarisServerConfig.createPolarisHttpClient(intLogger);
        try (final Response response = accessTokenPolarisHttpClient.attemptAuthentication()) {
            response.throwExceptionForError();
        } catch (final IOException ioException) {
            throw new AlertException(ioException);
        }
    }

    @Override
    public FieldModel saveConfig(final FieldModel fieldModel) {
        // FIXME had to remove client check for now so that the task actually starts without restarting alert.
        // final Optional<AccessTokenPolarisHttpClient> polarisHttpClient = polarisProperties.createPolarisHttpClientSafely(logger);
        final Optional<String> nextRunTime = taskManager.getNextRunTime(BlackDuckAccumulator.TASK_NAME);
        if (nextRunTime.isEmpty()) {
            taskManager.scheduleCronTask(ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION, PolarisProjectSyncTask.TASK_NAME);
        }
        return super.saveConfig(fieldModel);
    }
}
