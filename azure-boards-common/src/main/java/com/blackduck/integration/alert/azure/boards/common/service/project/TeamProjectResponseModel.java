/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.service.project;

import java.util.Map;

import com.blackduck.integration.alert.azure.boards.common.model.ReferenceLinkModel;
import com.google.gson.JsonObject;

public class TeamProjectResponseModel {
    private String id;
    private String name;
    private String abbreviation;
    private String description;
    private String url;
    private JsonObject state;
    private String defaultTeamImageUrl;
    private String lastUpdateTime;
    private Integer revision;
    private JsonObject capabilities;
    private JsonObject defaultTeam;
    private JsonObject visibility;
    private Map<String, ReferenceLinkModel> _links;

    public TeamProjectResponseModel() {
        // For serialization
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public JsonObject getState() {
        return state;
    }

    public String getDefaultTeamImageUrl() {
        return defaultTeamImageUrl;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public Integer getRevision() {
        return revision;
    }

    public JsonObject getCapabilities() {
        return capabilities;
    }

    public JsonObject getDefaultTeam() {
        return defaultTeam;
    }

    public JsonObject getVisibility() {
        return visibility;
    }

    public Map<String, ReferenceLinkModel> getLinks() {
        return _links;
    }

}
