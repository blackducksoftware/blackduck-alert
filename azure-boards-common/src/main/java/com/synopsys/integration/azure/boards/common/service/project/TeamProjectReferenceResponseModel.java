/**
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
