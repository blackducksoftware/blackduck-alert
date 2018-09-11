package com.synopsys.integration.alert.common.field;

import java.lang.reflect.Type;
import java.util.Collection;

import com.synopsys.integration.alert.common.enumeration.FieldContentIdentifier;

public class ObjectHierarchicalField extends HierarchicalField {

    public ObjectHierarchicalField(final Collection<String> pathToField, final String innerMostFieldName, final FieldContentIdentifier contentIdentifier, final String label,
        final Type type) {
        super(pathToField, innerMostFieldName, contentIdentifier, label, type);
    }

    public ObjectHierarchicalField(final Collection<String> pathToField, final String innerMostFieldName, final FieldContentIdentifier contentIdentifier, final String label,
        final String configNameMapping, final Type type) {
        super(pathToField, innerMostFieldName, contentIdentifier, label, configNameMapping, type);
    }
}
