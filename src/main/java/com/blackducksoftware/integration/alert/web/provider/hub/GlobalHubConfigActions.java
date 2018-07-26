/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.alert.web.provider.hub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubRepository;
import com.blackducksoftware.integration.alert.provider.hub.HubProperties;
import com.blackducksoftware.integration.alert.provider.hub.HubContentConverter;
import com.blackducksoftware.integration.alert.web.actions.ConfigActions;
import com.blackducksoftware.integration.alert.web.exception.AlertFieldException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.log.Slf4jIntLogger;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.blackducksoftware.integration.validator.FieldEnum;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResults;

@Component
public class GlobalHubConfigActions extends ConfigActions<GlobalHubConfigEntity, GlobalHubConfig, GlobalHubRepository> {
    private final Logger logger = LoggerFactory.getLogger(GlobalHubConfigActions.class);
    private final HubProperties hubProperties;

    @Autowired
    public GlobalHubConfigActions(final GlobalHubRepository globalRepository, final HubProperties hubProperties, final HubContentConverter hubContentConverter) {
        super(globalRepository, hubContentConverter);
        this.hubProperties = hubProperties;
    }

    @Override
    public List<GlobalHubConfig> getConfig(final Long id) throws AlertException {
        if (id != null) {
            final Optional<GlobalHubConfigEntity> foundEntity = getRepository().findById(id);
            if (foundEntity.isPresent()) {
                GlobalHubConfig restModel = (GlobalHubConfig) getDatabaseContentConverter().populateRestModelFromDatabaseEntity(foundEntity.get());
                restModel = updateModelFromEnvironment(restModel);
                if (restModel != null) {
                    final GlobalHubConfig maskedRestModel = maskRestModel(restModel);
                    return Arrays.asList(maskedRestModel);
                }
            }
            return Collections.emptyList();
        }
        final List<GlobalHubConfigEntity> databaseEntities = getRepository().findAll();
        List<GlobalHubConfig> restModels = new ArrayList<>(databaseEntities.size());
        if (databaseEntities != null && !databaseEntities.isEmpty()) {
            for (final GlobalHubConfigEntity entity : databaseEntities) {
                restModels.add((GlobalHubConfig) getDatabaseContentConverter().populateRestModelFromDatabaseEntity(entity));
            }
        } else {
            restModels.add(new GlobalHubConfig());
        }
        restModels = updateModelsFromEnvironment(restModels);
        restModels = maskRestModels(restModels);
        return restModels;
    }

    public GlobalHubConfig updateModelFromEnvironment(final GlobalHubConfig restModel) {
        restModel.setHubUrl(hubProperties.getHubUrl().orElse(null));
        if (hubProperties.getHubTrustCertificate().isPresent()) {
            restModel.setHubAlwaysTrustCertificate(String.valueOf(hubProperties.getHubTrustCertificate().get()));
        }
        restModel.setHubProxyHost(hubProperties.getHubProxyHost().orElse(null));
        restModel.setHubProxyPort(hubProperties.getHubProxyPort().orElse(null));
        restModel.setHubProxyUsername(hubProperties.getHubProxyUsername().orElse(null));
        // Do not send passwords going to the UI
        final boolean proxyPasswordIsSet = StringUtils.isNotBlank(hubProperties.getHubProxyPassword().orElse(null));
        restModel.setHubProxyPasswordIsSet(proxyPasswordIsSet);
        return restModel;
    }

    public List<GlobalHubConfig> updateModelsFromEnvironment(final List<GlobalHubConfig> restModels) {
        final List<GlobalHubConfig> updatedRestModels = new ArrayList<>();
        for (final GlobalHubConfig restModel : restModels) {
            updatedRestModels.add(updateModelFromEnvironment(restModel));
        }
        return restModels;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T updateNewConfigWithSavedConfig(final T newConfig, final GlobalHubConfigEntity savedConfig) throws AlertException {
        T updatedConfig = super.updateNewConfigWithSavedConfig(newConfig, savedConfig);
        if (updatedConfig instanceof GlobalHubConfig) {
            updatedConfig = (T) updateModelFromEnvironment((GlobalHubConfig) updatedConfig);
        }
        return updatedConfig;
    }

    @Override
    public String validateConfig(final GlobalHubConfig restModel) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        if (StringUtils.isNotBlank(restModel.getHubTimeout()) && !StringUtils.isNumeric(restModel.getHubTimeout())) {
            fieldErrors.put("hubTimeout", "Not an Integer.");
        }
        if (StringUtils.isNotBlank(restModel.getHubApiKey())) {
            if (restModel.getHubApiKey().length() < 64) {
                fieldErrors.put("hubApiKey", "Not enough characters to be a Hub API Key.");
            } else if (restModel.getHubApiKey().length() > 256) {
                fieldErrors.put("hubApiKey", "Too many characters to be a Hub API Key.");
            }
        }
        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
        return "Valid";
    }

    @SuppressWarnings("deprecation")
    @Override
    public String channelTestConfig(final GlobalHubConfig restModel) throws IntegrationException {
        final Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);

        final String apiToken = restModel.getHubApiKey();

        final HubServerConfigBuilder hubServerConfigBuilder = hubProperties.createHubServerConfigBuilderWithoutAuthentication(intLogger, NumberUtils.toInt(restModel.getHubTimeout()));
        hubServerConfigBuilder.setApiToken(apiToken);

        validateHubConfiguration(hubServerConfigBuilder);
        try (final RestConnection restConnection = createRestConnection(hubServerConfigBuilder)) {
            restConnection.connect();
        } catch (final IOException ex) {
            logger.error("Failed to close rest connection", ex);
        }
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
        return hubServerConfig.createRestConnection(hubServerConfigBuilder.getLogger());
    }
}
