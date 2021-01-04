/**
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.component.settings.descriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ComponentDescriptor;

@Component
public class SettingsDescriptor extends ComponentDescriptor {
    public static final String SETTINGS_LABEL = "Settings";
    public static final String SETTINGS_URL = "settings";
    public static final String SETTINGS_DESCRIPTION = "This page allows you to configure the global settings.";

    // Values not stored in the database, but keys must be registered
    public static final String KEY_ENCRYPTION_PWD = "settings.encryption.password";
    public static final String KEY_ENCRYPTION_GLOBAL_SALT = "settings.encryption.global.salt";

    public static final String FIELD_ERROR_ENCRYPTION_FIELD_TOO_SHORT = "The value must be at least 8 characters.";
    public static final String FIELD_ERROR_ENCRYPTION_PWD = "Encryption password missing";
    public static final String FIELD_ERROR_ENCRYPTION_GLOBAL_SALT = "Encryption global salt missing";

    @Autowired
    public SettingsDescriptor(SettingsDescriptorKey settingsDescriptorKey, SettingsUIConfig uiConfig) {
        super(settingsDescriptorKey, uiConfig);
    }

}
