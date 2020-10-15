/**
 * alert-common
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
package com.synopsys.integration.alert.common.rest.model;

import java.util.List;

import org.springframework.data.domain.Page;

import net.minidev.json.annotate.JsonIgnore;

public class AlertPagedModel<M extends AlertSerializableModel> extends AlertSerializableModel {
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";

    private final int totalPages;
    private final int currentPage;
    private final int pageSize;
    private final List<M> models;

    public AlertPagedModel(Page<?> page, List<M> models) {
        this(page.getTotalPages(), page.getNumber(), page.getSize(), models);
    }

    public AlertPagedModel(int totalPages, int currentPage, int pageSize, List<M> models) {
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.models = models;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    @JsonIgnore
    protected List<M> getModels() {
        return models;
    }

}
