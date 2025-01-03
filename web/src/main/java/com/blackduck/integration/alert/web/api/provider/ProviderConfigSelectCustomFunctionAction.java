/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.provider;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.action.CustomFunctionAction;
import com.blackduck.integration.alert.common.descriptor.DescriptorMap;
import com.blackduck.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.blackduck.integration.alert.common.descriptor.config.field.LabelValueSelectOptions;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.rest.HttpServletContentWrapper;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;

@Component
public class ProviderConfigSelectCustomFunctionAction extends CustomFunctionAction<LabelValueSelectOptions> {
    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final DescriptorMap descriptorMap;

    @Autowired
    public ProviderConfigSelectCustomFunctionAction(AuthorizationManager authorizationManager, ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor, DescriptorMap descriptorMap) {
        super(authorizationManager);
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.descriptorMap = descriptorMap;
    }

    @Override
    public ActionResponse<LabelValueSelectOptions> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        String providerName = fieldModel.getDescriptorName();
        Optional<DescriptorKey> descriptorKey = descriptorMap.getDescriptorKey(providerName);
        List<LabelValueSelectOption> options = List.of();
        if (descriptorKey.isPresent()) {
            List<ConfigurationModel> configurationModels = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(descriptorKey.get(), ConfigContextEnum.GLOBAL);
            options = configurationModels.stream()
                .map(this::createNameToIdOption)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        }
        LabelValueSelectOptions optionList = new LabelValueSelectOptions(options);
        return new ActionResponse<>(HttpStatus.OK, optionList);
    }

    @Override
    protected Collection<AlertFieldStatus> validateRelatedFields(FieldModel fieldModel) {
        return Set.of();
    }

    private Optional<LabelValueSelectOption> createNameToIdOption(ConfigurationModel configurationModel) {
        return configurationModel.getField(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME)
            .flatMap(ConfigurationFieldModel::getFieldValue)
            .map(providerConfigName -> new LabelValueSelectOption(providerConfigName, configurationModel.getConfigurationId().toString()));
    }

}
