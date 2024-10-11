/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.oauth.database.configuration;

import java.util.UUID;

import com.blackduck.integration.alert.database.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "alert", name = "oauth_credentials")
public class AlertOAuthConfigurationEntity extends BaseEntity {
    private static final long serialVersionUID = -8009008640085992405L;
    @Id
    @Column(name = "configuration_id")
    private UUID id;
    @Column(name = "access_token")
    private String accessToken;
    @Column(name = "refresh_token")
    private String refreshToken;
    @Column(name = "expiration_time_ms")
    private Long expirationTimeMilliseconds;

    public AlertOAuthConfigurationEntity() {
        //default constructor for JPA
    }

    public AlertOAuthConfigurationEntity(UUID id, String accessToken, String refreshToken, Long expirationTimeMilliseconds) {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expirationTimeMilliseconds = expirationTimeMilliseconds;
    }

    public UUID getId() {
        return id;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Long getExpirationTimeMilliseconds() {
        return expirationTimeMilliseconds;
    }
}
