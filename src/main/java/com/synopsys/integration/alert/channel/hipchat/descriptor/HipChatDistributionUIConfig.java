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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;

@Component
public class HipChatDistributionUIConfig extends ChannelDistributionUIConfig {
    private static final String LABEL_ROOM_ID = "Room Id";
    private static final String LABEL_NOTIFY = "Notify";
    private static final String LABEL_COLOR = "Color";

    private static final String HIP_CHAT_ROOM_ID_DESCRIPTION = "The API ID of the room to receive Alerts.";
    private static final String HIP_CHAT_NOTIFY_DESCRIPTION = "If true, this will add to the count of new messages in the HipChat room.";
    private static final String HIP_CHAT_COLOR_DESCRIPTION = "The text color to display the Alert messages in.";

    @Autowired
    public HipChatDistributionUIConfig(final DescriptorAccessor descriptorAccessor) {
        super(HipChatDescriptor.HIP_CHAT_LABEL, HipChatDescriptor.HIP_CHAT_URL, HipChatDescriptor.HIP_CHAT_ICON, descriptorAccessor);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        final ConfigField roomId = NumberConfigField.createRequired(HipChatDescriptor.KEY_ROOM_ID, LABEL_ROOM_ID, HIP_CHAT_ROOM_ID_DESCRIPTION);
        final ConfigField notify = CheckboxConfigField.create(HipChatDescriptor.KEY_NOTIFY, LABEL_NOTIFY, HIP_CHAT_NOTIFY_DESCRIPTION);
        final ConfigField color = SelectConfigField.create(HipChatDescriptor.KEY_COLOR, LABEL_COLOR, HIP_CHAT_COLOR_DESCRIPTION, List.of(
            new LabelValueSelectOption("Yellow"),
            new LabelValueSelectOption("Green"),
            new LabelValueSelectOption("Red"),
            new LabelValueSelectOption("Purple"),
            new LabelValueSelectOption("Gray"),
            new LabelValueSelectOption("Random")));
        return List.of(roomId, notify, color);
    }
}