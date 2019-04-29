/**
 * blackduck-alert
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
package com.synopsys.integration.alert.channel.hipchat.descriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;

@Component
public class HipChatDescriptor extends ChannelDescriptor {
    public static final String KEY_ROOM_ID = "channel.hipchat.room.id";
    public static final String KEY_NOTIFY = "channel.hipchat.notify";
    public static final String KEY_COLOR = "channel.hipchat.color";

    public static final String KEY_API_KEY = "channel.hipchat.api.key";
    public static final String KEY_HOST_SERVER = "channel.hipchat.host.server";

    public static final String HIP_CHAT_LABEL = "HipChat";
    public static final String HIP_CHAT_URL = "hipchat";
    public static final String HIP_CHAT_ICON = "comments";
    public static final String HIP_CHAT_DESCRIPTION = "This page allows you to configure the HipChat server that Alert will send messages to.";

    @Autowired
    public HipChatDescriptor(final HipChatDistributionUIConfig hipChatDistributionUIConfig, final HipChatGlobalUIConfig hipChatGlobalUIConfig) {
        super(HipChatChannel.COMPONENT_NAME, hipChatDistributionUIConfig, hipChatGlobalUIConfig);
    }

}