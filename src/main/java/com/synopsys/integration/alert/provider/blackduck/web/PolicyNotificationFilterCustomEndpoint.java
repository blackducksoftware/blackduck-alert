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
package com.synopsys.integration.alert.provider.blackduck.web;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.TableSelectCustomEndpoint;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.provider.blackduck.factories.BlackDuckPropertiesFactory;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.PolicyRuleService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class PolicyNotificationFilterCustomEndpoint extends TableSelectCustomEndpoint {
    private final Logger logger = LoggerFactory.getLogger(PolicyNotificationFilterCustomEndpoint.class);
    private final BlackDuckPropertiesFactory blackDuckPropertiesFactory;
    private final ConfigurationFieldModelConverter fieldModelConverter;
    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    protected PolicyNotificationFilterCustomEndpoint(CustomEndpointManager customEndpointManager, ResponseFactory responseFactory,
        Gson gson, BlackDuckPropertiesFactory blackDuckPropertiesFactory, ConfigurationFieldModelConverter fieldModelConverter, ConfigurationAccessor configurationAccessor) throws AlertException {
        super(BlackDuckDescriptor.KEY_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER, customEndpointManager, responseFactory, gson);
        this.blackDuckPropertiesFactory = blackDuckPropertiesFactory;
        this.fieldModelConverter = fieldModelConverter;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    protected List<?> createData(FieldModel fieldModel) throws AlertException {
        Optional<FieldValueModel> fieldValueModel = fieldModel.getFieldValueModel(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES);
        Collection<String> selectedNotificationTypes = fieldValueModel.map(FieldValueModel::getValues).orElse(List.of());
        if (selectedNotificationTypes.isEmpty()) {
            return List.of();
        }

        if (isFilterablePolicy(selectedNotificationTypes)) {
            try {
                return retrieveBlackDuckPolicyOptions(fieldModel);
            } catch (IntegrationException e) {
                logger.error("There was an issue communicating with Black Duck");
                logger.debug(e.getMessage(), e);
                throw new AlertException("Unable to communicate with Black Duck.", e);
            }
        }
        return List.of();
    }

    private boolean isFilterablePolicy(Collection<String> notificationTypes) {
        Set<String> filterableNotificationType = Set.of(
            NotificationType.POLICY_OVERRIDE,
            NotificationType.RULE_VIOLATION,
            NotificationType.RULE_VIOLATION_CLEARED
        ).stream().map(NotificationType::name).collect(Collectors.toSet());
        return notificationTypes.stream().anyMatch(filterableNotificationType::contains);
    }

    private List<NotificationFilterModel> retrieveBlackDuckPolicyOptions(FieldModel fieldModel) throws IntegrationException {
        Optional<BlackDuckProperties> blackDuckProperties = createProviderFieldAccessor(fieldModel);
        Optional<BlackDuckHttpClient> blackDuckHttpClient = blackDuckProperties.flatMap(this::createHttpClient);
        if (blackDuckProperties.isPresent() && blackDuckHttpClient.isPresent()) {
            BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.get().createBlackDuckServicesFactory(blackDuckHttpClient.get(), new Slf4jIntLogger(logger));
            PolicyRuleService policyRuleService = blackDuckServicesFactory.createPolicyRuleService();
            return policyRuleService.getAllPolicyRules()
                       .stream()
                       .map(policyRuleView -> new NotificationFilterModel(policyRuleView.getName()))
                       .collect(Collectors.toList());
        }

        return List.of();
    }

    private Optional<BlackDuckHttpClient> createHttpClient(BlackDuckProperties blackDuckProperties) {
        try {
            return blackDuckProperties.createBlackDuckHttpClient(logger);
        } catch (IntegrationException ex) {
            logger.error("Error creating Black Duck http client", ex);
            return Optional.empty();
        }
    }

    private Optional<BlackDuckProperties> createProviderFieldAccessor(FieldModel fieldModel) throws IntegrationException {
        FieldAccessor fieldAccessor = fieldModelConverter.convertToFieldAccessor(fieldModel);
        Optional<ConfigurationModel> configurationModel = configurationAccessor.getProviderConfigurationByName(fieldAccessor.getStringOrNull(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME));
        Optional<FieldAccessor> providerFieldAccessor = configurationModel
                                                            .map(ConfigurationModel::getCopyOfKeyToFieldMap)
                                                            .map(FieldAccessor::new);

        if (configurationModel.isPresent() && providerFieldAccessor.isPresent()) {
            ConfigurationModel configModel = configurationModel.get();
            return Optional.of(blackDuckPropertiesFactory.createProperties(configModel.getConfigurationId(), providerFieldAccessor.get()));
        }

        return Optional.empty();
    }

}
