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
package com.synopsys.integration.alert.channel.hipchat.descriptor;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;

@Component
public class HipChatDistributionUIConfig extends UIConfig {

    public HipChatDistributionUIConfig() {
        super(HipChatDescriptor.HIP_CHAT_LABEL, HipChatDescriptor.HIP_CHAT_URL, HipChatDescriptor.HIP_CHAT_ICON);
    }

    @Override
    public List<ConfigField> createFields() {
        final ConfigField roomId = NumberConfigField.createRequired(HipChatDescriptor.KEY_ROOM_ID, "Room Id");
        final ConfigField notify = CheckboxConfigField.create(HipChatDescriptor.KEY_NOTIFY, "Notify");
        final ConfigField color = SelectConfigField.create(HipChatDescriptor.KEY_COLOR, "Color", Arrays.asList("Yellow", "Green", "Red", "Purple", "Gray", "Random"));
        return List.of(roomId, notify, color);
    }
}
