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
package com.synopsys.integration.alert.common.descriptor.config.field.table;

import java.util.List;
import java.util.Map;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public abstract class TableSelectField extends ConfigField {
    private boolean paged;
    private boolean searchable;
    private List<TableSelectColumn> columns;

    public TableSelectField(final String key, final String label, final String description, final boolean required, final boolean sensitive, final boolean paged, final boolean searchable) {
        super(key, label, description, FieldType.TABLE_SELECT_INPUT.getFieldTypeName(), required, sensitive);
        this.paged = paged;
        this.searchable = searchable;
        columns = createTableColumns();
    }

    public abstract List<TableSelectColumn> createTableColumns();

    public abstract List<Map<String, String>> mapDataToColumns();

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
