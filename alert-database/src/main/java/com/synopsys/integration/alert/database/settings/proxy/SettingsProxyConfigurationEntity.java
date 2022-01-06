/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.settings.proxy;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "alert", name = "configuration_proxy")
public class SettingsProxyConfigurationEntity extends BaseEntity {
    private static final long serialVersionUID = 2892743902840944459L;
    @Id
    @Column(name = "configuration_id")
    private UUID configurationId;
    @Column(name = "name")
    private String name;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;
    @Column(name = "host")
    private String host;
    @Column(name = "port")
    private Integer port;
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;

    @OneToMany
    @JoinColumn(name = "configuration_id", referencedColumnName = "configuration_id", insertable = false, updatable = false)
    private List<NonProxyHostConfigurationEntity> nonProxyHosts;

    public SettingsProxyConfigurationEntity() {
    }

    public SettingsProxyConfigurationEntity(UUID configurationId, String name, OffsetDateTime createdAt, OffsetDateTime lastUpdated, String host, Integer port, String username,
        String password,
        List<NonProxyHostConfigurationEntity> nonProxyHosts) {
        this.configurationId = configurationId;
        this.name = name;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.nonProxyHosts = nonProxyHosts;
    }

    public UUID getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(UUID configurationId) {
        this.configurationId = configurationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<NonProxyHostConfigurationEntity> getNonProxyHosts() {
        return nonProxyHosts;
    }

    public void setNonProxyHosts(List<NonProxyHostConfigurationEntity> nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }
}
