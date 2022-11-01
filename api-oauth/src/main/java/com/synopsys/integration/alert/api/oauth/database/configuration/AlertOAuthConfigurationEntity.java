package com.synopsys.integration.alert.api.oauth.database.configuration;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

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
    @Column(name = "exipiration_time_ms")
    private Long exirationTimeMilliseconds;

    public AlertOAuthConfigurationEntity() {
        //default constructor for JPA
    }

    public AlertOAuthConfigurationEntity(final UUID id, final String accessToken, final String refreshToken, final Long exirationTimeMilliseconds) {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.exirationTimeMilliseconds = exirationTimeMilliseconds;
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

    public Long getExirationTimeMilliseconds() {
        return exirationTimeMilliseconds;
    }
}
