/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
