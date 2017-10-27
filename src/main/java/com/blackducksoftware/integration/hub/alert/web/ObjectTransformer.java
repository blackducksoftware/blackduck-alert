/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.web;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;

@Component
public class ObjectTransformer {

    public <T> List<T> tranformObjects(final List<?> objects, final Class<T> newClass) throws IntegrationException {
        final List<T> newList = new ArrayList<>();
        for (final Object object : objects) {
            final T newObject = tranformObject(object, newClass);
            newList.add(newObject);
        }
        return newList;
    }

    public <T> T tranformObject(final Object object, final Class<T> newClass) throws IntegrationException {
        if (null != object && newClass != null) {
            try {
                final T newClassObject = newClass.newInstance();

                final List<Field> fields = new ArrayList<>();
                fields.addAll(Arrays.asList(object.getClass().getDeclaredFields()));
                if (object.getClass().getSuperclass() != null) {
                    fields.addAll(Arrays.asList(object.getClass().getSuperclass().getDeclaredFields()));
                }

                final Map<String, Field> newFieldMap = new HashMap<>();
                for (final Field field : newClassObject.getClass().getDeclaredFields()) {
                    newFieldMap.put(field.getName(), field);
                }
                if (object.getClass().getSuperclass() != null) {
                    for (final Field field : newClassObject.getClass().getSuperclass().getDeclaredFields()) {
                        newFieldMap.put(field.getName(), field);
                    }
                }
                for (final Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    field.setAccessible(true);
                    final String fieldName = field.getName();
                    if (fieldName.equalsIgnoreCase("id")) {
                        System.out.println("");
                    }
                    final Field newField = newFieldMap.get(fieldName);
                    if (newField == null) {
                        throw new NoSuchFieldException("Could not find field '" + fieldName + "' in class " + newClass.getSimpleName());
                    }
                    newField.setAccessible(true);
                    if (String.class == field.getType()) {
                        final String oldField = (String) field.get(object);
                        if (String.class == newField.getType()) {
                            newField.set(newClassObject, oldField);
                        } else if (Integer.class == newField.getType()) {
                            newField.set(newClassObject, stringToInteger(oldField));
                        } else if (Long.class == newField.getType()) {
                            newField.set(newClassObject, stringToLong(oldField));
                        } else if (Boolean.class == newField.getType()) {
                            newField.set(newClassObject, stringToBoolean(oldField));
                        } else {
                            throw new IntegrationException(String.format("Could not transform object %s to %s because of field %s : The transformer does not support turning %s into %s", object.getClass().getSimpleName(),
                                    newClass.getSimpleName(), field.getName(), field.getType(), newField.getType()));
                        }
                    } else if (Integer.class == field.getType()) {
                        final Integer oldField = (Integer) field.get(object);
                        if (String.class == newField.getType()) {
                            newField.set(newClassObject, objectToString(oldField));
                        } else if (Integer.class == newField.getType()) {
                            newField.set(newClassObject, oldField);
                        } else {
                            throw new IntegrationException(String.format("Could not transform object %s to %s because of field %s : The transformer does not support turning %s into %s", object.getClass().getSimpleName(),
                                    newClass.getSimpleName(), field.getName(), field.getType(), newField.getType()));
                        }
                    } else if (Long.class == field.getType()) {
                        final Long oldField = (Long) field.get(object);
                        if (String.class == newField.getType()) {
                            newField.set(newClassObject, objectToString(oldField));
                        } else if (Long.class == newField.getType()) {
                            newField.set(newClassObject, oldField);
                        } else {
                            throw new IntegrationException(String.format("Could not transform object %s to %s because of field %s : The transformer does not support turning %s into %s", object.getClass().getSimpleName(),
                                    newClass.getSimpleName(), field.getName(), field.getType(), newField.getType()));
                        }
                    } else if (Boolean.class == field.getType()) {
                        final Boolean oldField = (Boolean) field.get(object);
                        if (String.class == newField.getType()) {
                            newField.set(newClassObject, objectToString(oldField));
                        } else if (Boolean.class == newField.getType()) {
                            newField.set(newClassObject, oldField);
                        } else {
                            throw new IntegrationException(String.format("Could not transform object %s to %s because of field %s : The transformer does not support turning %s into %s", object.getClass().getSimpleName(),
                                    newClass.getSimpleName(), field.getName(), field.getType(), newField.getType()));
                        }
                    } else {
                        throw new IntegrationException(String.format("Could not transform object %s to %s because of field %s : The transformer does not support turning %s into %s", object.getClass().getSimpleName(),
                                newClass.getSimpleName(), field.getName(), field.getType(), newField.getType()));
                    }
                }
                return newClassObject;
            } catch (IllegalAccessException | InstantiationException | SecurityException | NoSuchFieldException e) {
                throw new IntegrationException(String.format("Could not transform object %s to %s: %s", object.getClass().getSimpleName(), newClass.getSimpleName(), e.toString()));
            }
        }
        return null;
    }

    public Integer stringToInteger(final String value) {
        if (null != value) {
            try {
                return Integer.valueOf(value);
            } catch (final NumberFormatException e) {
            }
        }
        return null;
    }

    public Long stringToLong(final String value) {
        if (null != value) {
            try {
                return Long.valueOf(value);
            } catch (final NumberFormatException e) {
            }
        }
        return null;
    }

    public Boolean stringToBoolean(final String value) {
        if (null != value) {
            return Boolean.valueOf(value);
        }
        return null;
    }

    public String objectToString(final Object value) {
        if (null != value) {
            return String.valueOf(value);
        }
        return null;
    }

}
