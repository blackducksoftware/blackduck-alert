/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.update.model;

public class UpdateModel {
    private final String currentVersion;
    private final String currentCreatedDate;
    private final String dockerTagVersion;
    private final String dockerTagUpdatedDate;
    private final String repositoryUrl;
    private final boolean isUpdatable;

    public UpdateModel(String currentVersion, String currentCreatedDate, String dockerTagVersion, String dockerTagUpdatedDate, String repositoryUrl, boolean isUpdatable) {
        this.currentVersion = currentVersion;
        this.currentCreatedDate = currentCreatedDate;
        this.dockerTagVersion = dockerTagVersion;
        this.dockerTagUpdatedDate = dockerTagUpdatedDate;
        this.repositoryUrl = repositoryUrl;
        this.isUpdatable = isUpdatable;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getCurrentCreatedDate() {
        return currentCreatedDate;
    }

    public String getDockerTagVersion() {
        return dockerTagVersion;
    }

    public String getDockerTagUpdatedDate() {
        return dockerTagUpdatedDate;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public boolean getUpdatable() {
        return isUpdatable;
    }

}
