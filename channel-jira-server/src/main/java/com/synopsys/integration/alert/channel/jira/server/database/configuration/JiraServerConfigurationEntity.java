/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.database.configuration;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "alert", name = "configuration_jira_server")
public class JiraServerConfigurationEntity extends BaseEntity {
    private static final long serialVersionUID = 4134901454399557557L;
    @Id
    @Column(name = "configuration_id")
    private UUID configurationId;
    @Column(name = "name")
    private String name;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;
    @Column(name = "url")
    private String url;
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;
    @Column(name = "disable_plugin_check")
    private Boolean disablePluginCheck;

    public JiraServerConfigurationEntity() {
    }

    public JiraServerConfigurationEntity(UUID configurationId, String name, OffsetDateTime createdAt, OffsetDateTime lastUpdated, String url, String username, String password,
        Boolean disablePluginCheck) {
        this.configurationId = configurationId;
        this.name = name;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.url = url;
        this.username = username;
        this.password = password;
        this.disablePluginCheck = disablePluginCheck;
    }

    public UUID getConfigurationId() {
        return configurationId;
    }

    public String getName() {
        return name;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getDisablePluginCheck() {
        return disablePluginCheck;
    }
}
