package com.synopsys.integration.alert.workflow.update.model;

import org.apache.commons.lang3.StringUtils;

public class UpdateModel {
    private final String currentVersion;
    private final String latestAvailableVersion;
    private final String repositoryUrl;

    public UpdateModel(final String currentVersion, final String latestAvailableVersion, final String repositoryUrl) {
        this.currentVersion = currentVersion;
        this.latestAvailableVersion = latestAvailableVersion;
        this.repositoryUrl = repositoryUrl;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getLatestAvailableVersion() {
        return latestAvailableVersion;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public boolean isUpdatable() {
        if (StringUtils.isAnyBlank(currentVersion, latestAvailableVersion)) {
            return false;
        }
        return !currentVersion.equals(latestAvailableVersion);
    }

}
