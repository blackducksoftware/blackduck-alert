/**
 * alert-common
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
package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.endpoint.SelectCustomEndpoint;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class ProviderConfigSelectCustomEndpoint extends SelectCustomEndpoint {
    private final ConfigurationAccessor configurationAccessor;
    private final DescriptorMap descriptorMap;

    @Autowired
    public ProviderConfigSelectCustomEndpoint(ConfigurationAccessor configurationAccessor, DescriptorMap descriptorMap) {
        this.configurationAccessor = configurationAccessor;
        this.descriptorMap = descriptorMap;
    }

    @Override
    protected List<LabelValueSelectOption> createData(FieldModel fieldModel) throws AlertException {
        String providerName = fieldModel.getDescriptorName();
        Optional<DescriptorKey> descriptorKey = descriptorMap.getDescriptorKey(providerName);
        if (descriptorKey.isPresent()) {
            List<ConfigurationModel> configurationModels = configurationAccessor.getConfigurationsByDescriptorKeyAndContext(descriptorKey.get(), ConfigContextEnum.GLOBAL);
            return configurationModels.stream()
                       .map(ConfigurationModel::getCopyOfKeyToFieldMap)
                       .map(FieldAccessor::new)
                       .map(accessor -> accessor.getString(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME))
                       .flatMap(Optional::stream)
                       .map(LabelValueSelectOption::new)
                       .collect(Collectors.toList());
        }
        return List.of();
    }
}
