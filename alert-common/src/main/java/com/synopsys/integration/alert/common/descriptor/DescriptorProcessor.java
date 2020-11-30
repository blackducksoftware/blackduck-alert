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
package com.synopsys.integration.alert.common.descriptor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.channel.AutoActionable;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.channel.DistributionChannel;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

@Component
public class DescriptorProcessor {
    private final DescriptorMap descriptorMap;
    private final ConfigurationAccessor configurationAccessor;
    private final Map<String, ConfigurationAction> allConfigurationActions;

    @Autowired
    public DescriptorProcessor(DescriptorMap descriptorMap, ConfigurationAccessor configurationAccessor, List<ConfigurationAction> configurationActions, List<AutoActionable> autoActionables) {
        this.descriptorMap = descriptorMap;
        this.configurationAccessor = configurationAccessor;
        this.allConfigurationActions = DataStructureUtils.mapToValues(configurationActions, action -> action.getDescriptorKey().getUniversalKey());
        for (AutoActionable autoActionable : autoActionables) {
            DistributionChannel channel = autoActionable.getChannel();
            ChannelKey channelKey = autoActionable.getChannelKey();

            ChannelDistributionTestAction channelDistributionTestAction = new ChannelDistributionTestAction(channel) {
            };
            ConfigurationAction configurationAction = new ConfigurationAction(channelKey) {
            };
            configurationAction.addDistributionTestAction(channelDistributionTestAction);
            allConfigurationActions.put(configurationAction.getDescriptorKey().getUniversalKey(), configurationAction);
        }
    }

    public Optional<TestAction> retrieveTestAction(FieldModel fieldModel) {
        ConfigContextEnum descriptorContext = EnumUtils.getEnum(ConfigContextEnum.class, fieldModel.getContext());
        return retrieveTestAction(fieldModel.getDescriptorName(), descriptorContext);
    }

    public Optional<TestAction> retrieveTestAction(String descriptorName, ConfigContextEnum context) {
        return retrieveConfigurationAction(descriptorName).map(configurationAction -> configurationAction.getTestAction(context));
    }

    public Optional<Descriptor> retrieveDescriptor(String descriptorName) {
        return descriptorMap.getDescriptorKey(descriptorName).flatMap(descriptorMap::getDescriptor);
    }

    public Optional<ConfigurationModel> getSavedEntity(Long id) {
        if (null != id) {
            return configurationAccessor.getConfigurationById(id);
        }
        return Optional.empty();
    }

    public Optional<ApiAction> retrieveApiAction(FieldModel fieldModel) {
        return retrieveApiAction(fieldModel.getDescriptorName(), fieldModel.getContext());
    }

    public Optional<ApiAction> retrieveApiAction(String descriptorName, String context) {
        ConfigContextEnum descriptorContext = EnumUtils.getEnum(ConfigContextEnum.class, context);
        return retrieveConfigurationAction(descriptorName).map(configurationAction -> configurationAction.getApiAction(descriptorContext));
    }

    public Optional<ConfigurationAction> retrieveConfigurationAction(String descriptorName) {
        return Optional.ofNullable(allConfigurationActions.get(descriptorName));
    }

    public List<ConfigField> retrieveUIConfigFields(String context, String descriptorName) {
        ConfigContextEnum descriptorContext = EnumUtils.getEnum(ConfigContextEnum.class, context);
        return retrieveUIConfigFields(descriptorContext, descriptorName);
    }

    public List<ConfigField> retrieveUIConfigFields(ConfigContextEnum context, String descriptorName) {
        Optional<Descriptor> optionalDescriptor = retrieveDescriptor(descriptorName);
        List<ConfigField> fieldsToReturn = new LinkedList<>();
        if (optionalDescriptor.isPresent()) {
            Descriptor descriptor = optionalDescriptor.get();
            Optional<UIConfig> uiConfig = descriptor.getUIConfig(context);
            fieldsToReturn.addAll(uiConfig.map(UIConfig::getFields).orElse(List.of()));
        }
        return fieldsToReturn;
    }

}
