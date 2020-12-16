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
package com.synopsys.integration.alert.common.action;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public abstract class ConfigurationAction {
    private final DescriptorKey descriptorKey;
    private final Map<ConfigContextEnum, ApiAction> apiActionMap = new EnumMap<>(ConfigContextEnum.class);
    private final Map<ConfigContextEnum, TestAction> testActionMap = new EnumMap<>(ConfigContextEnum.class);

    // FIXME there needs to be a better distinction between a global TestAction and a distribution TestAction
    //  for 6.4.0, this will have to suffice to avoid additional scope-creep of re-architecting TestActions
    private ChannelDistributionTestAction channelDistributionTestAction;

    protected ConfigurationAction(DescriptorKey descriptorKey) {
        this.descriptorKey = descriptorKey;
    }

    public DescriptorKey getDescriptorKey() {
        return descriptorKey;
    }

    public void addGlobalApiAction(ApiAction apiAction) {
        apiActionMap.put(ConfigContextEnum.GLOBAL, apiAction);
    }

    public void addDistributionApiAction(ApiAction apiAction) {
        apiActionMap.put(ConfigContextEnum.DISTRIBUTION, apiAction);
    }

    public void addGlobalTestAction(TestAction testAction) {
        testActionMap.put(ConfigContextEnum.GLOBAL, testAction);
    }

    public void addDistributionTestAction(TestAction testAction) {
        testActionMap.put(ConfigContextEnum.DISTRIBUTION, testAction);
    }

    public void addDistributionTestAction(ChannelDistributionTestAction testAction) {
        channelDistributionTestAction = testAction;
    }

    public ApiAction getApiAction(ConfigContextEnum context) {
        return apiActionMap.get(context);
    }

    public TestAction getTestAction(ConfigContextEnum context) {
        return testActionMap.get(context);
    }

    public Optional<ChannelDistributionTestAction> getChannelDistributionTestAction() {
        return Optional.ofNullable(channelDistributionTestAction);
    }

}
