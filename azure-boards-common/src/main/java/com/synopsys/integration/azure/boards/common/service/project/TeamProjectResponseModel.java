/*
 * azure-boards-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.azure.boards.common.service.project;

import java.util.Map;

import com.google.gson.JsonObject;
import com.synopsys.integration.azure.boards.common.model.ReferenceLinkModel;

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
