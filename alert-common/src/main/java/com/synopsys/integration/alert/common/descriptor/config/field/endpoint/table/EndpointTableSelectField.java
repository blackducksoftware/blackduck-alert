/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigValidationFunction;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class EndpointTableSelectField extends ConfigField {
    private boolean paged;
    private boolean searchable;
    private String endpoint;
    private List<TableSelectColumn> columns;

    public static EndpointTableSelectField create(String key, String label, String description, boolean searchable) {
        return new EndpointTableSelectField(key, label, description, false, false, true, searchable);
    }

    public static EndpointTableSelectField createSearchable(String key, String label, String description) {
        return new EndpointTableSelectField(key, label, description, false, false, true, true);
    }

    public static EndpointTableSelectField createSearchable(String key, String label, String description, ConfigValidationFunction validationFunction) {
        return new EndpointTableSelectField(key, label, description, false, false, true, true, validationFunction);
    }

    public EndpointTableSelectField(final String key, final String label, final String description, final boolean required, final boolean sensitive, final boolean paged, final boolean searchable) {
        super(key, label, description, FieldType.TABLE_SELECT_INPUT.getFieldTypeName(), required, sensitive);
        this.paged = paged;
        this.searchable = searchable;
        endpoint = CustomEndpointManager.CUSTOM_ENDPOINT_URL;
        columns = new LinkedList<>();
    }

    public EndpointTableSelectField(final String key, final String label, final String description, final boolean required, final boolean sensitive, final boolean paged, final boolean searchable,
        ConfigValidationFunction validationFunction) {
        super(key, label, description, FieldType.TABLE_SELECT_INPUT.getFieldTypeName(), required, sensitive, validationFunction);
        this.paged = paged;
        this.searchable = searchable;
        endpoint = CustomEndpointManager.CUSTOM_ENDPOINT_URL;
        columns = new LinkedList<>();
    }

    public boolean isPaged() {
        return paged;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public EndpointTableSelectField addColumn(TableSelectColumn tableSelectColumn) {
        columns.add(tableSelectColumn);
        return this;
    }

    public EndpointTableSelectField addColumns(TableSelectColumn... tableSelectColumns) {
        columns.addAll(Stream.of(tableSelectColumns).collect(Collectors.toList()));
        return this;
    }

    public List<TableSelectColumn> getColumns() {
        return columns;
    }
}
