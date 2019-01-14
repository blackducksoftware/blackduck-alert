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

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.PasswordConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.FieldGroup;
import com.synopsys.integration.alert.web.model.FieldValueModel;

@Component
public class HipChatGlobalUIConfig extends UIConfig {

    public HipChatGlobalUIConfig() {
        super(HipChatDescriptor.HIP_CHAT_LABEL, HipChatDescriptor.HIP_CHAT_URL, HipChatDescriptor.HIP_CHAT_ICON);
    }

    @Override
    public List<ConfigField> createFields() {
        final ConfigField apiKey = PasswordConfigField.createRequired(HipChatDescriptor.KEY_API_KEY, "Api Key", this::validateApiKey);
        final ConfigField hostServer = TextInputConfigField.createGrouped(HipChatDescriptor.KEY_HOST_SERVER, "Host Server", FieldGroup.ADVANCED);
        return List.of(apiKey, hostServer);
    }

    private Collection<String> validateApiKey(final FieldValueModel fieldValueModel) {
        final String apiKey = fieldValueModel.getValue().orElse(null);
        if (StringUtils.isBlank(apiKey)) {
            return List.of("API Key can't be blank");
        }
        return List.of();
    }

}
