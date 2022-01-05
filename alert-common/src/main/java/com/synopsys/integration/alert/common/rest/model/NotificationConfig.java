/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
