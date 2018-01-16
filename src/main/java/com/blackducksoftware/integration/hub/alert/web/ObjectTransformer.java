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
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.enumeration.StatusEnum;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.model.Model;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

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

    public DigestTypeEnum stringToDigestTypeEnum(final String value) {
        if (value != null) {
            try {
                return DigestTypeEnum.valueOf(value);
            } catch (final IllegalArgumentException e) {
            }
        }
        return null;
    }

    public String digestTypeEnumToString(final DigestTypeEnum digestTypeEnum) {
        if (digestTypeEnum != null) {
            return digestTypeEnum.name();
        }
        return null;
    }

    public NotificationCategoryEnum stringToNotificationCategoryEnum(final String value) {
        if (value != null) {
            try {
                return NotificationCategoryEnum.valueOf(value);
            } catch (final IllegalArgumentException e) {
            }
        }
        return null;
    }

    public String notificationCategoryEnumToString(final NotificationCategoryEnum notificationCategoryEnum) {
        if (notificationCategoryEnum != null) {
            return notificationCategoryEnum.name();
        }
        return null;
    }

    public StatusEnum stringToStatusEnum(final String value) {
        if (value != null) {
            try {
                return StatusEnum.valueOf(value);
            } catch (final IllegalArgumentException e) {
            }
        }
        return StatusEnum.FAILURE;
    }

    public String statusEnumToString(final StatusEnum statusEnum) {
        if (statusEnum != null) {
            return statusEnum.getDisplayName();
        }
        return null;
    }

    public String objectToString(final Object value) {
        if (value != null) {
            return String.valueOf(value);
        }
        return null;
    }

}
