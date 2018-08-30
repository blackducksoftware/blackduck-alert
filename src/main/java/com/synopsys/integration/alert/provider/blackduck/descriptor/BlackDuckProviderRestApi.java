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
package com.synopsys.integration.alert.provider.blackduck.descriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.RestApi;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckConfig;
import com.synopsys.integration.blackduck.configuration.HubServerConfig;
import com.synopsys.integration.blackduck.configuration.HubServerConfigBuilder;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.validator.AbstractValidator;
import com.synopsys.integration.validator.FieldEnum;
import com.synopsys.integration.validator.ValidationResult;
import com.synopsys.integration.validator.ValidationResults;

@Component
public class BlackDuckProviderRestApi extends RestApi {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BlackDuckProperties blackDuckProperties;

    @Autowired
    public BlackDuckProviderRestApi(final BlackDuckTypeConverter databaseContentConverter, final BlackDuckRepositoryAccessor repositoryAccessor, final BlackDuckProviderStartupComponent startupComponent,
    final BlackDuckProperties blackDuckProperties) {
        super(databaseContentConverter, repositoryAccessor, startupComponent);
        this.blackDuckProperties = blackDuckProperties;
    }

    @Override
    public void validateConfig(final Config config, final Map<String, String> fieldErrors) {
        final BlackDuckConfig blackDuckConfig = (BlackDuckConfig) config;
        if (StringUtils.isNotBlank(blackDuckConfig.getBlackDuckTimeout()) && !StringUtils.isNumeric(blackDuckConfig.getBlackDuckTimeout())) {
            fieldErrors.put("blackDuckTimeout", "Not an Integer.");
        }
        if (StringUtils.isNotBlank(blackDuckConfig.getBlackDuckApiKey())) {
            if (blackDuckConfig.getBlackDuckApiKey().length() < 64) {
                fieldErrors.put("blackDuckApiKey", "Not enough characters to be a Hub API Key.");
            } else if (blackDuckConfig.getBlackDuckApiKey().length() > 256) {
                fieldErrors.put("blackDuckApiKey", "Too many characters to be a Hub API Key.");
            }
        }
    }

    @Override
    public void testConfig(final DatabaseEntity entity) throws IntegrationException {
        final Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);

        final GlobalBlackDuckConfigEntity blackDuckEntity = (GlobalBlackDuckConfigEntity) entity;
        final String apiToken = blackDuckEntity.getBlackDuckApiKey();
        final String url = blackDuckEntity.getBlackDuckUrl();

        final HubServerConfigBuilder blackDuckServerConfigBuilder = blackDuckProperties.createServerConfigBuilderWithoutAuthentication(intLogger, blackDuckEntity.getBlackDuckTimeout());
        blackDuckServerConfigBuilder.setApiToken(apiToken);
        blackDuckServerConfigBuilder.setUrl(url);

        validateBlackDuckConfiguration(blackDuckServerConfigBuilder);
        try (final RestConnection restConnection = createRestConnection(blackDuckServerConfigBuilder)) {
            restConnection.connect();
        } catch (final IOException ex) {
            logger.error("Failed to close rest connection", ex);
        }
    }

    public void validateBlackDuckConfiguration(final HubServerConfigBuilder blackDuckServerConfigBuilder) throws AlertFieldException {
        final AbstractValidator validator = blackDuckServerConfigBuilder.createValidator();
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

    private RestConnection createRestConnection(final HubServerConfigBuilder blackDuckServerConfigBuilder) throws IntegrationException {
        final HubServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
        return blackDuckServerConfig.createRestConnection(blackDuckServerConfigBuilder.getLogger());
    }

}
