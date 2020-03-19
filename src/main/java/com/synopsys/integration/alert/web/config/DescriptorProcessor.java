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
package com.synopsys.integration.alert.web.config;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class DescriptorProcessor {
    private final DescriptorMap descriptorMap;
    private final ConfigurationAccessor configurationAccessor;
    private final List<ConfigurationAction> allConfigurationActions;

    @Autowired
    public DescriptorProcessor(final DescriptorMap descriptorMap, ConfigurationAccessor configurationAccessor, final List<ConfigurationAction> allConfigurationActions) {
        this.descriptorMap = descriptorMap;
        this.configurationAccessor = configurationAccessor;
        this.allConfigurationActions = allConfigurationActions;
    }

    public Optional<TestAction> retrieveTestAction(final FieldModel fieldModel) {
        final ConfigContextEnum descriptorContext = EnumUtils.getEnum(ConfigContextEnum.class, fieldModel.getContext());
        return retrieveTestAction(fieldModel.getDescriptorName(), descriptorContext);
    }

    public Optional<TestAction> retrieveTestAction(final String descriptorName, final ConfigContextEnum context) {
        return retrieveConfigurationAction(descriptorName).map(configurationAction -> configurationAction.getTestAction(context));
    }

    public Optional<Descriptor> retrieveDescriptor(final String descriptorName) {
        return descriptorMap.getDescriptor(descriptorName);
    }

    public Optional<ConfigurationModel> getSavedEntity(final Long id) throws AlertException {
        if (null != id) {
            return configurationAccessor.getConfigurationById(id);
        }
        return Optional.empty();
    }

    public Optional<ApiAction> retrieveApiAction(final FieldModel fieldModel) {
        return retrieveApiAction(fieldModel.getDescriptorName(), fieldModel.getContext());
    }

    public Optional<ApiAction> retrieveApiAction(final String descriptorName, final String context) {
        final ConfigContextEnum descriptorContext = EnumUtils.getEnum(ConfigContextEnum.class, context);
        return retrieveConfigurationAction(descriptorName).map(configurationAction -> configurationAction.getApiAction(descriptorContext));
    }

    public Optional<ConfigurationAction> retrieveConfigurationAction(final String descriptorName) {
        return allConfigurationActions.stream()
                   .filter(configurationAction -> configurationAction.getDescriptorName().equals(descriptorName))
                   .findFirst();
    }

    public List<ConfigField> retrieveUIConfigFields(final String context, final String descriptorName) {
        final ConfigContextEnum descriptorContext = EnumUtils.getEnum(ConfigContextEnum.class, context);
        return retrieveUIConfigFields(descriptorContext, descriptorName);
    }

    public List<ConfigField> retrieveUIConfigFields(final ConfigContextEnum context, final String descriptorName) {
        final Optional<Descriptor> optionalDescriptor = retrieveDescriptor(descriptorName);
        final List<ConfigField> fieldsToReturn = new LinkedList<>();
        if (optionalDescriptor.isPresent()) {
            final Descriptor descriptor = optionalDescriptor.get();
            final Optional<UIConfig> uiConfig = descriptor.getUIConfig(context);
            fieldsToReturn.addAll(uiConfig.map(UIConfig::createFields).orElse(List.of()));
        }
        return fieldsToReturn;
    }

}
