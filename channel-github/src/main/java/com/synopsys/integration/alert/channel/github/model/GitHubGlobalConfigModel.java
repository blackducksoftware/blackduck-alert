package com.synopsys.integration.alert.channel.github.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.common.model.Obfuscated;
import com.synopsys.integration.alert.common.rest.model.ConfigWithMetadata;

public class GitHubGlobalConfigModel extends ConfigWithMetadata implements Obfuscated<GitHubGlobalConfigModel> {
    private String apiToken;
    private Boolean isApiTokenSet;
    private Long timeoutInSeconds;

    public GitHubGlobalConfigModel() {
        // for serialization
    }

    public GitHubGlobalConfigModel(String id, String name, String apiToken, Boolean isApiTokenSet, Long timeoutInSeconds) {
        super(id, name);
        this.apiToken = apiToken;
        this.isApiTokenSet = isApiTokenSet;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public GitHubGlobalConfigModel(
        String id,
        String name,
        String createdAt,
        String lastUpdated,
        String apiToken,
        Boolean isApiTokenSet,
        Long timeoutInSeconds
    ) {
        super(id, name, createdAt, lastUpdated);
        this.apiToken = apiToken;
        this.isApiTokenSet = isApiTokenSet;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public Long getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public Optional<Boolean> getIsApiTokenSet() {
        return Optional.ofNullable(isApiTokenSet);
    }

    @Override
    public GitHubGlobalConfigModel obfuscate() {
        return new GitHubGlobalConfigModel(
            getId(),
            getName(),
            getCreatedAt(),
            getLastUpdated(),
            null,
            StringUtils.isNotBlank(apiToken),
            timeoutInSeconds
        );
    }
}
