/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.settings.proxy;

import java.util.UUID;

import com.blackduck.integration.alert.database.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(NonProxyHostConfigurationPK.class)
@Table(schema = "alert", name = "configuration_non_proxy_hosts")
public class NonProxyHostConfigurationEntity extends BaseEntity {
    private static final long serialVersionUID = -1764078816502000994L;
    @Id
    @Column(name = "configuration_id")
    private UUID configurationId;
    @Column(name = "hostname_pattern")
    private String hostnamePattern;

    public NonProxyHostConfigurationEntity() {
    }

    public NonProxyHostConfigurationEntity(UUID configurationId, String hostnamePattern) {
        this.configurationId = configurationId;
        this.hostnamePattern = hostnamePattern;
    }

    public UUID getConfigurationId() {
        return configurationId;
    }

    public String getHostnamePattern() {
        return hostnamePattern;
    }
}
