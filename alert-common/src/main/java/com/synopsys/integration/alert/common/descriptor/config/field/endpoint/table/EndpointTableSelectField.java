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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.EndpointField;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.ConfigValidationFunction;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class EndpointTableSelectField extends EndpointField {
    private boolean paged;
    private boolean searchable;
    private List<TableSelectColumn> columns;

    public EndpointTableSelectField(final String key, final String label, final String description, final boolean required, final boolean sensitive, final boolean paged, final boolean searchable) {
        super(key, label, description, FieldType.TABLE_SELECT_INPUT, required, sensitive, "Select", CustomEndpointManager.CUSTOM_ENDPOINT_URL);
        this.paged = paged;
        this.searchable = searchable;
        columns = new LinkedList<>();
    }

    public EndpointTableSelectField(final String key, final String label, final String description, final boolean required, final boolean sensitive, final boolean paged, final boolean searchable,
        ConfigValidationFunction... validationFunctions) {
        super(key, label, description, FieldType.TABLE_SELECT_INPUT, required, sensitive, "Select", CustomEndpointManager.CUSTOM_ENDPOINT_URL);
        this.paged = paged;
        this.searchable = searchable;
        columns = new LinkedList<>();
        this.setValidationFunctions(validationFunctions);
    }

    public static EndpointTableSelectField create(String key, String label, String description, boolean searchable) {
        return new EndpointTableSelectField(key, label, description, false, false, true, searchable);
    }

    public static EndpointTableSelectField createSearchable(String key, String label, String description) {
        return new EndpointTableSelectField(key, label, description, false, false, true, true);
    }

    public static EndpointTableSelectField createSearchable(String key, String label, String description, ConfigValidationFunction... validationFunctions) {
        return new EndpointTableSelectField(key, label, description, false, false, true, true, validationFunctions);
    }

    public boolean isPaged() {
        return paged;
    }

    public boolean isSearchable() {
        return searchable;
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
