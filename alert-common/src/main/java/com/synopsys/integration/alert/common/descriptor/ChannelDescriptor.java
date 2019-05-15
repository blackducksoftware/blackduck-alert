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
package com.synopsys.integration.alert.common.descriptor;

import com.synopsys.integration.alert.common.descriptor.action.DescriptorActionApi;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.event.AlertEventListener;

public abstract class ChannelDescriptor extends Descriptor {
    private final AlertEventListener eventListener;

    public ChannelDescriptor(final String name, final AlertEventListener eventListener, final DescriptorActionApi distributionDescriptorActionApi) {
        super(name, DescriptorType.CHANNEL);
        this.eventListener = eventListener;
        addDistributionActionApi(distributionDescriptorActionApi);
    }

    public ChannelDescriptor(final String name, final AlertEventListener eventListener, final DescriptorActionApi distributionDescriptorActionApi, final UIConfig distributionUIConfig) {
        super(name, DescriptorType.CHANNEL);
        this.eventListener = eventListener;
        addDistributionUiConfig(distributionDescriptorActionApi, distributionUIConfig);
    }

    public ChannelDescriptor(final String name, final AlertEventListener eventListener, final DescriptorActionApi distributionDescriptorActionApi, final UIConfig distributionUIConfig,
        final DescriptorActionApi globalDescriptorActionApi, final UIConfig globalUIConfig) {
        this(name, eventListener, distributionDescriptorActionApi, distributionUIConfig);
        addGlobalUiConfig(globalDescriptorActionApi, globalUIConfig);
    }

    public AlertEventListener getEventListener() {
        return eventListener;
    }

}
