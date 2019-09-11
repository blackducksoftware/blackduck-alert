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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class ChannelDistributionCustomEndpoint {
    private DescriptorMap descriptorMap;
    private ResponseFactory responseFactory;
    private Gson gson;

    @Autowired
    public ChannelDistributionCustomEndpoint(CustomEndpointManager customEndpointManager, DescriptorMap descriptorMap, ResponseFactory responseFactory, Gson gson) throws AlertException {
        this.descriptorMap = descriptorMap;
        this.responseFactory = responseFactory;
        this.gson = gson;

        customEndpointManager.registerFunction(ChannelDistributionUIConfig.KEY_PROVIDER_NAME, this::retrieveProviders);
        customEndpointManager.registerFunction(ChannelDistributionUIConfig.KEY_CHANNEL_NAME, this::retrieveChannels);
    }

    private ResponseEntity<String> retrieveProviders(Map<String, FieldValueModel> fieldValues) {
        final List<LabelValueSelectOption> providerOptions = descriptorMap.getDescriptorByType(DescriptorType.PROVIDER).stream()
                                                                 .map(descriptor -> descriptor.createMetaData(ConfigContextEnum.DISTRIBUTION))
                                                                 .flatMap(Optional::stream)
                                                                 .map(descriptorMetadata -> new LabelValueSelectOption(descriptorMetadata.getLabel(), descriptorMetadata.getName(), descriptorMetadata.getFontAwesomeIcon()))
                                                                 .sorted()
                                                                 .collect(Collectors.toList());
        String providerOptionsConverted = gson.toJson(providerOptions);
        return responseFactory.createOkContentResponse(providerOptionsConverted);
    }

    private ResponseEntity<String> retrieveChannels(Map<String, FieldValueModel> fieldValues) {
        List<LabelValueSelectOption> channelOptions = descriptorMap.getDescriptorByType(DescriptorType.CHANNEL).stream()
                                                          .map(descriptor -> descriptor.getUIConfig(ConfigContextEnum.DISTRIBUTION))
                                                          .flatMap(Optional::stream)
                                                          .map(uiConfig -> (ChannelDistributionUIConfig) uiConfig)
                                                          .map(channelDistributionUIConfig -> new LabelValueSelectOption(channelDistributionUIConfig.getLabel(), channelDistributionUIConfig.getChannelKey().getUniversalKey(),
                                                              channelDistributionUIConfig.getFontAwesomeIcon()))
                                                          .sorted()
                                                          .collect(Collectors.toList());
        String channelOptionsConverted = gson.toJson(channelOptions);
        return responseFactory.createOkContentResponse(channelOptionsConverted);
    }

}
