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

    public HideCheckboxConfigField addHiddenFieldKey(String key) {
        relatedHiddenFields.add(key);
        return this;
    }

    public HideCheckboxConfigField addHiddenFieldKeys(String... key) {
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
