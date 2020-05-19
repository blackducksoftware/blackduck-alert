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

import java.time.OffsetDateTime;

public class AlertNotificationModel extends AlertSerializableModel {
    private Long id;
    private Long providerConfigId;

    private String provider;
    private String providerConfigName;
    private String notificationType;
    private String content;

    private OffsetDateTime createdAt;
    private OffsetDateTime providerCreationTime;

    public AlertNotificationModel(Long id, Long providerConfigId, String provider, String providerConfigName, String notificationType, String content, OffsetDateTime createdAt, OffsetDateTime providerCreationTime) {
        this.id = id;
        this.providerConfigId = providerConfigId;
        this.provider = provider;
        this.providerConfigName = providerConfigName;
        this.notificationType = notificationType;
        this.content = content;
        this.createdAt = createdAt;
        this.providerCreationTime = providerCreationTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderConfigName() {
        return providerConfigName;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getContent() {
        return content;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getProviderCreationTime() {
        return providerCreationTime;
    }

}
