/**
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.PagedCustomFunctionAction;
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
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.blackduck.http.BlackDuckPageDefinition;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;

@Component
public class PolicyNotificationFilterCustomFunctionAction extends PagedCustomFunctionAction<NotificationFilterModelOptions> {
    private final IntLogger logger = new Slf4jIntLogger(LoggerFactory.getLogger(getClass()));
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
    public ActionResponse<NotificationFilterModelOptions> createPagedActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper, int pageNumber, int pageSize, String searchTerm) throws IntegrationException {
        Optional<FieldValueModel> fieldValueModel = fieldModel.getFieldValueModel(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES);
        Collection<String> selectedNotificationTypes = fieldValueModel.map(FieldValueModel::getValues).orElse(List.of());

        int totalPages = 1;
        List<NotificationFilterModel> options = List.of();

        if (isJobFilterableByPolicy(selectedNotificationTypes)) {
            try {
                Optional<BlackDuckServicesFactory> blackDuckServicesFactory = createBlackDuckServicesFactory(fieldModel);
                if (blackDuckServicesFactory.isPresent()) {
                    BlackDuckPageResponse<PolicyRuleView> policyRulesPage = retrievePolicyRules(blackDuckServicesFactory.get(), pageNumber, pageSize, searchTerm);
                    totalPages = (policyRulesPage.getTotalCount() + (pageSize - 1)) / pageSize;
                    options = convertToNotificationFilterModel(policyRulesPage.getItems());
                }
            } catch (IntegrationException e) {
                logger.errorAndDebug("There was an issue communicating with Black Duck. " + e.getMessage(), e);
                throw new AlertException("Unable to communicate with Black Duck.", e);
            }
        }

        NotificationFilterModelOptions notificationFilterModelOptions = new NotificationFilterModelOptions(totalPages, pageNumber, pageSize, options);
        return new ActionResponse<>(HttpStatus.OK, notificationFilterModelOptions);
    }

    private boolean isJobFilterableByPolicy(Collection<String> notificationTypes) {
        Set<String> filterableNotificationType = Set.of(
            NotificationType.POLICY_OVERRIDE,
            NotificationType.RULE_VIOLATION,
            NotificationType.RULE_VIOLATION_CLEARED
        ).stream().map(NotificationType::name).collect(Collectors.toSet());
        return notificationTypes.stream().anyMatch(filterableNotificationType::contains);
    }

    private BlackDuckPageResponse<PolicyRuleView> retrievePolicyRules(BlackDuckServicesFactory blackDuckServicesFactory, int pageNumber, int pageSize, String searchTerm) throws IntegrationException {
        BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckApiClient();

        HttpUrl policyRulesUrl = blackDuckApiClient.getUrl(ApiDiscovery.POLICY_RULES_LINK);
        BlackDuckRequestBuilder requestBuilder = new BlackDuckRequestBuilder(new Request.Builder())
                                                     .url(policyRulesUrl)
                                                     .addQueryParameter("q", "name:" + searchTerm)
                                                     .addQueryParameter("filter", "policyRuleEnabled:true");
        BlackDuckPageDefinition blackDuckPageDefinition = new BlackDuckPageDefinition(pageSize, pageNumber * pageSize);
        return blackDuckApiClient.getPageResponse(requestBuilder, PolicyRuleView.class, blackDuckPageDefinition);
    }

    private List<NotificationFilterModel> convertToNotificationFilterModel(List<PolicyRuleView> policyRules) {
        return policyRules.stream()
                   .map(PolicyRuleView::getName)
                   .map(NotificationFilterModel::new)
                   .collect(Collectors.toList());
    }

    private Optional<BlackDuckServicesFactory> createBlackDuckServicesFactory(FieldModel fieldModel) throws IntegrationException {
        Optional<BlackDuckProperties> optionalBlackDuckProperties = createBlackDuckProperties(fieldModel);
        if (optionalBlackDuckProperties.isPresent()) {
            BlackDuckProperties blackDuckProperties = optionalBlackDuckProperties.get();
            BlackDuckHttpClient blackDuckHttpClient = blackDuckProperties.createBlackDuckHttpClient(logger);
            BlackDuckServicesFactory blackDuckServicesFactory = blackDuckProperties.createBlackDuckServicesFactory(blackDuckHttpClient, logger);
            return Optional.of(blackDuckServicesFactory);
        }
        return Optional.empty();
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
