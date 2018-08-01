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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.alert.common.descriptor.config.DescriptorConfig;
import com.blackducksoftware.integration.alert.common.descriptor.config.UIComponent;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatGlobalRepositoryAccessor;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.channel.model.HipChatGlobalConfig;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.exception.IntegrationException;

@Component
public class HipChatGlobalDescriptorConfig extends DescriptorConfig {
    private final HipChatChannel hipChatChannel;

    @Autowired
    public HipChatGlobalDescriptorConfig(final HipChatGlobalContentConverter databaseContentConverter, final HipChatGlobalRepositoryAccessor repositoryAccessor, final HipChatChannel hipChatChannel,
            final HipChatStartupComponent hipChatStartupComponent) {
        super(databaseContentConverter, repositoryAccessor);
        this.hipChatChannel = hipChatChannel;
        setStartupComponent(hipChatStartupComponent);
    }

    @Override
    public UIComponent getUiComponent() {
        return new UIComponent("HipChat", "hipchat", "comments", "HipChatConfiguration");
    }

    @Override
    public void validateConfig(final Config restModel, final Map<String, String> fieldErrors) {
        final HipChatGlobalConfig hipChatRestModel = (HipChatGlobalConfig) restModel;
        if (StringUtils.isBlank(hipChatRestModel.getApiKey())) {
            fieldErrors.put("apiKey", "ApiKey can't be blank");
        }
    }

    @Override
    public void testConfig(final DatabaseEntity entity) throws IntegrationException {
        final HipChatGlobalConfigEntity hipChatEntity = (HipChatGlobalConfigEntity) entity;
        hipChatChannel.testGlobalConfig(hipChatEntity);
    }

}
