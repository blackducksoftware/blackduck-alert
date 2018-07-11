/**
 * blackduck-alert
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
package com.blackducksoftware.integration.alert;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.alert.model.Model;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;

@Component
public class ObjectTransformer {
    private final Logger logger = LoggerFactory.getLogger(ObjectTransformer.class);
    private final ConversionService conversionService;

    public ObjectTransformer() {
        this.conversionService = new DefaultConversionService();
    }

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
        return convert(databaseEntity, newClass);
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
        return convert(configRestModel, newClass);
    }

    public <FROM extends Model, TO extends Model> TO convert(final FROM fromObject, final Class<TO> toClass) throws AlertException {
        if (fromObject != null && toClass != null) {
            final String fromClassName = fromObject.getClass().getSimpleName();
            final String toClassName = toClass.getSimpleName();
            try {
                final TO toObject = toClass.newInstance();
                final Map<String, Field> newFieldMap = createNewFieldMap(toObject);

                final List<Field> oldFieldList = createOldFieldList(fromObject);
                for (final Field oldField : oldFieldList) {
                    try {
                        if (Modifier.isStatic(oldField.getModifiers())) {
                            continue;
                        }
                        oldField.setAccessible(true);
                        final String oldFieldName = oldField.getName();
                        final Field newField = newFieldMap.get(oldFieldName);
                        if (newField == null) {
                            throw new NoSuchFieldException("Could not find field '" + oldFieldName + "' in class " + toClassName);
                        }
                        newField.setAccessible(true);

                        if (conversionService.canConvert(oldField.getType(), newField.getType())) {
                            newField.set(toObject, conversionService.convert(oldField.get(fromObject), newField.getType()));
                        } else {
                            throw new AlertException(String.format("Could not transform object %s to %s because of field %s : The transformer does not support turning %s into %s", fromObject.getClass().getSimpleName(),
                                    toObject.getClass().getSimpleName(), oldField.getName(), oldField.getType().getSimpleName(), newField.getType().getSimpleName()));
                        }
                    } catch (final NoSuchFieldException e) {
                        logger.trace(String.format("Could not find field %s from %s in %s", oldField.getName(), fromClassName, toClassName));
                        continue;
                    }
                }
                return toObject;
            } catch (IllegalAccessException | InstantiationException | SecurityException e) {
                throw new AlertException(String.format("Could not transform object %s to %s: %s", fromClassName, toClassName, e.toString()), e);
            }
        }
        return null;
    }

    public <T extends Object> T stringToObject(final String value, final Class<T> toClass) {
        if (conversionService.canConvert(String.class, toClass)) {
            try {
                return conversionService.convert(value, toClass);
            } catch (final IllegalArgumentException | ConversionException e) {
                logger.debug(e.getMessage());
            }
        }
        return null;
    }

    public String objectToString(final Object value) {
        if (value != null) {
            return String.valueOf(value);
        }
        return null;
    }

    public Long stringToLong(final String value) {
        return stringToObject(value, Long.class);
    }

    public Boolean stringToBoolean(final String value) {
        return stringToObject(value, Boolean.class);
    }

    private Map<String, Field> createNewFieldMap(final Object newClassObject) {
        final Map<String, Field> newFieldMap = new HashMap<>();
        Class<?> newClassHierarchy = newClassObject.getClass();
        while (newClassHierarchy != null) {
            for (final Field field : newClassHierarchy.getDeclaredFields()) {
                newFieldMap.put(field.getName(), field);
            }
            newClassHierarchy = newClassHierarchy.getSuperclass();
        }
        return newFieldMap;
    }

    private List<Field> createOldFieldList(final Object fromObject) {
        final List<Field> oldFieldList = new ArrayList<>();
        Class<?> fromObjectClassHierarchy = fromObject.getClass();
        while (fromObjectClassHierarchy != null) {
            oldFieldList.addAll(Arrays.asList(fromObjectClassHierarchy.getDeclaredFields()));
            fromObjectClassHierarchy = fromObjectClassHierarchy.getSuperclass();
        }
        return oldFieldList;
    }

}
