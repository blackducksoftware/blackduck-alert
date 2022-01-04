/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.update.model;

import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class DockerTagModel extends AlertSerializableModel {
    // This is not all of the data that Docker provides for tags. These are just the fields we use.
    private String name;
    private Long id;
    private Long repository;
    @SerializedName("last_updated")
    private String lastUpdated;

    public DockerTagModel() {
    }

    public DockerTagModel(String name, Long id, Long repository, String lastUpdated) {
        this.name = name;
        this.id = id;
        this.repository = repository;
        this.lastUpdated = lastUpdated;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public Long getRepository() {
        return repository;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

}
