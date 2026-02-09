package com.blackduck.integration.alert.common.system;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class SystemInfo extends AlertSerializableModel {
    private String version;
    private String created;
    private String description;
    private String projectUrl;
    private String commitHash;
    private String copyrightYear;

    protected SystemInfo() {
        //default constructor for serialization
    }

    public SystemInfo(
        String version,
        String created,
        String description,
        String projectUrl,
        String commitHash,
        String copyrightYear
    ) {
        this.version = version;
        this.created = created;
        this.description = description;
        this.projectUrl = projectUrl;
        this.commitHash = commitHash;
        this.copyrightYear = copyrightYear;
    }

    public String getVersion() {
        return version;
    }

    public String getCreated() {
        return created;
    }

    public String getDescription() {
        return description;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public String getCopyrightYear() {
        return copyrightYear;
    }
}
