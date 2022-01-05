/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.project;

import com.google.gson.JsonElement;

public class TeamProjectReferenceResponseModel {
    private String id;
    private String name;
    private String description;
    private String url;
    private JsonElement state;

    public TeamProjectReferenceResponseModel() {
        // For serialization
    }

    public TeamProjectReferenceResponseModel(String id, String name, String description, String url, JsonElement state) {
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

    public JsonElement getState() {
        return state;
    }

}
