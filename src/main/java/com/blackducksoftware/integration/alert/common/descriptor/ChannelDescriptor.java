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
package com.blackducksoftware.integration.alert.common.descriptor;

import javax.jms.MessageListener;

import com.blackducksoftware.integration.alert.common.descriptor.config.DescriptorConfig;
import com.blackducksoftware.integration.alert.common.enumeration.DescriptorType;

public abstract class ChannelDescriptor extends Descriptor {
    private final String destinationName;
    private final MessageListener channelListener;

    public ChannelDescriptor(final String name, final String destinationName, final MessageListener channelListener, final DescriptorConfig distributionDescriptorConfig) {
        super(name, DescriptorType.CHANNEL);
        this.destinationName = destinationName;
        this.channelListener = channelListener;

        addDistributionConfig(distributionDescriptorConfig);
    }

    public String getDestinationName() {
        return destinationName;
    }

    public MessageListener getChannelListener() {
        return channelListener;
    }
}
