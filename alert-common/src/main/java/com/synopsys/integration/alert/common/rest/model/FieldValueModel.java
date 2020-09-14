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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class FieldValueModel extends AlertSerializableModel {
    private static final long serialVersionUID = -4163785381973494922L;
    private Collection<String> values;
    private boolean isSet;

    private FieldValueModel() {
        this(null, false);
    }

    public FieldValueModel(Collection<String> values, boolean isSet) {
        setValues(values);
        this.isSet = isSet;
    }

    public Collection<String> getValues() {
        if (null != values) {
            return values;
        }
        return Set.of();
    }

    public void setValues(Collection<String> values) {
        this.values = values;
        cleanValues();
    }

    public Optional<String> getValue() {
        return getValues().stream().findFirst();
    }

    public void setValue(String value) {
        if (null == value) {
            setValues(Set.of());
        } else {
            setValues(Set.of(value));
        }
    }

    // DO NOT CHANGE this method name unless you change the UI. Alert is serializing this object in controllers. Spring uses Jackson which uses getters and setters to serialize fields.
    // Jackson will remove the is, get, and set prefixes from method names to determine the field names for the JSON object.  This changes what the UI expects so get was added to the method name.
    public boolean getIsSet() {
        return isSet;
    }

    public void setIsSet(boolean isSet) {
        this.isSet = isSet;
    }

    public boolean hasValues() {
        return !getValues().isEmpty();
    }

    public boolean containsNoData() {
        return !hasValues() && !getIsSet();
    }

    private void cleanValues() {
        values = getValues().stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());
    }
}
