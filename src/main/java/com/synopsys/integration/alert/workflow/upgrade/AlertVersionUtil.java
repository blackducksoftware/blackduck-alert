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
package com.synopsys.integration.alert.workflow.upgrade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AboutReader;
import com.synopsys.integration.alert.common.database.BaseSettingsKeyAccessor;
import com.synopsys.integration.alert.database.api.settingskey.SettingsKeyModel;

@Component
public class AlertVersionUtil {
    public static final String KEY_ALERT_VERSION = "alert.version";

    private final BaseSettingsKeyAccessor settingsKeyAccessor;
    private final AboutReader aboutReader;

    @Autowired
    public AlertVersionUtil(final BaseSettingsKeyAccessor settingsKeyAccessor, final AboutReader aboutReader) {
        this.settingsKeyAccessor = settingsKeyAccessor;
        this.aboutReader = aboutReader;
    }

    public String findDBVersion() {
        return settingsKeyAccessor.getSettingsKeyByKey(KEY_ALERT_VERSION)
                   .map(SettingsKeyModel::getValue)
                   .orElse(AboutReader.PRODUCT_VERSION_UNKNOWN);
    }

    public String findFileVersion() {
        return aboutReader.getProductVersion();
    }

    public void updateVersionInDB(final String newVersion) {
        settingsKeyAccessor.saveSettingsKey(KEY_ALERT_VERSION, newVersion);
    }

}
