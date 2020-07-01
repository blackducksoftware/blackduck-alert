package com.synopsys.integration.azure.boards.common.model;

import com.google.gson.JsonObject;

public class ReferenceLinksModel {
    private JsonObject links;

    public ReferenceLinksModel() {
        // For serialization
    }

    public ReferenceLinksModel(JsonObject links) {
        this.links = links;
    }

    public JsonObject getLinks() {
        return links;
    }

}
