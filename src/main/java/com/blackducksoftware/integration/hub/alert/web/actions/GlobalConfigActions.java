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
package com.blackducksoftware.integration.hub.alert.web.actions;

import java.util.ArrayList;
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
import com.blackducksoftware.integration.hub.alert.datasource.entity.GlobalConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.GlobalRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.GlobalConfigRestModel;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.Slf4jIntLogger;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.blackducksoftware.integration.validator.FieldEnum;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResults;

@Component
public class GlobalConfigActions extends ConfigActions<GlobalConfigEntity, GlobalConfigRestModel> {
    private final Logger logger = LoggerFactory.getLogger(GlobalConfigActions.class);
    private final AccumulatorConfig accumulatorConfig;
    private final DailyDigestBatchConfig dailyDigestBatchConfig;

    @Autowired
    public GlobalConfigActions(final GlobalRepository globalRepository, final AccumulatorConfig accumulatorConfig, final DailyDigestBatchConfig dailyDigestBatchConfig, final ObjectTransformer objectTransformer) {
        super(GlobalConfigEntity.class, GlobalConfigRestModel.class, globalRepository, objectTransformer);
        this.accumulatorConfig = accumulatorConfig;
        this.dailyDigestBatchConfig = dailyDigestBatchConfig;
    }

    @Override
    public GlobalConfigRestModel maskRestModel(final GlobalConfigRestModel restModel) {
        final GlobalConfigRestModel maskedRestModel = new GlobalConfigRestModel(restModel.getId(), restModel.getHubUrl(), restModel.getHubTimeout(), restModel.getHubUsername(), "", restModel.getHubProxyHost(), restModel.getHubProxyPort(),
                restModel.getHubProxyUsername(), "", restModel.getHubAlwaysTrustCertificate(), restModel.getAccumulatorCron(), restModel.getDailyDigestCron());
        return maskedRestModel;
    }

    @Override
    public GlobalConfigEntity updateNewConfigWithSavedConfig(final GlobalConfigEntity newConfig, final GlobalConfigEntity savedConfig) {
        final Long id = newConfig.getId();
        final String hubUrl = newConfig.getHubUrl();
        final Integer hubTimeout = newConfig.getHubTimeout();
        final String hubUsername = newConfig.getHubUsername();
        String hubPassword = "";
        if (StringUtils.isBlank(newConfig.getHubPassword())) {
            hubPassword = savedConfig.getHubPassword();
        } else {
            hubPassword = newConfig.getHubPassword();
        }
        final String hubProxyHost = newConfig.getHubProxyHost();
        final String hubProxyPort = newConfig.getHubProxyPort();
        final String hubProxyUsername = newConfig.getHubProxyUsername();
        String hubProxyPassword = "";
        if (StringUtils.isBlank(newConfig.getHubProxyPassword())) {
            hubProxyPassword = savedConfig.getHubProxyPassword();
        } else {
            hubProxyPassword = newConfig.getHubProxyPassword();
        }
        final Boolean hubAlwaysTrustCertificate = newConfig.getHubAlwaysTrustCertificate();
        final String accumulatorCron = newConfig.getAccumulatorCron();
        final String dailyDigestCron = newConfig.getDailyDigestCron();
        final GlobalConfigEntity globalConfigEntity = new GlobalConfigEntity(hubUrl, hubTimeout, hubUsername, hubPassword, hubProxyHost, hubProxyPort, hubProxyUsername, hubProxyPassword, hubAlwaysTrustCertificate, accumulatorCron,
                dailyDigestCron);
        globalConfigEntity.setId(id);
        return globalConfigEntity;
    }

    @Override
    public String validateConfig(final GlobalConfigRestModel restModel) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        if (StringUtils.isNotBlank(restModel.getHubTimeout()) && !StringUtils.isNumeric(restModel.getHubTimeout())) {
            fieldErrors.put("hubTimeout", "Not an Integer.");
        }

        if (StringUtils.isNotBlank(restModel.getHubAlwaysTrustCertificate()) && !isBoolean(restModel.getHubAlwaysTrustCertificate())) {
            fieldErrors.put("hubAlwaysTrustCertificate", "Not an Boolean.");
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

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    @Override
    public String testConfig(final GlobalConfigRestModel restModel) throws IntegrationException {
        final Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);

        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setHubUrl(restModel.getHubUrl());
        hubServerConfigBuilder.setTimeout(restModel.getHubTimeout());
        hubServerConfigBuilder.setUsername(restModel.getHubUsername());

        hubServerConfigBuilder.setProxyHost(restModel.getHubProxyHost());
        hubServerConfigBuilder.setProxyPort(restModel.getHubProxyPort());
        hubServerConfigBuilder.setProxyUsername(restModel.getHubProxyUsername());

        if (null != restModel && StringUtils.isNotBlank(restModel.getId()) && (StringUtils.isBlank(restModel.getHubPassword()) || StringUtils.isBlank(restModel.getHubProxyPassword()))) {
            final Long longId = objectTransformer.stringToLong(restModel.getId());
            final GlobalConfigEntity savedConfig = repository.findOne(longId);
            if (null != savedConfig && StringUtils.isBlank(restModel.getHubPassword())) {
                hubServerConfigBuilder.setPassword(savedConfig.getHubPassword());
            } else {
                hubServerConfigBuilder.setPassword(restModel.getHubPassword());
            }
            if (null != savedConfig && StringUtils.isBlank(restModel.getHubProxyPassword())) {
                hubServerConfigBuilder.setProxyPassword(savedConfig.getHubProxyPassword());
            } else {
                hubServerConfigBuilder.setProxyPassword(restModel.getHubProxyPassword());
            }
        } else {
            hubServerConfigBuilder.setPassword(restModel.getHubPassword());
            hubServerConfigBuilder.setProxyPassword(restModel.getHubProxyPassword());
        }

        if (StringUtils.isNotBlank(restModel.getHubAlwaysTrustCertificate())) {
            hubServerConfigBuilder.setAlwaysTrustServerCertificate(Boolean.valueOf(restModel.getHubAlwaysTrustCertificate()));
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
    public void configurationChangeTriggers(final GlobalConfigRestModel globalConfig) {
        if (globalConfig != null) {
            accumulatorConfig.scheduleJobExecution(globalConfig.getAccumulatorCron());
            dailyDigestBatchConfig.scheduleJobExecution(globalConfig.getDailyDigestCron());
        }
    }

}
