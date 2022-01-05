/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

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
