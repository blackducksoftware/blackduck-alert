/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigWithMetadata extends Config {
    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("lastUpdated")
    private String lastUpdated;

    public ConfigWithMetadata() {
    }

    public ConfigWithMetadata(String id) {
        this(id, null, null);
    }

    public ConfigWithMetadata(String id, String createdAt, String lastUpdated) {
        super(id);
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
