/**
 * alert-common
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
package com.synopsys.integration.alert.common.enumeration;

public enum PermissionKeys {
    CONFIG_GLOBAL_PROVIDER_BLACKDUCK("config.global.provider_blackduck"),
    CONFIG_DISTRIBUTION_PROVIDER_BLACKDUCK("config.distribution.provider_blackduck"),
    CONFIG_GLOBAL_PROVIDER_POLARIS("config.global.provider_polaris"),
    CONFIG_DISTRIBUTION_PROVIDER_POLARIS("config.distribution.provider_polaris"),
    CONFIG_CHANNEL_EMAIL("config.global.channel_email"),
    CONFIG_DISTRIBUTION_CHANNEL_EMAIL("config.distribution.channel_email"),
    CONFIG_GLOBAL_CHANNEL_HIPCHAT("config.global.channel_hipchat"),
    CONFIG_DISTRIBUTION_CHANNEL_HIPCHAT("config.distribution.channel_hipchat"),
    CONFIG_CHANNEL_SLACK("config.distribution.channel_slack"),
    CONFIG_DISTRIBUTION("config.distribution"),
    CONFIG_COMPONENT_SCHEDULING("config.global.component_scheduling"),
    CONFIG_COMPONENT_SETTINGS("config.global.component_settings"),
    AUDIT_NOTIFICATIONS("audit.notifications"),
    AUDIT_NOTIFICATIONS_RESEND("audit.notifications.resend");

    private final String dbKey;

    PermissionKeys(final String dbKey) {
        this.dbKey = dbKey;
    }

    public String getDatabaseKey() {
        return dbKey;
    }
}
