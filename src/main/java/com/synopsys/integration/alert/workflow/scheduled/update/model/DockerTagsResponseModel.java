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

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class DockerTagsResponseModel extends AlertSerializableModel {
    public static final DockerTagsResponseModel EMPTY = new DockerTagsResponseModel();

    static {
        EMPTY.count = 0;
        EMPTY.next = null;
        EMPTY.previous = null;
        EMPTY.results = List.of();
    }

    private int count;
    private String next;
    private String previous;
    private List<DockerTagModel> results;

    public DockerTagsResponseModel() {
    }

    public DockerTagsResponseModel(final int count, final String next, final String previous, final List<DockerTagModel> results) {
        this.count = count;
        this.next = next;
        this.previous = previous;
        this.results = results;
    }

    public int getCount() {
        return count;
    }

    public boolean hasNextPage() {
        return null != next;
    }

    public String getNextPageUrl() {
        return next;
    }

    public boolean hasPreviousPage() {
        return null != previous;
    }

    public String getPreviousPageUrl() {
        return previous;
    }

    public List<DockerTagModel> getResults() {
        return results;
    }

    public boolean isEmpty() {
        return count == 0 || results.isEmpty();
    }

}
