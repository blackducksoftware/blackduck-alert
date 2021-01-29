/*
 * blackduck-alert
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
package com.synopsys.integration.alert.workflow.scheduled.update.model;

import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class DockerTagModel extends AlertSerializableModel {
    // This is not all of the data that Docker provides for tags. These are just the fields we use.
    private String name;
    private Long id;
    private Long repository;
    @SerializedName("last_updated")
    private String lastUpdated;

    public DockerTagModel() {
    }

    public DockerTagModel(String name, Long id, Long repository, String lastUpdated) {
        this.name = name;
        this.id = id;
        this.repository = repository;
        this.lastUpdated = lastUpdated;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public Long getRepository() {
        return repository;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

}
