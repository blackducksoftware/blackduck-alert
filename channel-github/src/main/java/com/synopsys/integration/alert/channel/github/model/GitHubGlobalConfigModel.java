package com.synopsys.integration.alert.channel.github.model;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.common.model.Obfuscated;
import com.synopsys.integration.alert.common.rest.model.ConfigWithMetadata;

public class GitHubGlobalConfigModel extends ConfigWithMetadata implements Obfuscated<GitHubGlobalConfigModel> {
    private static final long serialVersionUID = 6190207942888784566L;

    private String apiToken;
    private Boolean isApiTokenSet;
    private Long timeoutInSeconds;

    public GitHubGlobalConfigModel() {
        // for serialization
    }

    public GitHubGlobalConfigModel(final String id, final String name, final String apiToken, final Boolean isApiTokenSet, final Long timeoutInSeconds) {
        super(id, name);
        this.apiToken = apiToken;
        this.isApiTokenSet = isApiTokenSet;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public GitHubGlobalConfigModel(
        final String id,
        final String name,
        final String createdAt,
        final String lastUpdated,
        final String apiToken,
        final Boolean isApiTokenSet,
        final Long timeoutInSeconds
    ) {
        super(id, name, createdAt, lastUpdated);
        this.apiToken = apiToken;
        this.isApiTokenSet = isApiTokenSet;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public String getApiToken() {
        return apiToken;
    }

    public Long getTimeoutInSeconds() {
        return timeoutInSeconds;
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
