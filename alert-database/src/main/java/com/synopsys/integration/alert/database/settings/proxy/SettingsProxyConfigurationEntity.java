package com.synopsys.integration.alert.database.settings.proxy;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "alert", name = "configuration_proxy")
public class SettingsProxyConfigurationEntity extends BaseEntity {
    private static final long serialVersionUID = 2892743902840944459L;
    @Id
    @Column(name = "configuration_id")
    private UUID configurationId;
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
    @Column(name = "non_proxy_hosts")
    private String nonProxyHosts;
    //TODO: If nonProxyHosts is a list, we may want to create a table. Name it configuration_non_proxy_hosts
    //  give it a one to many relation

    public SettingsProxyConfigurationEntity() {
    }

    public SettingsProxyConfigurationEntity(UUID configurationId, OffsetDateTime createdAt, OffsetDateTime lastUpdated, String host, Integer port, String username, String password, String nonProxyHosts) {
        this.configurationId = configurationId;
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

    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

    public void setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }
}
