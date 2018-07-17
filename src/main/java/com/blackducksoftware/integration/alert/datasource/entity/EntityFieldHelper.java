package com.blackducksoftware.integration.alert.datasource.entity;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;

public class EntityFieldHelper {
    public static EntityFieldHelper SOLE_INSTANCE = new EntityFieldHelper();

    public Map<String, String> getFieldDetailsToMap(Class<?> clazz) {
        Map<String, String> fieldDetails = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                final String propertyKey = field.getAnnotation(Column.class).name().toUpperCase();
                fieldDetails.put(propertyKey, field.getName());
            }
        }

        return fieldDetails;
    }
}
