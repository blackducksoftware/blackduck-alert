/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.message.model;

import java.util.Date;

import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;

public class CommonMessageData {
    private final Long notificationId;
    private final Long providerConfigId;
    private final String providerName;
    private final String providerConfigName;
    private final String providerURL;
    private final Date providerCreationDate;
    private final ConfigurationJobModel job;

    public CommonMessageData(Long notificationId, Long providerConfigId, String providerName, String providerConfigName, String providerURL, Date providerCreationDate,
        ConfigurationJobModel job) {
        this.notificationId = notificationId;
        this.providerConfigId = providerConfigId;
        this.providerName = providerName;
        this.providerConfigName = providerConfigName;
        this.providerURL = providerURL;
        this.providerCreationDate = providerCreationDate;
        this.job = job;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getProviderConfigName() {
        return providerConfigName;
    }

    public String getProviderURL() {
        return providerURL;
    }

    public Date getProviderCreationDate() {
        return providerCreationDate;
    }

    public ConfigurationJobModel getJob() {
        return job;
    }
}
