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
package com.synopsys.integration.alert.common.descriptor.config.field;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.alert.common.enumeration.FieldType;

public class HideCheckboxConfigField extends CheckboxConfigField {
    private List<String> relatedHiddenFields;

    public static HideCheckboxConfigField create(final String key, final String label, final String description) {
        return new HideCheckboxConfigField(key, label, description, false);
    }

    public HideCheckboxConfigField(final String key, final String label, final String description, final boolean required) {
        super(key, label, description, FieldType.HIDE_CHECKBOX_INPUT, required);
        relatedHiddenFields = new LinkedList<>();
    }

    public HideCheckboxConfigField addRelatedHiddenFieldKey(String key) {
        relatedHiddenFields.add(key);
        return this;
    }

    public HideCheckboxConfigField addRelatedHiddenFieldKeys(String... key) {
        relatedHiddenFields.addAll(Stream.of(key).collect(Collectors.toList()));
        return this;
    }

    public List<String> getRelatedHiddenFields() {
        return relatedHiddenFields;
    }

    public void setRelatedHiddenFields(final List<String> relatedHiddenFields) {
        this.relatedHiddenFields = relatedHiddenFields;
    }
}
