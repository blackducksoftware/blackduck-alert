/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.provider;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.CustomFunctionAction;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOptions;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

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
