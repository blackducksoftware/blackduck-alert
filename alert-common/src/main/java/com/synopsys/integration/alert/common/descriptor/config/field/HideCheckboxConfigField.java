/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.alert.common.enumeration.FieldType;

public class HideCheckboxConfigField extends CheckboxConfigField {
    private List<String> relatedHiddenFields;

    public HideCheckboxConfigField(String key, String label, String description) {
        super(key, label, description, FieldType.HIDE_CHECKBOX_INPUT);
        relatedHiddenFields = new LinkedList<>();
    }

    public HideCheckboxConfigField applyRelatedHiddenFieldKey(String key) {
        if (null != key) {
            relatedHiddenFields.add(key);
        }
        return this;
    }

    public HideCheckboxConfigField applyRelatedHiddenFieldKeys(String... keys) {
        if (null != keys) {
            relatedHiddenFields.addAll(Stream.of(keys).collect(Collectors.toList()));
        }
        return this;
    }

    public List<String> getRelatedHiddenFields() {
        return relatedHiddenFields;
    }

}
