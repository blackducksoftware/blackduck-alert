package com.blackduck.integration.alert.api.oauth.database;

import java.util.Optional;
import java.util.UUID;

import com.blackduck.integration.util.Stringable;

public class AlertOAuthModel extends Stringable {
    private final UUID id;
    private final String accessToken;
    private final String refreshToken;
    private final Long exirationTimeMilliseconds;

    public AlertOAuthModel(UUID id, String accessToken, String refreshToken, Long exirationTimeMilliseconds) {
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
