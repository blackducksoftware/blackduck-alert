package com.synopsys.integration.azure.boards.common.service.project;

import com.google.gson.JsonObject;

public class TeamProjectReferenceResponseModel {
    private String id;
    private String name;
    private String description;
    private String url;
    private JsonObject state;

    public TeamProjectReferenceResponseModel() {
        // For serialization
    }

    public TeamProjectReferenceResponseModel(String id, String name, String description, String url, JsonObject state) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.url = url;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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

}
