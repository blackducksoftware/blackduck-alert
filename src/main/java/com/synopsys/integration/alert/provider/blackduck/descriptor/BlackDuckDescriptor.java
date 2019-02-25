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
package com.synopsys.integration.alert.provider.blackduck.descriptor;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.action.NoTestActionApi;
import com.synopsys.integration.alert.common.workflow.MessageContentCollector;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;

@Component
public class BlackDuckDescriptor extends ProviderDescriptor {
    public static final String KEY_BLACKDUCK_URL = "blackduck.url";
    public static final String KEY_BLACKDUCK_API_KEY = "blackduck.api.key";
    public static final String KEY_BLACKDUCK_TIMEOUT = "blackduck.timeout";

    public static final String BLACKDUCK_LABEL = "Black Duck";
    public static final String BLACKDUCK_URL = "blackduck";
    public static final String BLACKDUCK_ICON = "laptop";

    private final BlackDuckTopicCollectorFactory topicCollectorFactory;

    @Autowired
    public BlackDuckDescriptor(final BlackDuckProviderDescriptorActionApi providerRestApi, final BlackDuckProviderUIConfig blackDuckProviderUIConfig, final NoTestActionApi noTestActionApi,
        final BlackDuckDistributionUIConfig blackDuckDistributionUIConfig, final BlackDuckProvider provider, final BlackDuckTopicCollectorFactory topicCollectorFactory) {
        super(providerRestApi, blackDuckProviderUIConfig, noTestActionApi, blackDuckDistributionUIConfig, provider);
        this.topicCollectorFactory = topicCollectorFactory;
    }

    @Override
    public Set<MessageContentCollector> createTopicCollectors() {
        return topicCollectorFactory.createTopicCollectors();
    }
}
