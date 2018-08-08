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
package com.blackducksoftware.integration.alert.channel.hipchat.descriptor;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.channel.event.ChannelEventFactory;
import com.blackducksoftware.integration.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.alert.common.descriptor.config.ConfigField;
import com.blackducksoftware.integration.alert.common.descriptor.config.DescriptorConfig;
import com.blackducksoftware.integration.alert.common.descriptor.config.UIComponent;
import com.blackducksoftware.integration.alert.common.enumeration.FieldGroup;
import com.blackducksoftware.integration.alert.common.enumeration.FieldType;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatDistributionRepositoryAccessor;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.channel.model.HipChatDistributionConfig;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.exception.IntegrationException;

@Component
public class HipChatDistributionDescriptorConfig extends DescriptorConfig {
    private final ChannelEventFactory channelEventFactory;
    private final HipChatChannel hipChatChannel;

    @Autowired
    public HipChatDistributionDescriptorConfig(final HipChatDistributionTypeConverter databaseContentConverter, final HipChatDistributionRepositoryAccessor repositoryAccessor,
            final ChannelEventFactory channelEventFactory, final HipChatChannel hipChatChannel) {
        super(databaseContentConverter, repositoryAccessor);
        this.channelEventFactory = channelEventFactory;
        this.hipChatChannel = hipChatChannel;
    }

    @Override
    public UIComponent getUiComponent() {
        final ConfigField roomId = new ConfigField("roomId", "Room Id", FieldType.NUMBER_INPUT, true, false, FieldGroup.DEFAULT);
        final ConfigField notify = new ConfigField("notify", "Notify", FieldType.CHECKBOX_INPUT, false, false, FieldGroup.DEFAULT);
        final ConfigField color = new ConfigField("color", "Color", FieldType.SELECT, false, false, FieldGroup.DEFAULT, Arrays.asList("Yellow, Green, Red, Purple, Gray, Random"));
        return new UIComponent("HipChat", "hipchat", "comments", Arrays.asList(roomId, notify, color));
    }

    @Override
    public void validateConfig(final Config restModel, final Map<String, String> fieldErrors) {
        final HipChatDistributionConfig hipChatRestModel = (HipChatDistributionConfig) restModel;
        if (StringUtils.isBlank(hipChatRestModel.getRoomId())) {
            fieldErrors.put("roomId", "A Room Id is required.");
        } else if (!StringUtils.isNumeric(hipChatRestModel.getRoomId())) {
            fieldErrors.put("roomId", "Room Id must be an integer value");
        }
    }

    @Override
    public void testConfig(final DatabaseEntity entity) throws IntegrationException {
        final HipChatDistributionConfigEntity hipChatEntity = (HipChatDistributionConfigEntity) entity;
        final ChannelEvent event = channelEventFactory.createChannelTestEvent(HipChatChannel.COMPONENT_NAME);
        hipChatChannel.sendAuditedMessage(event, hipChatEntity);
    }

}
