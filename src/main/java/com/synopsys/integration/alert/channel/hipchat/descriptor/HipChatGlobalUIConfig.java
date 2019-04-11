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

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;

@Component
public class HipChatGlobalUIConfig extends UIConfig {
    private static final String LABEL_API_KEY = "Api Key";
    private static final String LABEL_HOST_SERVER = "Host Server";

    private static final String HIP_CHAT_API_KEY_DESCRIPTION = "The API key of the user you want to use to authenticate with the HipChat server.";
    private static final String HIP_CHAT_HOST_SERVER_DESCRIPTION = "The URL for your HipChat server.";

    private static final String LABEL_TEST_ROOM_ID = "Room ID";

    public HipChatGlobalUIConfig() {
        super(HipChatDescriptor.HIP_CHAT_LABEL, HipChatDescriptor.HIP_CHAT_DESCRIPTION, HipChatDescriptor.HIP_CHAT_URL, HipChatDescriptor.HIP_CHAT_ICON);
    }

    @Override
    public List<ConfigField> createFields() {
        final ConfigField apiKey = PasswordConfigField.createRequired(HipChatDescriptor.KEY_API_KEY, LABEL_API_KEY, HIP_CHAT_API_KEY_DESCRIPTION);
        final ConfigField hostServer = TextInputConfigField.createRequired(HipChatDescriptor.KEY_HOST_SERVER, LABEL_HOST_SERVER, HIP_CHAT_HOST_SERVER_DESCRIPTION);
        return List.of(apiKey, hostServer);
    }

    @Override
    public String createTestLabel() {
        return LABEL_TEST_ROOM_ID;
    }
}