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
package com.blackducksoftware.integration.alert.common;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.common.descriptor.Descriptor;
import com.blackducksoftware.integration.alert.common.descriptor.DescriptorMap;
import com.blackducksoftware.integration.alert.common.descriptor.config.DescriptorConfig;
import com.blackducksoftware.integration.alert.common.enumeration.DescriptorConfigType;
import com.blackducksoftware.integration.alert.web.model.AboutDescriptorModel;
import com.blackducksoftware.integration.alert.web.model.AboutModel;
import com.blackducksoftware.integration.util.ResourceUtil;
import com.google.gson.Gson;

@Component
public class AboutReader {
    public final static String PRODUCT_VERSION_UNKNOWN = "unknown";
    private final static Logger logger = LoggerFactory.getLogger(AboutReader.class);
    private final Gson gson;
    private final DescriptorMap descriptorMap;

    @Autowired
    public AboutReader(final Gson gson, final DescriptorMap descriptorMap) {
        this.gson = gson;
        this.descriptorMap = descriptorMap;
    }

    public AboutModel getAboutModel() {
        try {
            final String aboutJson = ResourceUtil.getResourceAsString(getClass(), "/about.txt", StandardCharsets.UTF_8.toString());
            final AboutModel aboutModel = gson.fromJson(aboutJson, AboutModel.class);
            final List<AboutDescriptorModel> channelNameList = getDescriptorLabels(descriptorMap.getChannelDescriptorMap(), DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG);
            final List<AboutDescriptorModel> providerNameList = getDescriptorLabels(descriptorMap.getProviderDescriptorMap(), DescriptorConfigType.PROVIDER_CONFIG);
            return new AboutModel(aboutModel.getVersion(), aboutModel.getDescription(), aboutModel.getProjectUrl(), providerNameList, channelNameList);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    private List<AboutDescriptorModel> getDescriptorLabels(final Map<String, ? extends Descriptor> descriptorMap, final DescriptorConfigType descriptorConfigType) {
        return descriptorMap.values().stream()
                       .map(descriptor -> descriptor.getConfig(descriptorConfigType))
                       .map(DescriptorConfig::getUiComponent)
                       .map(uiComponent -> new AboutDescriptorModel(uiComponent.getFontAwesomeIcon(), uiComponent.getLabel()))
                       .collect(Collectors.toList());
    }

    public String getProductVersion() {
        final AboutModel aboutModel = getAboutModel();
        if (aboutModel != null) {
            return aboutModel.getVersion();
        } else {
            return PRODUCT_VERSION_UNKNOWN;
        }
    }
}
