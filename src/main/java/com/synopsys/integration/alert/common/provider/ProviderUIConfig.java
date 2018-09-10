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
package com.synopsys.integration.alert.common.provider;

import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.descriptor.config.UIConfig;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.enumeration.FormatType;

public abstract class ProviderUIConfig extends UIConfig {

    private final Provider provider;

    public ProviderUIConfig(final Provider provider) {
        this.provider = provider;
    }

    public ConfigField getNotificationTypeField() {
        return new SelectConfigField("notificationTypes", "Notification Types", true, false, false, true, provider.getProviderContentTypes().stream().map(ProviderContentType::getNotificationType).collect(Collectors.toList()));
    }

    public ConfigField getSupportedFormatTypeField() {
        return new SelectConfigField("formatType", "Format", true, false, false, false, provider.getSupportedFormatTypes().stream().map(FormatType::name).collect(Collectors.toList()));
    }
}
