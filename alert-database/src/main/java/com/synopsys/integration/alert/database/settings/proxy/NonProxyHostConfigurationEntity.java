/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.settings.proxy;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

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
