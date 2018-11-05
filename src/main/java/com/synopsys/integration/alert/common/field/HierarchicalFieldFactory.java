package com.synopsys.integration.alert.common.field;

import java.util.List;

import com.synopsys.integration.alert.common.enumeration.FieldContentIdentifier;

public class HierarchicalFieldFactory {
    // TODO provide something more strictly typed
    public static <T> HierarchicalField<T> createObjectField(final List<String> fieldPath, final FieldContentIdentifier contentIdentifier, final String label, final Class<T> fieldClazz) {
        return new HierarchicalField<>(fieldPath, getInnerMostFieldName(fieldPath), contentIdentifier, label);
    }

    public static HierarchicalField<Long> createLongField(final List<String> fieldPath, final FieldContentIdentifier contentIdentifier, final String label) {
        return new HierarchicalField<>(fieldPath, getInnerMostFieldName(fieldPath), contentIdentifier, label);
    }

    public static HierarchicalField<String> createStringField(final List<String> fieldPath, final FieldContentIdentifier contentIdentifier, final String label) {
        return new HierarchicalField<>(fieldPath, getInnerMostFieldName(fieldPath), contentIdentifier, label);
    }

    public static HierarchicalField<String> createStringField(final List<String> fieldPath, final FieldContentIdentifier contentIdentifier, final String label, final String configNameMapping) {
        return new HierarchicalField<>(fieldPath, getInnerMostFieldName(fieldPath), contentIdentifier, label, configNameMapping);
    }

    private static String getInnerMostFieldName(final List<String> fieldPath) {
        if (fieldPath != null && !fieldPath.isEmpty()) {
            return fieldPath.get(fieldPath.size() - 1);
        }
        return null;
    }
}
