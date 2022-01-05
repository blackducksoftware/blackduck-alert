/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigWithMetadata extends Config {
    @JsonProperty("name")
    private String name;
    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("lastUpdated")
    private String lastUpdated;

    public ConfigWithMetadata() {
    }

    public ConfigWithMetadata(String id, String name) {
        this(id, name, null, null);
    }

    public ConfigWithMetadata(String id, String name, String createdAt, String lastUpdated) {
        super(id);
        this.name = name;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
