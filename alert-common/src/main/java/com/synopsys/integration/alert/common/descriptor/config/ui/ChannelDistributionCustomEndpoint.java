/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.SelectCustomEndpoint;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class ChannelDistributionCustomEndpoint {
    private DescriptorMap descriptorMap;

    @Autowired
    public ChannelDistributionCustomEndpoint(CustomEndpointManager customEndpointManager, DescriptorMap descriptorMap, ResponseFactory responseFactory, Gson gson) throws AlertException {
        this.descriptorMap = descriptorMap;

        new ProvidersFunction(customEndpointManager, responseFactory, gson);
        new ChannelFunction(customEndpointManager, responseFactory, gson);
    }

    class ProvidersFunction extends SelectCustomEndpoint {
        protected ProvidersFunction(CustomEndpointManager customEndpointManager, ResponseFactory responseFactory, Gson gson) throws AlertException {
            super(ChannelDistributionUIConfig.KEY_PROVIDER_NAME, customEndpointManager, responseFactory, gson);
        }

        @Override
        protected List<LabelValueSelectOption> createData(Map<String, FieldValueModel> fieldValueModels) throws AlertException {
            return descriptorMap.getDescriptorByType(DescriptorType.PROVIDER).stream()
                       .map(descriptor -> descriptor.createMetaData(ConfigContextEnum.DISTRIBUTION))
                       .flatMap(Optional::stream)
                       .map(descriptorMetadata -> new LabelValueSelectOption(descriptorMetadata.getLabel(), descriptorMetadata.getName()))
                       .sorted()
                       .collect(Collectors.toList());
        }
    }

    class ChannelFunction extends SelectCustomEndpoint {
        protected ChannelFunction(CustomEndpointManager customEndpointManager, ResponseFactory responseFactory, Gson gson) throws AlertException {
            super(ChannelDistributionUIConfig.KEY_CHANNEL_NAME, customEndpointManager, responseFactory, gson);
        }

        @Override
        protected List<LabelValueSelectOption> createData(Map<String, FieldValueModel> fieldValueModels) throws AlertException {
            return descriptorMap.getDescriptorByType(DescriptorType.CHANNEL).stream()
                       .map(descriptor -> descriptor.getUIConfig(ConfigContextEnum.DISTRIBUTION))
                       .flatMap(Optional::stream)
                       .map(uiConfig -> (ChannelDistributionUIConfig) uiConfig)
                       .map(channelDistributionUIConfig -> new LabelValueSelectOption(channelDistributionUIConfig.getLabel(), channelDistributionUIConfig.getChannelKey().getUniversalKey()))
                       .sorted()
                       .collect(Collectors.toList());
        }
    }

}
