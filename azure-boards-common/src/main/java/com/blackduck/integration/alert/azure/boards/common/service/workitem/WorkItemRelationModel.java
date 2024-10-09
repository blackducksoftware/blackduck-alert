package com.blackduck.integration.alert.azure.boards.common.service.workitem;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class WorkItemRelationModel {
    private JsonObject attributes;
    @SerializedName("rel")
    private String relationType;
    private String url;

    public WorkItemRelationModel() {
        // For serialization
    }

    public WorkItemRelationModel(JsonObject attributes, String relationType, String url) {
        this.attributes = attributes;
        this.relationType = relationType;
        this.url = url;
    }

    public JsonObject getAttributes() {
        return attributes;
    }

    public String getRelationType() {
        return relationType;
    }

    public String getUrl() {
        return url;
    }

}
