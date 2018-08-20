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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.synopsys.integration.alert.common.descriptor.config.field.DropDownConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.enumeration.DescriptorConfigType;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
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
    public Collection<Descriptor> getDescriptors(@RequestParam(value = "descriptorName", required = false) final String descriptorName, @RequestParam(value = "descriptorType", required = false) final String descriptorType) {
        if (StringUtils.isNotBlank(descriptorName)) {
            return Arrays.asList(descriptorMap.getDescriptor(descriptorName));
        }

        if (StringUtils.isNotBlank(descriptorType)) {
            final DescriptorType descriptorTypeEnum = Enum.valueOf(DescriptorType.class, descriptorType);
            return descriptorMap.getDescriptorMap().values()
                    .stream()
                    .filter(descriptor -> descriptorTypeEnum.equals(descriptor.getType()))
                    .collect(Collectors.toList());
        }

        return descriptorMap.getDescriptorMap().values();

    }

    @GetMapping("descriptorConfig/{descriptorConfigType}")
    public Collection<UIComponent> getUIComponents(@RequestParam(value = "descriptorName", required = false) final String descriptorName, @PathVariable final String descriptorConfigType) {
        final DescriptorConfigType descriptorConfigTypeEnum = Enum.valueOf(DescriptorConfigType.class, descriptorConfigType);
        if (StringUtils.isNotBlank(descriptorName)) {
            return Arrays.asList(descriptorMap.getDescriptor(descriptorName).getConfig(descriptorConfigTypeEnum).getUiComponent());
        }

        return descriptorMap.getDescriptorConfigs(descriptorConfigTypeEnum)
                .stream()
                .map(descriptorConfig -> descriptorConfig.getUiComponent())
                .collect(Collectors.toList());
    }

    @GetMapping("/distribution")
    public UIComponent getDistributionDescriptor(@RequestParam(value = "providerName", required = true) final String providerName, @RequestParam(value = "channelName", required = true) final String channelName) {
        final ProviderDescriptor providerDescriptor = descriptorMap.getProviderDescriptor(providerName);
        final ChannelDescriptor channelDescriptor = descriptorMap.getChannelDescriptor(channelName);
        final DescriptorConfig channelDescriptorConfig = channelDescriptor.getConfig(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG);
        final UIComponent channelUIComponent = channelDescriptorConfig.getUiComponent();

        final List<ConfigField> combinedFields = new ArrayList<>();
        final ConfigField name = new TextInputConfigField("name", "Name", true, false);
        final ConfigField frequency = new DropDownConfigField("frequency", "Digest type", true, false, Arrays.stream(DigestType.values()).map(type -> type.getDisplayName()).collect(Collectors.toList()));
        final ConfigField notificationTypes = new DropDownConfigField("notificationTypes", "Notification Types", true, false, providerDescriptor.getNotificationTypes().stream().collect(Collectors.toList()));
        combinedFields.add(name);
        combinedFields.add(frequency);
        combinedFields.add(notificationTypes);
        combinedFields.addAll(channelUIComponent.getFields());

        final UIComponent combinedUIComponent = new UIComponent(channelUIComponent.getLabel(), channelUIComponent.getUrlName(), channelUIComponent.getFontAwesomeIcon(), combinedFields);
        return combinedUIComponent;
    }

}
