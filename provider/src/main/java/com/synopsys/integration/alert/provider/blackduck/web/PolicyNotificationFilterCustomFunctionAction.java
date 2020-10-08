/**
 * provider
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.CustomFunctionAction;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.FieldValidationUtility;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.provider.blackduck.factories.BlackDuckPropertiesFactory;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.PolicyRuleService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class PolicyNotificationFilterCustomFunctionAction extends CustomFunctionAction<NotificationFilterModelOptions> {
    private final Logger logger = LoggerFactory.getLogger(PolicyNotificationFilterCustomFunctionAction.class);
    private final BlackDuckPropertiesFactory blackDuckPropertiesFactory;
    private final ConfigurationFieldModelConverter fieldModelConverter;
    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    protected PolicyNotificationFilterCustomFunctionAction(AuthorizationManager authorizationManager, BlackDuckPropertiesFactory blackDuckPropertiesFactory, ConfigurationFieldModelConverter fieldModelConverter,
        ConfigurationAccessor configurationAccessor, DescriptorMap descriptorMap, FieldValidationUtility fieldValidationUtility) {
        super(BlackDuckDescriptor.KEY_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER, authorizationManager, descriptorMap, fieldValidationUtility);
        this.blackDuckPropertiesFactory = blackDuckPropertiesFactory;
        this.fieldModelConverter = fieldModelConverter;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public ActionResponse<NotificationFilterModelOptions> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) throws IntegrationException {
        Optional<FieldValueModel> fieldValueModel = fieldModel.getFieldValueModel(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES);
        Collection<String> selectedNotificationTypes = fieldValueModel.map(FieldValueModel::getValues).orElse(List.of());
        List<NotificationFilterModel> options = List.of();

        if (isFilterablePolicy(selectedNotificationTypes)) {
            try {
                options = retrieveBlackDuckPolicyOptions(fieldModel);
            } catch (IntegrationException e) {
                logger.error("There was an issue communicating with Black Duck");
                logger.debug(e.getMessage(), e);
                throw new AlertException("Unable to communicate with Black Duck.", e);
            }
        }
        NotificationFilterModelOptions notificationFilterModelOptions = new NotificationFilterModelOptions(options);
        return new ActionResponse<>(HttpStatus.OK, notificationFilterModelOptions);
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
        Optional<PolicyRuleService> optionalPolicyRuleService = createPolicyRuleService(fieldModel);
        if (optionalPolicyRuleService.isPresent()) {
            return optionalPolicyRuleService.get()
                       .getAllPolicyRules()
                       .stream()
                       .filter(PolicyRuleView::getEnabled)
                       .map(PolicyRuleView::getName)
                       .map(NotificationFilterModel::new)
                       .collect(Collectors.toList());
        }
        return List.of();
    }

    private Optional<PolicyRuleService> createPolicyRuleService(FieldModel fieldModel) throws IntegrationException {
        Optional<BlackDuckProperties> optionalBlackDuckProperties = createBlackDuckProperties(fieldModel);
        if (optionalBlackDuckProperties.isPresent()) {
            Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
            BlackDuckProperties blackDuckProperties = optionalBlackDuckProperties.get();
            return createHttpClient(blackDuckProperties)
                       .map(client -> blackDuckProperties.createBlackDuckServicesFactory(client, intLogger))
                       .map(BlackDuckServicesFactory::createPolicyRuleService);
        }
        return Optional.empty();
    }

    private Optional<BlackDuckHttpClient> createHttpClient(BlackDuckProperties blackDuckProperties) {
        try {
            return blackDuckProperties.createBlackDuckHttpClient(logger);
        } catch (IntegrationException ex) {
            logger.error("Error creating Black Duck http client", ex);
            return Optional.empty();
        }
    }

    private Optional<BlackDuckProperties> createBlackDuckProperties(FieldModel fieldModel) throws IntegrationException {
        FieldUtility fieldUtility = fieldModelConverter.convertToFieldAccessor(fieldModel);
        Long providerConfigId = fieldUtility.getLong(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID).orElse(null);
        if (null == providerConfigId) {
            return Optional.empty();
        }
        return configurationAccessor.getConfigurationById(providerConfigId)
                   .map(ConfigurationModel::getCopyOfKeyToFieldMap)
                   .map(FieldUtility::new)
                   .map(accessor -> blackDuckPropertiesFactory.createProperties(providerConfigId, accessor));
    }

}
