/**
 * hub-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.hub.alert.web;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.enumeration.StatusEnum;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

@Component
public class ObjectTransformer {
    private final Logger logger = LoggerFactory.getLogger(ObjectTransformer.class);

    public <T extends ConfigRestModel> List<T> databaseEntitiesToConfigRestModels(final List<? extends DatabaseEntity> databaseEntities, final Class<T> newClass) throws AlertException {
        final List<T> newList = new ArrayList<>();
        if (databaseEntities != null) {
            for (final DatabaseEntity databaseEntity : databaseEntities) {
                final T newObject = databaseEntityToConfigRestModel(databaseEntity, newClass);
                newList.add(newObject);
            }
        }
        return newList;
    }

    public <T extends ConfigRestModel> T databaseEntityToConfigRestModel(final DatabaseEntity databaseEntity, final Class<T> newClass) throws AlertException {
        if (databaseEntity != null && newClass != null) {
            final String databaseEntityClassName = databaseEntity.getClass().getSimpleName();
            final String newClassName = newClass.getSimpleName();
            try {
                final T newClassObject = newClass.newInstance();

                final List<Field> fields = new ArrayList<>();
                Class<?> databaseEntityClassHierarchy = databaseEntity.getClass();
                while (databaseEntityClassHierarchy != null) {
                    fields.addAll(Arrays.asList(databaseEntityClassHierarchy.getDeclaredFields()));
                    databaseEntityClassHierarchy = databaseEntityClassHierarchy.getSuperclass();
                }

                final Map<String, Field> newFieldMap = new HashMap<>();
                Class<?> newClassHierarchy = newClassObject.getClass();
                while (newClassHierarchy != null) {
                    for (final Field field : newClassHierarchy.getDeclaredFields()) {
                        newFieldMap.put(field.getName(), field);
                    }
                    newClassHierarchy = newClassHierarchy.getSuperclass();
                }
                for (final Field field : fields) {
                    try {
                        if (Modifier.isStatic(field.getModifiers())) {
                            continue;
                        }
                        field.setAccessible(true);
                        final String fieldName = field.getName();
                        final Field newField = newFieldMap.get(fieldName);
                        if (newField == null) {
                            throw new NoSuchFieldException("Could not find field '" + fieldName + "' in class " + newClassName);
                        }
                        newField.setAccessible(true);
                        if (String.class == field.getType()) {
                            final String oldField = (String) field.get(databaseEntity);
                            newField.set(newClassObject, oldField);
                        } else if (Integer.class == field.getType()) {
                            final Integer oldField = (Integer) field.get(databaseEntity);
                            newField.set(newClassObject, objectToString(oldField));
                        } else if (Long.class == field.getType()) {
                            final Long oldField = (Long) field.get(databaseEntity);
                            newField.set(newClassObject, objectToString(oldField));
                        } else if (Boolean.class == field.getType()) {
                            final Boolean oldField = (Boolean) field.get(databaseEntity);
                            newField.set(newClassObject, objectToString(oldField));
                        } else if (Date.class == field.getType()) {
                            final Date oldField = (Date) field.get(databaseEntity);
                            newField.set(newClassObject, objectToString(oldField));
                        } else if (StatusEnum.class == field.getType()) {
                            final StatusEnum oldField = (StatusEnum) field.get(databaseEntity);
                            newField.set(newClassObject, statusEnumToString(oldField));
                        } else {
                            throw new AlertException(String.format("Could not transform object %s to %s because of field %s : The transformer does not support turning %s into %s", databaseEntityClassName, newClassName, field.getName(),
                                    field.getType().getSimpleName(), newField.getType().getSimpleName()));
                        }
                    } catch (final NoSuchFieldException e) {
                        logger.trace(String.format("Could not find field %s from %s in %s", field.getName(), databaseEntityClassName, newClassName));
                        continue;
                    }
                }
                return newClassObject;
            } catch (IllegalAccessException | InstantiationException | SecurityException e) {
                throw new AlertException(String.format("Could not transform object %s to %s: %s", databaseEntityClassName, newClassName, e.toString()));
            }
        }
        return null;
    }

    public <T extends DatabaseEntity> List<T> configRestModelsToDatabaseEntities(final List<ConfigRestModel> configRestModels, final Class<T> newClass) throws AlertException {
        final List<T> newList = new ArrayList<>();
        if (configRestModels != null) {
            for (final ConfigRestModel configRestModel : configRestModels) {
                final T newObject = configRestModelToDatabaseEntity(configRestModel, newClass);
                newList.add(newObject);
            }
        }
        return newList;
    }

    public <T extends DatabaseEntity> T configRestModelToDatabaseEntity(final ConfigRestModel configRestModel, final Class<T> newClass) throws AlertException {
        if (configRestModel != null && newClass != null) {
            final String configRestModelClassName = configRestModel.getClass().getSimpleName();
            final String newClassName = newClass.getSimpleName();
            try {
                final T newClassObject = newClass.newInstance();

                final List<Field> fields = new ArrayList<>();
                Class<?> configRestModelClassHierarchy = configRestModel.getClass();
                while (configRestModelClassHierarchy != null) {
                    fields.addAll(Arrays.asList(configRestModelClassHierarchy.getDeclaredFields()));
                    configRestModelClassHierarchy = configRestModelClassHierarchy.getSuperclass();
                }

                final Map<String, Field> newFieldMap = new HashMap<>();
                Class<?> newClassHierarchy = newClassObject.getClass();
                while (newClassHierarchy != null) {
                    for (final Field field : newClassHierarchy.getDeclaredFields()) {
                        newFieldMap.put(field.getName(), field);
                    }
                    newClassHierarchy = newClassHierarchy.getSuperclass();
                }
                for (final Field field : fields) {
                    try {
                        if (Modifier.isStatic(field.getModifiers())) {
                            continue;
                        }
                        field.setAccessible(true);
                        final String fieldName = field.getName();
                        final Field newField = newFieldMap.get(fieldName);
                        if (newField == null) {
                            throw new NoSuchFieldException("Could not find field '" + fieldName + "' in class " + newClassName);
                        }
                        newField.setAccessible(true);
                        final String oldField = (String) field.get(configRestModel);
                        if (String.class == newField.getType()) {
                            newField.set(newClassObject, oldField);
                        } else if (Integer.class == newField.getType()) {
                            newField.set(newClassObject, stringToInteger(oldField));
                        } else if (Long.class == newField.getType()) {
                            newField.set(newClassObject, stringToLong(oldField));
                        } else if (Boolean.class == newField.getType()) {
                            newField.set(newClassObject, stringToBoolean(oldField));
                        } else if (Date.class == field.getType()) {
                            newField.set(newClassObject, stringToDate(oldField));
                        } else if (StatusEnum.class == field.getType()) {
                            newField.set(newClassObject, stringToStatusEnum(oldField));
                        } else {
                            throw new AlertException(String.format("Could not transform object %s to %s because of field %s : The transformer does not support turning %s into %s", configRestModelClassName, newClassName, field.getName(),
                                    field.getType().getSimpleName(), newField.getType().getSimpleName()));
                        }
                    } catch (final NoSuchFieldException e) {
                        logger.trace(String.format("Could not find field %s from %s in %s", field.getName(), configRestModelClassName, newClassName));
                        continue;
                    }
                }
                return newClassObject;
            } catch (final AlertException e) {
                throw e;
            } catch (IllegalAccessException | InstantiationException | SecurityException e) {
                throw new AlertException(String.format("Could not transform object %s to %s: %s", configRestModelClassName, newClassName, e.toString()), e);
            }
        }
        return null;
    }

    public Integer stringToInteger(final String value) {
        if (value != null) {
            final String trimmedValue = value.trim();
            try {
                return Integer.valueOf(trimmedValue);
            } catch (final NumberFormatException e) {
            }
        }
        return null;
    }

    public Long stringToLong(final String value) {
        if (value != null) {
            final String trimmedValue = value.trim();
            try {
                return Long.valueOf(trimmedValue);
            } catch (final NumberFormatException e) {
            }
        }
        return null;
    }

    public Boolean stringToBoolean(final String value) {
        if (value != null) {
            final String trimmedValue = value.trim();
            if (trimmedValue.equalsIgnoreCase("false")) {
                return false;
            } else if (trimmedValue.equalsIgnoreCase("true")) {
                return true;
            }
        }
        return null;
    }

    public Date stringToDate(final String value) {
        if (value != null) {
            final String trimmedValue = value.trim();
            try {
                return java.sql.Date.valueOf(trimmedValue);
            } catch (final Exception e) {
            }
        }
        return null;
    }

    public StatusEnum stringToStatusEnum(final String value) {
        if (value != null) {
            try {
                return StatusEnum.getStatusEnumFromString(value);
            } catch (final IllegalArgumentException e) {
            }
        }
        return StatusEnum.FAILURE;
    }

    public String statusEnumToString(final StatusEnum statusEnum) {
        return statusEnum.getDisplayName();
    }

    public String objectToString(final Object value) {
        if (value != null) {
            return String.valueOf(value);
        }
        return null;
    }

}
