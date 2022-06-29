/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class GlobalConfigExistsValidator {
    public static final String GLOBAL_CONFIG_MISSING = "%s global configuration missing.";

    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final List<Descriptor> descriptors;
    private final Map<DescriptorKey, ConcreteGlobalConfigExistsValidator> concreteGlobalConfigModelValidatorMap;

    @Autowired
    public GlobalConfigExistsValidator(
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor,
        List<Descriptor> descriptors,
        List<ConcreteGlobalConfigExistsValidator> concreteGlobalConfigExistsValidators
    ) {
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.descriptors = descriptors;
        this.concreteGlobalConfigModelValidatorMap = concreteGlobalConfigExistsValidators.stream()
            .collect(Collectors.toMap(ConcreteGlobalConfigExistsValidator::getDescriptorKey, Function.identity()));
    }

    /**
     * @return An Optional<String> containing the error message.
     */
    public Optional<String> validate(String descriptorName) {
        if (StringUtils.isBlank(descriptorName)) {
            return Optional.empty();
        }

        Optional<DescriptorKey> optionalDescriptorKey = descriptors
            .stream()
            .filter(desc -> desc.getDescriptorKey().getUniversalKey().equals(descriptorName))
            .filter(desc -> desc.getConfigContexts().contains(ConfigContextEnum.GLOBAL))
            .map(Descriptor::getDescriptorKey)
            .findFirst();
        if (optionalDescriptorKey.isEmpty()) {
            return Optional.empty();
        }

        if (doesValidGlobalModelExists(descriptorName, optionalDescriptorKey.get())) {
            return Optional.empty();
        }

        String descriptorDisplayName = optionalDescriptorKey
            .map(DescriptorKey::getDisplayName)
            .orElse(descriptorName);
        return Optional.of(String.format(GLOBAL_CONFIG_MISSING, descriptorDisplayName));
    }

    private boolean doesValidGlobalModelExists(String descriptorName, DescriptorKey descriptorKey) {
        if (concreteGlobalConfigModelValidatorMap.containsKey(descriptorKey)) {
            ConcreteGlobalConfigExistsValidator validator = concreteGlobalConfigModelValidatorMap.get(descriptorKey);
            return validator.exists();
        }

        List<ConfigurationModel> configurations = configurationModelConfigurationAccessor.getConfigurationsByDescriptorNameAndContext(descriptorName, ConfigContextEnum.GLOBAL);
        return !configurations
            .stream()
            .allMatch(ConfigurationModel::isConfiguredFieldsEmpty);
    }
}
