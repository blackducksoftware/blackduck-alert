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
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table;

import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.EndpointField;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class EndpointTableSelectField extends EndpointField {
    private boolean paged;
    private boolean searchable;
    private List<TableSelectColumn> columns;

    public EndpointTableSelectField(String key, String label, String description) {
        super(key, label, description, FieldType.TABLE_SELECT_INPUT, "Select", CustomEndpointManager.CUSTOM_ENDPOINT_URL);
        this.paged = false;
        this.searchable = true;
        columns = new LinkedList<>();
    }

    public EndpointTableSelectField applyPaged(boolean paged) {
        this.paged = paged;
        return this;
    }

    public EndpointTableSelectField applySearchable(boolean searchable) {
        this.searchable = searchable;
        return this;
    }

    public EndpointTableSelectField applyColumns(List<TableSelectColumn> columns) {
        if (columns != null) {
            this.columns.addAll(columns);
        }
        return this;
    }

    public EndpointTableSelectField applyColumn(TableSelectColumn tableSelectColumn) {
        if (null != tableSelectColumn) {
            columns.add(tableSelectColumn);
        }
        return this;
    }

    public boolean isPaged() {
        return paged;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public List<TableSelectColumn> getColumns() {
        return columns;
    }

}
