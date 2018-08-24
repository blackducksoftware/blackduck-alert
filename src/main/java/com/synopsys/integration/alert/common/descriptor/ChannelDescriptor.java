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

import javax.jms.MessageListener;

import com.synopsys.integration.alert.common.descriptor.config.RestApi;
import com.synopsys.integration.alert.common.descriptor.config.UIConfig;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;

public abstract class ChannelDescriptor extends Descriptor {
    private final String destinationName;
    private final MessageListener channelListener;

    public ChannelDescriptor(final String name, final String destinationName, final MessageListener channelListener, final RestApi distributionRestApi) {
        super(name, DescriptorType.CHANNEL);
        this.destinationName = destinationName;
        this.channelListener = channelListener;

        addDistributionRestApi(distributionRestApi);
    }

    public ChannelDescriptor(final String name, final String destinationName, final MessageListener channelListener, final RestApi distributionRestApi, final UIConfig distributionUIConfig) {
        super(name, DescriptorType.CHANNEL);
        this.destinationName = destinationName;
        this.channelListener = channelListener;

        addDistributionUiConfigs(distributionRestApi, distributionUIConfig);
    }

    public ChannelDescriptor(final String name, final String destinationName, final MessageListener channelListener, final RestApi distributionRestApi, final RestApi globalRestApi) {
        this(name, destinationName, channelListener, distributionRestApi);
        addGlobalRestApi(globalRestApi);
    }

    public ChannelDescriptor(final String name, final String destinationName, final MessageListener channelListener, final RestApi distributionRestApi, final UIConfig distributionUIConfig,
            final RestApi globalRestApi, final UIConfig globalUIConfig) {
        this(name, destinationName, channelListener, distributionRestApi, distributionUIConfig);
        addGlobalUiConfigs(globalRestApi, globalUIConfig);
    }

    public String getDestinationName() {
        return destinationName;
    }

    public MessageListener getChannelListener() {
        return channelListener;
    }
}
