package com.synopsys.integration.alert.api.oauth.database;

import java.util.Optional;
import java.util.UUID;

public class AlertOAuthModel {
    private final UUID id;
    private final String accessToken;
    private final String refreshToken;
    private final Long exirationTimeMilliseconds;

    public AlertOAuthModel(final UUID id, final String accessToken, final String refreshToken, final Long exirationTimeMilliseconds) {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.exirationTimeMilliseconds = exirationTimeMilliseconds;
    }

    public UUID getId() {
        return id;
    }

    public Optional<String> getAccessToken() {
        return Optional.ofNullable(accessToken);
    }

    public Optional<String> getRefreshToken() {
        return Optional.ofNullable(refreshToken);
    }

    public Optional<Long> getExirationTimeMilliseconds() {
        return Optional.ofNullable(exirationTimeMilliseconds);
    }
}
