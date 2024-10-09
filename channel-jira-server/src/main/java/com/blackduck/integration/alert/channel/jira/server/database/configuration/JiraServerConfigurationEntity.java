package com.blackduck.integration.alert.channel.jira.server.database.configuration;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.blackduck.integration.alert.database.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
    @Column(name = "authorization_method")
    private JiraServerAuthorizationMethod authorizationMethod;
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;
    @Column(name = "access_token")
    private String accessToken;
    @Column(name = "disable_plugin_check")
    private Boolean disablePluginCheck;

    public JiraServerConfigurationEntity() {
    }

    public JiraServerConfigurationEntity(
        UUID configurationId, String name, OffsetDateTime createdAt, OffsetDateTime lastUpdated, String url, JiraServerAuthorizationMethod authorizationMethod,
        String username, String password, String accessToken, Boolean disablePluginCheck
    ) {
        this.configurationId = configurationId;
        this.name = name;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.url = url;
        this.authorizationMethod = authorizationMethod;
        this.username = username;
        this.password = password;
        this.accessToken = accessToken;
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

    public JiraServerAuthorizationMethod getAuthorizationMethod() {
        return authorizationMethod;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Boolean getDisablePluginCheck() {
        return disablePluginCheck;
    }
}
