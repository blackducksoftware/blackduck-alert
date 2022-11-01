package com.synopsys.integration.alert.channel.github.database.configuration;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "alert", name = "configuration_github")
public class GitHubConfigurationEntity extends BaseEntity {
    private static final long serialVersionUID = 8760711403794861595L;
    @Id
    @Column(name = "configuration_id")
    private UUID configurationId;
    @Column(name = "name")
    private String name;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "last_updated")
    private OffsetDateTime lastUpdated;
    @Column(name = "api_token")
    private String apiToken;
    @Column(name = "timeout_seconds")
    private Long timeoutSeconds;

    public GitHubConfigurationEntity() {
    }

    public GitHubConfigurationEntity(
        UUID configurationId,
        String name,
        OffsetDateTime createdAt,
        OffsetDateTime lastUpdated,
        String apiToken,
        Long timeoutSeconds
    ) {
        this.configurationId = configurationId;
        this.name = name;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.apiToken = apiToken;
        this.timeoutSeconds = timeoutSeconds;
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

    public String getApiToken() {
        return apiToken;
    }

    public Long getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
