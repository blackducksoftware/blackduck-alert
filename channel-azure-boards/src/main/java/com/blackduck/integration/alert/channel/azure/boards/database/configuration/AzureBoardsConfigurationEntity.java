package com.blackduck.integration.alert.channel.azure.boards.database.configuration;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.synopsys.integration.alert.database.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "alert", name = "configuration_azure_boards")
public class AzureBoardsConfigurationEntity extends BaseEntity {
    @Id
    @Column(name = "configuration_id")
    private UUID configurationId;
    @Column(name = "name")
    private String name;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;
    @Column(name = "organization_name")
    private String organizationName;
    @Column(name = "app_id")
    private String appId;
    @Column(name = "client_secret")
    private String clientSecret;

    public AzureBoardsConfigurationEntity() {
    }

    public AzureBoardsConfigurationEntity(
        UUID configurationId,
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime lastUpdated,
        String organizationName,
        String appId,
        String clientSecret
    ) {
        this.configurationId = configurationId;
        this.name = name;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.organizationName = organizationName;
        this.appId = appId;
        this.clientSecret = clientSecret;
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

    public String getOrganizationName() {
        return organizationName;
    }

    public String getAppId() {
        return appId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
