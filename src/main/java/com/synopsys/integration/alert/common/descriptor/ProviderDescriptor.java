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
package com.synopsys.integration.alert.common.descriptor;

import java.util.Set;

import javax.validation.constraints.NotNull;

import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.ProviderContentType;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentCollector;

public abstract class ProviderDescriptor extends Descriptor {
    private final Provider provider;

    public ProviderDescriptor(final DescriptorActionApi providerDescriptorActionApi, final UIConfig providerUiConfig, final DescriptorActionApi distributionDescriptorActionApi, final UIConfig distributionUIConfig,
        @NotNull final Provider provider) {
        super(provider.getName(), DescriptorType.PROVIDER);
        this.provider = provider;
        addGlobalUiConfig(providerDescriptorActionApi, providerUiConfig);
        addDistributionUiConfig(distributionDescriptorActionApi, distributionUIConfig);
    }

    public Provider getProvider() {
        return provider;
    }

    public Set<ProviderContentType> getProviderContentTypes() {
        return getProvider().getProviderContentTypes();
    }

    public abstract Set<MessageContentCollector> createTopicCollectors();
}
