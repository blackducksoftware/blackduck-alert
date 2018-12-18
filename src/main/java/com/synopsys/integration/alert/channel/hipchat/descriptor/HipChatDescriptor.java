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
package com.synopsys.integration.alert.channel.hipchat.descriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.api.configuration.DefinedFieldModel;

@Component
public class HipChatDescriptor extends ChannelDescriptor {
    public static final String KEY_ROOM_ID = "channel.hipchat.room.id";
    public static final String KEY_NOTIFY = "channel.hipchat.notify";
    public static final String KEY_COLOR = "channel.hipchat.color";

    public static final String KEY_API_KEY = "api.key";
    public static final String KEY_HOST_SERVER = "host.server";

    @Autowired
    public HipChatDescriptor(final HipChatChannel channelListener, final HipChatDistributionDescriptorActionApi distributionRestApi, final HipChatDistributionUIConfig hipChatDistributionUIConfig,
        final HipChatGlobalDescriptorActionApi hipChatGlobalRestApi, final HipChatGlobalUIConfig hipChatGlobalUIConfig) {
        super(HipChatChannel.COMPONENT_NAME, HipChatChannel.COMPONENT_NAME, channelListener, distributionRestApi, hipChatDistributionUIConfig, hipChatGlobalRestApi, hipChatGlobalUIConfig);
    }

    @Override
    public Collection<DefinedFieldModel> getDefinedFields(final ConfigContextEnum context) {
        if (ConfigContextEnum.GLOBAL == context) {
            final DefinedFieldModel apiKey = DefinedFieldModel.createGlobalSensitiveField(KEY_API_KEY);
            final DefinedFieldModel hostServer = DefinedFieldModel.createGlobalField(KEY_HOST_SERVER);
            return List.of(apiKey, hostServer);
        } else if (ConfigContextEnum.DISTRIBUTION == context) {
            final DefinedFieldModel roomId = DefinedFieldModel.createDistributionField(KEY_ROOM_ID);
            final DefinedFieldModel notify = DefinedFieldModel.createDistributionField(KEY_NOTIFY);
            final DefinedFieldModel color = DefinedFieldModel.createDistributionField(KEY_COLOR);
            return List.of(roomId, notify, color);
        }
        return Collections.emptyList();
    }

}
