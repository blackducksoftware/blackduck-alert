/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.model;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

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

    // since we return these objects now in the controllers Jackson will create a value attribute in the JSON unless we ignore it.
    // if we don't ignore it the setValue method is called with the value of the JSON object causing inconsistent state with the values attribute.
    @JsonIgnore
    public Optional<String> getValue() {
        return getValues().stream().findFirst();
    }

    @JsonIgnore
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
