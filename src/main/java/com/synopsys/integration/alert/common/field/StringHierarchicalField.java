package com.synopsys.integration.alert.common.field;

import java.lang.reflect.Type;
import java.util.Collection;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.alert.common.enumeration.FieldContentIdentifier;

public class StringHierarchicalField extends HierarchicalField {

    private static final Type TYPE = new TypeToken<String>() {}.getType();

    public StringHierarchicalField(final Collection<String> pathToField, final String innerMostFieldName, final FieldContentIdentifier contentIdentifier, final String label) {
        super(pathToField, innerMostFieldName, contentIdentifier, label, TYPE);
    }

    public StringHierarchicalField(final Collection<String> pathToField, final String innerMostFieldName, final FieldContentIdentifier contentIdentifier, final String label, final String configNameMapping) {
        super(pathToField, innerMostFieldName, contentIdentifier, label, configNameMapping, TYPE);
    }
}
