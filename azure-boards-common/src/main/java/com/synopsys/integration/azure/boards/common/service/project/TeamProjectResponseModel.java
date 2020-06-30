package com.synopsys.integration.azure.boards.common.service.project;

import com.google.gson.JsonObject;
import com.synopsys.integration.azure.boards.common.model.ReferenceLinksModel;

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
    private ReferenceLinksModel _links;

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

    public ReferenceLinksModel getLinks() {
        return _links;
    }

}
