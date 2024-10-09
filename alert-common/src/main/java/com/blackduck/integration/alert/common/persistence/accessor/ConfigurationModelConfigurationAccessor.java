/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.DescriptorType;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;

@Deprecated(forRemoval = true)
public interface ConfigurationModelConfigurationAccessor {
    Optional<ConfigurationModel> getProviderConfigurationByName(String providerConfigName);

    Optional<ConfigurationModel> getConfigurationById(Long id);

    List<ConfigurationModel> getConfigurationsByDescriptorKey(DescriptorKey descriptorKey);

    List<ConfigurationModel> getConfigurationsByDescriptorType(DescriptorType descriptorType);

    List<ConfigurationModel> getConfigurationsByDescriptorNameAndContext(String descriptorName, ConfigContextEnum context);

    List<ConfigurationModel> getConfigurationsByDescriptorKeyAndContext(DescriptorKey descriptorKey, ConfigContextEnum context);

    ConfigurationModel createConfiguration(DescriptorKey descriptorKey, ConfigContextEnum context, Collection<ConfigurationFieldModel> configuredFields);

    ConfigurationModel updateConfiguration(Long descriptorConfigId, Collection<ConfigurationFieldModel> configuredFields) throws AlertConfigurationException;

    void deleteConfiguration(ConfigurationModel configModel);

    void deleteConfiguration(Long descriptorConfigId);

}
