/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.web.actions.global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.config.AccumulatorConfig;
import com.blackducksoftware.integration.hub.alert.config.DailyDigestBatchConfig;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.config.PurgeConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHubConfigRestModel;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.Slf4jIntLogger;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.blackducksoftware.integration.validator.FieldEnum;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResults;

@Component
public class GlobalHubConfigActions extends ConfigActions<GlobalHubConfigEntity, GlobalHubConfigRestModel> {
    private final Logger logger = LoggerFactory.getLogger(GlobalHubConfigActions.class);
    private final GlobalProperties globalProperties;
    private final AccumulatorConfig accumulatorConfig;
    private final DailyDigestBatchConfig dailyDigestBatchConfig;
    private final PurgeConfig purgeConfig;

    @Autowired
    public GlobalHubConfigActions(final GlobalHubRepository globalRepository, final GlobalProperties globalProperties, final AccumulatorConfig accumulatorConfig, final DailyDigestBatchConfig dailyDigestBatchConfig, final PurgeConfig purgeConfig,
            final ObjectTransformer objectTransformer) {
        super(GlobalHubConfigEntity.class, GlobalHubConfigRestModel.class, globalRepository, objectTransformer);
        this.globalProperties = globalProperties;
        this.accumulatorConfig = accumulatorConfig;
        this.dailyDigestBatchConfig = dailyDigestBatchConfig;
        this.purgeConfig = purgeConfig;
    }

    @Override
    public List<GlobalHubConfigRestModel> getConfig(final Long id) throws AlertException {
        if (id != null) {
            final GlobalHubConfigEntity foundEntity = repository.findOne(id);
            if (foundEntity != null) {
                GlobalHubConfigRestModel restModel = objectTransformer.databaseEntityToConfigRestModel(foundEntity, configRestModelClass);
                restModel = updateModelFromEnvironment(restModel);
                if (restModel != null) {
                    final GlobalHubConfigRestModel maskedRestModel = maskRestModel(restModel);
                    return Arrays.asList(maskedRestModel);
                }
            }
            return Collections.emptyList();
        }
        final List<GlobalHubConfigEntity> databaseEntities = repository.findAll();
        List<GlobalHubConfigRestModel> restModels = null;
        if (databaseEntities != null && !databaseEntities.isEmpty()) {
            restModels = objectTransformer.databaseEntitiesToConfigRestModels(databaseEntities, configRestModelClass);
        } else {
            restModels = new ArrayList<>();
            restModels.add(new GlobalHubConfigRestModel());
        }
        restModels = updateModelsFromEnvironment(restModels);
        restModels = maskRestModels(restModels);
        return restModels;
    }

    public GlobalHubConfigRestModel updateModelFromEnvironment(final GlobalHubConfigRestModel restModel) {
        restModel.setHubUrl(globalProperties.hubUrl);
        if (globalProperties.hubTrustCertificate != null) {
            restModel.setHubAlwaysTrustCertificate(String.valueOf(globalProperties.hubTrustCertificate));
        }
        restModel.setHubProxyHost(globalProperties.hubProxyHost);
        restModel.setHubProxyPort(globalProperties.hubProxyPort);
        restModel.setHubProxyUsername(globalProperties.hubProxyUsername);
        // Do not send passwords going to the UI
        return restModel;
    }

    public List<GlobalHubConfigRestModel> updateModelsFromEnvironment(final List<GlobalHubConfigRestModel> restModels) {
        final List<GlobalHubConfigRestModel> updatedRestModels = new ArrayList<>();
        for (final GlobalHubConfigRestModel restModel : restModels) {
            updatedRestModels.add(updateModelFromEnvironment(restModel));
        }
        return restModels;
    }

    @Override
    public <T> T updateNewConfigWithSavedConfig(final T newConfig, final GlobalHubConfigEntity savedConfig) throws AlertException {
        T updatedConfig = super.updateNewConfigWithSavedConfig(newConfig, savedConfig);
        if (updatedConfig instanceof GlobalHubConfigRestModel) {
            updatedConfig = (T) updateModelFromEnvironment((GlobalHubConfigRestModel) updatedConfig);
        }
        return updatedConfig;
    }

    @Override
    public String validateConfig(final GlobalHubConfigRestModel restModel) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        if (StringUtils.isNotBlank(restModel.getHubTimeout()) && !StringUtils.isNumeric(restModel.getHubTimeout())) {
            fieldErrors.put("hubTimeout", "Not an Integer.");
        }

        if (StringUtils.isNotBlank(restModel.getAccumulatorCron())) {
            try {
                new CronTrigger(restModel.getAccumulatorCron(), TimeZone.getTimeZone("UTC"));
            } catch (final IllegalArgumentException e) {
                fieldErrors.put("accumulatorCron", e.getMessage());
            }
        }

        if (StringUtils.isNotBlank(restModel.getDailyDigestCron())) {
            try {
                new CronTrigger(restModel.getDailyDigestCron(), TimeZone.getTimeZone("UTC"));
            } catch (final IllegalArgumentException e) {
                fieldErrors.put("dailyDigestCron", e.getMessage());
            }
        }

        if (StringUtils.isNotBlank(restModel.getPurgeDataCron())) {
            try {
                new CronTrigger(restModel.getPurgeDataCron(), TimeZone.getTimeZone("UTC"));
            } catch (final IllegalArgumentException e) {
                fieldErrors.put("purgeDataCron", e.getMessage());
            }
        }

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    @Override
    public String channelTestConfig(final GlobalHubConfigRestModel restModel) throws IntegrationException {
        final Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);

        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setHubUrl(globalProperties.hubUrl);
        hubServerConfigBuilder.setTimeout(restModel.getHubTimeout());
        hubServerConfigBuilder.setUsername(restModel.getHubUsername());

        hubServerConfigBuilder.setProxyHost(globalProperties.hubProxyHost);
        hubServerConfigBuilder.setProxyPort(globalProperties.hubProxyPort);
        hubServerConfigBuilder.setProxyUsername(globalProperties.hubProxyUsername);

        hubServerConfigBuilder.setPassword(restModel.getHubPassword());
        hubServerConfigBuilder.setProxyPassword(globalProperties.hubProxyPassword);

        if (globalProperties.hubTrustCertificate != null) {
            hubServerConfigBuilder.setAlwaysTrustServerCertificate(globalProperties.hubTrustCertificate);
        }
        hubServerConfigBuilder.setLogger(intLogger);
        validateHubConfiguration(hubServerConfigBuilder);
        final RestConnection restConnection = createRestConnection(hubServerConfigBuilder);
        restConnection.connect();
        return "Successfully connected to the Hub.";
    }

    public void validateHubConfiguration(final HubServerConfigBuilder hubServerConfigBuilder) throws AlertFieldException {
        final AbstractValidator validator = hubServerConfigBuilder.createValidator();
        final ValidationResults results = validator.assertValid();
        if (!results.getResultMap().isEmpty()) {
            final Map<String, String> fieldErrors = new HashMap<>();
            for (final Entry<FieldEnum, Set<ValidationResult>> result : results.getResultMap().entrySet()) {
                final Set<ValidationResult> validationResult = result.getValue();
                final List<String> errors = new ArrayList<>();
                for (final ValidationResult currentValidationResult : validationResult) {
                    errors.add(currentValidationResult.getMessage());
                }

                fieldErrors.put(result.getKey().getKey(), StringUtils.join(errors, " , "));
            }
            throw new AlertFieldException("There were issues with the configuration.", fieldErrors);
        }
    }

    public RestConnection createRestConnection(final HubServerConfigBuilder hubServerConfigBuilder) throws IntegrationException {
        final HubServerConfig hubServerConfig = hubServerConfigBuilder.build();
        return hubServerConfig.createCredentialsRestConnection(hubServerConfigBuilder.getLogger());
    }

    @Override
    public void configurationChangeTriggers(final GlobalHubConfigRestModel globalConfig) {
        if (globalConfig != null) {
            accumulatorConfig.scheduleJobExecution(globalConfig.getAccumulatorCron());
            dailyDigestBatchConfig.scheduleJobExecution(globalConfig.getDailyDigestCron());
            purgeConfig.scheduleJobExecution(globalConfig.getPurgeDataCron());

        }
    }

    @Override
    public List<String> sensitiveFields() {
        final List<String> sensitiveFields = new ArrayList<>();
        sensitiveFields.add("hubPassword");
        return sensitiveFields;
    }

}
