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
package com.synopsys.integration.alert.provider.polaris.descriptor;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentCollector;
import com.synopsys.integration.alert.database.api.configuration.model.DefinedFieldModel;
import com.synopsys.integration.alert.provider.polaris.PolarisProvider;

@Component
public class PolarisDescriptor extends ProviderDescriptor {
    public static final String KEY_POLARIS_URL = "polaris.url";
    public static final String KEY_POLARIS_ACCESS_TOKEN = "polaris.access.token";
    public static final String KEY_POLARIS_TIMEOUT = "polaris.timeout";

    public static final String POLARIS_ICON = "desktop";
    public static final String POLARIS_LABEL = "Polaris";
    public static final String POLARIS_URL_NAME = "polaris";

    @Autowired
    public PolarisDescriptor(final PolarisGlobalDescriptorActionApi polarisGlobalDescriptorActionApi, final PolarisGlobalUIConfig polarisGlobalUIConfig, final PolarisDistributionDescriptorActionApi polarisDistributionDescriptorActionApi,
        final PolarisDistributionUIConfig polarisDistributionUIConfig, final @NotNull PolarisProvider provider) {
        super(polarisGlobalDescriptorActionApi, polarisGlobalUIConfig, polarisDistributionDescriptorActionApi, polarisDistributionUIConfig, provider);
    }

    @Override
    public Set<MessageContentCollector> createTopicCollectors() {
        // FIXME implement topic collectors
        return Set.of();
    }

    @Override
    public Collection<DefinedFieldModel> getDefinedFields(final ConfigContextEnum context) {
        if (ConfigContextEnum.GLOBAL.equals(context)) {
            final DefinedFieldModel polarisUrl = DefinedFieldModel.createGlobalField(KEY_POLARIS_URL);
            final DefinedFieldModel polarisAccessToken = DefinedFieldModel.createGlobalField(KEY_POLARIS_ACCESS_TOKEN);
            final DefinedFieldModel polarisTimeout = DefinedFieldModel.createGlobalField(KEY_POLARIS_TIMEOUT);

            return List.of(polarisUrl, polarisAccessToken, polarisTimeout);
        } else if (ConfigContextEnum.DISTRIBUTION.equals(context)) {
            // FIXME add distribution fields
        }
        return List.of();
    }
}
