/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;

@RestController
@RequestMapping(UIComponentController.DESCRIPTOR_PATH)
@Deprecated
// FIXME this class should be deleted once the UI switches to the new endpoints
public class UIComponentController extends BaseController {
    public static final String DESCRIPTOR_PATH = BASE_PATH + "/descriptor";
    private final DescriptorMap descriptorMap;

    @Autowired
    public UIComponentController(final DescriptorMap descriptorMap) {
        this.descriptorMap = descriptorMap;
    }

    @GetMapping
    public Collection<DescriptorMetadata> getDescriptors(@RequestParam(value = "descriptorName", required = false) final String descriptorName, @RequestParam(value = "context", required = false) final String context) {
        // filter by name
        if (StringUtils.isNotBlank(descriptorName)) {
            final Optional<Descriptor> optionalDescriptor = descriptorMap.getDescriptor(descriptorName);
            if (optionalDescriptor.isPresent()) {
                // filter by type also
                final Descriptor descriptor = optionalDescriptor.get();
                if (StringUtils.isNotBlank(context)) {
                    final ConfigContextEnum contextType = EnumUtils.getEnum(ConfigContextEnum.class, context);
                    final Optional<DescriptorMetadata> uiConfig = descriptor.getMetaData(contextType);
                    if (uiConfig.isPresent()) {
                        return List.of(uiConfig.get());
                    } else {
                        return Collections.emptyList();
                    }
                } else {
                    // name only

                    return descriptor.getAllMetaData();
                }
            } else {
                return Collections.emptyList();
            }
        } else if (StringUtils.isNotBlank(context)) {
            final ConfigContextEnum contextType = EnumUtils.getEnum(ConfigContextEnum.class, context);
            return descriptorMap.getDescriptorMetadataList(contextType);
        } else {
            return descriptorMap.getAllUIComponents();
        }
    }

    // TODO split out provider and distribution endpoints
    @GetMapping("/distribution")
    public DescriptorMetadata getDistributionUIComponent(@RequestParam(value = "providerName", required = true) final String providerName, @RequestParam(value = "channelName", required = true) final String channelName) {
        if (StringUtils.isBlank(providerName) || StringUtils.isBlank(channelName)) {
            return null;
        } else {
            final Optional<ProviderDescriptor> providerDescriptor = descriptorMap.getProviderDescriptor(providerName);
            final Optional<ChannelDescriptor> channelDescriptor = descriptorMap.getChannelDescriptor(channelName);
            if (providerDescriptor.isPresent() && channelDescriptor.isPresent()) {
                final DescriptorMetadata channelDescriptorMetadata = channelDescriptor.get().getMetaData(ConfigContextEnum.DISTRIBUTION).get();
                final DescriptorMetadata providerDescriptorMetadata = providerDescriptor.get().getMetaData(ConfigContextEnum.DISTRIBUTION).get();
                final List<ConfigField> combinedFields = new ArrayList<>();
                final ConfigField name = TextInputConfigField.createRequired("name", "Name");
                final ConfigField frequency = SelectConfigField.createRequired("frequency", "Frequency", Arrays.stream(FrequencyType.values()).map(FrequencyType::getDisplayName).collect(Collectors.toList()));
                combinedFields.add(name);
                combinedFields.add(frequency);
                combinedFields.addAll(channelDescriptorMetadata.getFields());
                combinedFields.addAll(providerDescriptorMetadata.getFields());

                final DescriptorMetadata combinedDescriptorMetadata = new DescriptorMetadata(channelDescriptorMetadata.getLabel(), channelDescriptorMetadata.getUrlName(), channelDescriptorMetadata.getName(),
                    channelDescriptorMetadata.getType(),
                    channelDescriptorMetadata.getContext(), channelDescriptorMetadata.getFontAwesomeIcon(), channelDescriptorMetadata.isAutomaticallyGenerateUI(), combinedFields);
                return combinedDescriptorMetadata;
            }
        }

        return null;
    }
}
