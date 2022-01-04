/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

import java.time.OffsetDateTime;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class AlertNotificationModel extends AlertSerializableModel {
    private Long id;
    private final Long providerConfigId;

    private final String provider;
    private final String providerConfigName;
    private final String notificationType;
    private final String content;
    private final boolean processed;

    private final OffsetDateTime createdAt;
    private final OffsetDateTime providerCreationTime;

    public AlertNotificationModel(Long id, Long providerConfigId, String provider, String providerConfigName, String notificationType, String content, OffsetDateTime createdAt, OffsetDateTime providerCreationTime, boolean processed) {
        this.id = id;
        this.providerConfigId = providerConfigId;
        this.provider = provider;
        this.providerConfigName = providerConfigName;
        this.notificationType = notificationType;
        this.content = content;
        this.createdAt = createdAt;
        this.providerCreationTime = providerCreationTime;
        this.processed = processed;
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

    public boolean getProcessed() {
        return processed;
    }

}
