/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.common.descriptor.config.field;

import java.util.Collection;

import com.synopsys.integration.alert.common.enumeration.FieldGroup;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class SelectConfigField extends ConfigField {
    private Collection<String> options;
    private boolean searchable;
    private boolean multiSelect;

    public SelectConfigField(final String key, final String label, final boolean required, final boolean sensitive, final boolean searchable, final boolean multiSelect, final Collection<String> options) {
        super(key, label, FieldType.SELECT.getFieldTypeName(), required, sensitive, FieldGroup.DEFAULT, "");
        this.searchable = searchable;
        this.multiSelect = multiSelect;
        this.options = options;
    }

    public SelectConfigField(final String key, final String label, final boolean required, final boolean sensitive, final Collection<String> options) {
        this(key, label, required, sensitive, true, false, options);
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(final boolean searchable) {
        this.searchable = searchable;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(final boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    public Collection<String> getOptions() {
        return options;
    }

    public void setOptions(final Collection<String> options) {
        this.options = options;
    }
}
