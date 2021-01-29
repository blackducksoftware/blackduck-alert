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
package com.synopsys.integration.azure.boards.common.service.board;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.synopsys.integration.azure.boards.common.model.ReferenceLinkModel;

public class BoardResponseModel {
    private String id;
    private String name;
    private List<BoardColumnModel> columns;
    private List<BoardRowModel> rows;
    private BoardFieldsModel fields;
    private JsonObject allowedMappings;
    private Boolean canEdit;
    private Boolean isValid;
    private Integer revision;
    private String url;
    private Map<String, ReferenceLinkModel> _links;

    public BoardResponseModel() {
        // For serialization
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<BoardColumnModel> getColumns() {
        return columns;
    }

    public List<BoardRowModel> getRows() {
        return rows;
    }

    public BoardFieldsModel getFields() {
        return fields;
    }

    public JsonObject getAllowedMappings() {
        return allowedMappings;
    }

    public Boolean getCanEdit() {
        return canEdit;
    }

    public Boolean getValid() {
        return isValid;
    }

    public Integer getRevision() {
        return revision;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, ReferenceLinkModel> get_links() {
        return _links;
    }

}
