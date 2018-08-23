/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
import com.synopsys.integration.alert.common.descriptor.config.DescriptorConfig;
import com.synopsys.integration.alert.common.descriptor.config.UIComponent;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.enumeration.DescriptorConfigType;
import com.synopsys.integration.alert.common.enumeration.DigestType;

@RestController
@RequestMapping(DescriptorController.DESCRIPTOR_PATH)
public class DescriptorController extends BaseController {
    public static final String DESCRIPTOR_PATH = BASE_PATH + "/descriptor";

    private final DescriptorMap descriptorMap;

    @Autowired
    public DescriptorController(final DescriptorMap descriptorMap) {
        this.descriptorMap = descriptorMap;
    }

    @GetMapping
    public List<UIComponent> getDescriptors(@RequestParam(value = "descriptorName", required = false) final String descriptorName, @RequestParam(value = "descriptorType", required = false) final String descriptorConfigType) {
        // filter by name
        if (StringUtils.isNotBlank(descriptorName)) {
            final Descriptor descriptor = descriptorMap.getDescriptor(descriptorName);
            if (descriptor != null) {
                // filter by type also
                if (StringUtils.isNotBlank(descriptorConfigType)) {
                    final DescriptorConfigType descriptorConfigTypeEnum = getDescriptorConfigType(descriptorConfigType);
                    final DescriptorConfig descriptorConfig = descriptor.getConfig(descriptorConfigTypeEnum);
                    if (descriptorConfig != null) {
                        return Arrays.asList(descriptorConfig.getUiComponent());
                    } else {
                        return Collections.emptyList();
                    }
                } else {
                    // name only
                    return descriptor.getAllConfigs().stream().map(descriptorConfig -> descriptorConfig.getUiComponent()).collect(Collectors.toList());
                }
            } else {
                return Collections.emptyList();
            }
        } else if (StringUtils.isNotBlank(descriptorConfigType)) {
            final DescriptorConfigType descriptorConfigTypeEnum = getDescriptorConfigType(descriptorConfigType);
            return descriptorMap.getDescriptorConfigs(descriptorConfigTypeEnum).stream().map(descriptorConfig -> descriptorConfig.getUiComponent()).collect(Collectors.toList());
        } else {
            return descriptorMap.getAllDescriptorConfigs()
                   .stream()
                   .map(descriptorConfig -> descriptorConfig.getUiComponent())
                   .collect(Collectors.toList());
        }
    }

    @GetMapping("/distribution")
    public UIComponent getDistributionDescriptor(@RequestParam(value = "providerName", required = true) final String providerName, @RequestParam(value = "channelName", required = true) final String channelName) {
        final ProviderDescriptor providerDescriptor = descriptorMap.getProviderDescriptor(providerName);
        final ChannelDescriptor channelDescriptor = descriptorMap.getChannelDescriptor(channelName);
        final DescriptorConfig channelDescriptorConfig = channelDescriptor.getConfig(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG);
        final UIComponent channelUIComponent = channelDescriptorConfig.getUiComponent();

        final List<ConfigField> combinedFields = new ArrayList<>();
        final ConfigField name = new TextInputConfigField("name", "Name", true, false);
        final ConfigField frequency = new SelectConfigField("frequency", "Digest type", true, false, Arrays.stream(DigestType.values()).map(type -> type.getDisplayName()).collect(Collectors.toList()));
        final ConfigField notificationTypes = new SelectConfigField("notificationTypes", "Notification Types", true, false, providerDescriptor.getNotificationTypes().stream().collect(Collectors.toList()));
        combinedFields.add(name);
        combinedFields.add(frequency);
        combinedFields.add(notificationTypes);
        combinedFields.addAll(channelUIComponent.getFields());

        final UIComponent combinedUIComponent = new UIComponent(channelUIComponent.getLabel(), channelUIComponent.getUrlName(), channelDescriptor.getName(), channelUIComponent.getFontAwesomeIcon(), combinedFields);
        return combinedUIComponent;
    }

    private DescriptorConfigType getDescriptorConfigType(final String descriptorConfigType) {
        try {
            return Enum.valueOf(DescriptorConfigType.class, descriptorConfigType);
        } catch (final IllegalArgumentException ex) {
            return null;
        }

    }
}
