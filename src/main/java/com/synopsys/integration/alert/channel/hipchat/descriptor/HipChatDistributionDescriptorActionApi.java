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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.context.ChannelDistributionDescriptorActionApi;

@Component
public class HipChatDistributionDescriptorActionApi extends ChannelDistributionDescriptorActionApi {

    @Autowired
    public HipChatDistributionDescriptorActionApi(final HipChatChannel hipChatChannel, final BaseConfigurationAccessor configurationAccessor, final ContentConverter contentConverter, final List<ProviderDescriptor> providerDescriptors) {
        super(hipChatChannel, configurationAccessor, contentConverter, providerDescriptors);
    }

    @Override
    public void validateChannelConfig(final FieldAccessor fieldAccessor, final Map<String, String> fieldErrors) {
        final String roomId = fieldAccessor.getString(HipChatDistributionUIConfig.KEY_ROOM_ID).orElse(null);
        if (StringUtils.isBlank(roomId)) {
            fieldErrors.put("roomId", "A Room Id is required.");
        } else if (!StringUtils.isNumeric(roomId)) {
            fieldErrors.put("roomId", "Room Id must be an integer value");
        }
    }

}
