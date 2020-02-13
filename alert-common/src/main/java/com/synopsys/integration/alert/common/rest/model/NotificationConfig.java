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
package com.synopsys.integration.alert.common.rest.model;

public class NotificationConfig extends Config {
    private String createdAt;
    private String provider;
    private Long providerConfigId;
    private String providerConfigName;
    private String providerCreationTime;
    private String notificationType;
    private String content;

    public NotificationConfig() {
    }

    public NotificationConfig(String id, String createdAt, String provider, Long providerConfigId, String providerConfigName, String providerCreationTime, String notificationType, String content) {
        super(id);
        this.createdAt = createdAt;
        this.provider = provider;
        this.providerConfigId = providerConfigId;
        this.providerConfigName = providerConfigName;
        this.providerCreationTime = providerCreationTime;
        this.notificationType = notificationType;
        this.content = content;

    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    public void setProviderConfigId(Long providerConfigId) {
        this.providerConfigId = providerConfigId;
    }

    public String getProviderConfigName() {
        return providerConfigName;
    }

    public void setProviderConfigName(String providerConfigName) {
        this.providerConfigName = providerConfigName;
    }

    public String getProviderCreationTime() {
        return providerCreationTime;
    }

    public void setProviderCreationTime(String providerCreationTime) {
        this.providerCreationTime = providerCreationTime;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

}
