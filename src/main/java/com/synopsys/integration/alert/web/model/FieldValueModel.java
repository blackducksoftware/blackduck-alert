package com.synopsys.integration.alert.web.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class FieldValueModel {
    private Collection<String> values;
    private boolean isSet;

    public FieldValueModel(final Collection<String> values, final boolean isSet) {
        this.values = values;
        this.isSet = isSet;
    }

    public Collection<String> getValues() {
        return values;
    }

    public void setValues(final Collection<String> values) {
        this.values = values;
    }

    public Optional<String> getValue() {
        return values.stream().findFirst();
    }

    public void setValue(String value) {
        this.values = Arrays.asList(value);
    }

    public boolean isSet() {
        return isSet;
    }

    public void setIsSet(final boolean set) {
        isSet = set;
    }
}
