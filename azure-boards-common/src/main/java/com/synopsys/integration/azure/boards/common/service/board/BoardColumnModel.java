/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.board;

import com.google.gson.JsonObject;

public class BoardColumnModel {
    private String id;
    private String name;
    private String description;
    private String columnType;
    private Boolean isSplit;
    private Integer itemLimit;
    private JsonObject stateMappings;

    public BoardColumnModel() {
        // For serialization
    }

    public BoardColumnModel(String id, String name, String description, String columnType, Boolean isSplit, Integer itemLimit, JsonObject stateMappings) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.columnType = columnType;
        this.isSplit = isSplit;
        this.itemLimit = itemLimit;
        this.stateMappings = stateMappings;
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

    public String getColumnType() {
        return columnType;
    }

    public Boolean getSplit() {
        return isSplit;
    }

    public Integer getItemLimit() {
        return itemLimit;
    }

    public JsonObject getStateMappings() {
        return stateMappings;
    }

}
