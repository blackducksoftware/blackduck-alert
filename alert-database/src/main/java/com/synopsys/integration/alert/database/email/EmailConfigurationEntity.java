/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.email;

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
import com.synopsys.integration.alert.database.email.properties.EmailConfigurationsProperties;

@Entity
@Table(schema = "alert", name = "configuration_email")
public class EmailConfigurationEntity extends BaseEntity {
    private static final long serialVersionUID = -7390754753617711596L;
    @Id
    @Column(name = "configuration_id")
    private UUID configurationId;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;
    @Column(name = "smtp_host")
    private String smtpHost;
    @Column(name = "smtp_from")
    private String smtpFrom;
    @Column(name = "port")
    private Long smtpPort;
    @Column(name = "auth_required")
    private Boolean authRequired;
    @Column(name = "auth_username")
    private String authUsername;
    @Column(name = "auth_password")
    private String authPassword;

    @OneToMany
    @JoinColumn(name = "configuration_id", referencedColumnName = "configuration_id", insertable = false, updatable = false)
    private List<EmailConfigurationsProperties> emailConfigurationsProperties;

    public EmailConfigurationEntity(UUID configurationId, OffsetDateTime createdAt, OffsetDateTime lastUpdated, String smtpHost, String smtpFrom, Long smtpPort, Boolean authRequired,
        String authUsername, String authPassword,
        List<EmailConfigurationsProperties> emailConfigurationsProperties) {
        this.configurationId = configurationId;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.smtpHost = smtpHost;
        this.smtpFrom = smtpFrom;
        this.smtpPort = smtpPort;
        this.authRequired = authRequired;
        this.authUsername = authUsername;
        this.authPassword = authPassword;
        this.emailConfigurationsProperties = emailConfigurationsProperties;
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

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getSmtpFrom() {
        return smtpFrom;
    }

    public void setSmtpFrom(String smtpFrom) {
        this.smtpFrom = smtpFrom;
    }

    public Long getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(Long smtpPort) {
        this.smtpPort = smtpPort;
    }

    public Boolean getAuthRequired() {
        return authRequired;
    }

    public void setAuthRequired(Boolean authRequired) {
        this.authRequired = authRequired;
    }

    public String getAuthUsername() {
        return authUsername;
    }

    public void setAuthUsername(String authUsername) {
        this.authUsername = authUsername;
    }

    public String getAuthPassword() {
        return authPassword;
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    public List<EmailConfigurationsProperties> getEmailConfigurationsProperties() {
        return emailConfigurationsProperties;
    }

    public void setEmailConfigurationsProperties(List<EmailConfigurationsProperties> emailConfigurationsProperties) {
        this.emailConfigurationsProperties = emailConfigurationsProperties;
    }
}
