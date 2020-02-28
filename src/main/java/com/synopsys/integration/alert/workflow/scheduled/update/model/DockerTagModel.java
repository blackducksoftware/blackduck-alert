/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.workflow.scheduled.update.model;

import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class DockerTagModel extends AlertSerializableModel {
    private String name;
    @SerializedName("full_size")
    private Long fullSize;
    //private List<Object> images;
    private Long id;
    private Long repository;
    private Long creator;
    @SerializedName("last_updater")
    private Long lastUpdater;
    @SerializedName("last_updated")
    private String lastUpdated;
    private boolean v2;

    public DockerTagModel() {
    }

    public DockerTagModel(String name, Long fullSize, Long id, Long repository, Long creator, Long lastUpdater, String lastUpdated, boolean v2) {
        this.name = name;
        this.fullSize = fullSize;
        this.id = id;
        this.repository = repository;
        this.creator = creator;
        this.lastUpdater = lastUpdater;
        this.lastUpdated = lastUpdated;
        this.v2 = v2;
    }

    public String getName() {
        return name;
    }

    public Long getFullSize() {
        return fullSize;
    }

    // If necessary, this can return a list of more specific Objects.
    //    public List<Object> getImages() {
    //        return images;
    //    }

    public Long getId() {
        return id;
    }

    public Long getRepository() {
        return repository;
    }

    public Long getCreator() {
        return creator;
    }

    public Long getLastUpdater() {
        return lastUpdater;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public boolean isV2() {
        return v2;
    }

}
