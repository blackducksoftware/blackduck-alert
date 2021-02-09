/**
 * web
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
package com.synopsys.integration.alert.web.api.provider;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.CustomFunctionAction;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOptions;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.FieldValidationUtility;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class ProviderConfigSelectCustomFunctionAction extends CustomFunctionAction<LabelValueSelectOptions> {
    private final ConfigurationAccessor configurationAccessor;
    private final DescriptorMap descriptorMap;

    @Autowired
    public ProviderConfigSelectCustomFunctionAction(AuthorizationManager authorizationManager, ConfigurationAccessor configurationAccessor, DescriptorMap descriptorMap,
        FieldValidationUtility fieldValidationUtility) {
        super(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, authorizationManager, descriptorMap, fieldValidationUtility);
        this.configurationAccessor = configurationAccessor;
        this.descriptorMap = descriptorMap;
    }

    @Override
    public ActionResponse<LabelValueSelectOptions> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) throws AlertDatabaseConstraintException {
        String providerName = fieldModel.getDescriptorName();
        Optional<DescriptorKey> descriptorKey = descriptorMap.getDescriptorKey(providerName);
        List<LabelValueSelectOption> options = List.of();
        if (descriptorKey.isPresent()) {
            List<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(descriptorKey.get(), ConfigContextEnum.GLOBAL);
            options = configurationModels.stream()
                          .map(this::createNameToIdOption)
                          .flatMap(Optional::stream)
                          .collect(Collectors.toList());
        }
        LabelValueSelectOptions optionList = new LabelValueSelectOptions(options);
        return new ActionResponse<>(HttpStatus.OK, optionList);
    }

    private Optional<LabelValueSelectOption> createNameToIdOption(ConfigurationModel configurationModel) {
        return configurationModel.getField(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME)
                   .flatMap(ConfigurationFieldModel::getFieldValue)
                   .map(providerConfigName -> new LabelValueSelectOption(providerConfigName, configurationModel.getConfigurationId().toString()));
    }

}
