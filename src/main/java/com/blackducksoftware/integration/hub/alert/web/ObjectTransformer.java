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
import com.blackducksoftware.integration.hub.alert.enumeration.ConversionTypeEnum;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.enumeration.StatusEnum;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.model.Model;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

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
        return performConversion(databaseEntity, newClass, ConversionTypeEnum.ENTITY_TO_REST_MODEL);
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
        return performConversion(configRestModel, newClass, ConversionTypeEnum.REST_MODEL_TO_ENTITY);
    }

    public <FROM extends Model, TO extends Model> TO performConversion(final FROM fromObject, final Class<TO> toClass, final ConversionTypeEnum conversionType) throws AlertException {
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
                        if (ConversionTypeEnum.ENTITY_TO_REST_MODEL.equals(conversionType)) {
                            setFieldToString(oldField, newField, fromObject, toObject);
                        } else if (ConversionTypeEnum.REST_MODEL_TO_ENTITY.equals(conversionType)) {
                            setFieldFromString(oldField, newField, fromObject, toObject);
                        } else {
                            throw new UnsupportedOperationException(String.format("{} does not support the conversion type: {}", getClass().getSimpleName(), conversionType));
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

    private <FROM extends Model, TO extends Model> void setFieldToString(final Field oldField, final Field newField, final FROM oldClassObject, final TO newClassObject)
            throws IllegalArgumentException, IllegalAccessException, AlertException {
        final Class<?> oldFieldType = oldField.getType();
        if (String.class == oldFieldType) {
            final String stringField = (String) oldField.get(oldClassObject);
            newField.set(newClassObject, stringField);
        } else if (Integer.class == oldFieldType) {
            final Integer integerField = (Integer) oldField.get(oldClassObject);
            newField.set(newClassObject, objectToString(integerField));
        } else if (Long.class == oldFieldType) {
            final Long longField = (Long) oldField.get(oldClassObject);
            newField.set(newClassObject, objectToString(longField));
        } else if (Boolean.class == oldFieldType) {
            final Boolean booleanField = (Boolean) oldField.get(oldClassObject);
            newField.set(newClassObject, objectToString(booleanField));
        } else if (Date.class == oldFieldType || java.sql.Date.class == oldFieldType) {
            final Date dateField = (Date) oldField.get(oldClassObject);
            newField.set(newClassObject, objectToString(dateField));
        } else if (DigestTypeEnum.class == oldFieldType) {
            final DigestTypeEnum digestTypeField = (DigestTypeEnum) oldField.get(oldClassObject);
            newField.set(newClassObject, digestTypeEnumToString(digestTypeField));
        } else if (NotificationCategoryEnum.class == oldFieldType) {
            final NotificationCategoryEnum notificationCategoryField = (NotificationCategoryEnum) oldField.get(oldClassObject);
            newField.set(newClassObject, notificationCategoryEnumToString(notificationCategoryField));
        } else if (StatusEnum.class == oldFieldType) {
            final StatusEnum statusField = (StatusEnum) oldField.get(oldClassObject);
            newField.set(newClassObject, statusEnumToString(statusField));
        } else {
            throw new AlertException(String.format("Could not transform object %s to %s because of field %s : The transformer does not support turning %s into %s", oldClassObject.getClass().getSimpleName(),
                    newClassObject.getClass().getSimpleName(), oldField.getName(), oldFieldType.getSimpleName(), newField.getType().getSimpleName()));
        }
    }

    private <FROM extends Model, TO extends Model> void setFieldFromString(final Field oldField, final Field newField, final FROM oldClassObject, final TO newClassObject)
            throws IllegalArgumentException, IllegalAccessException, AlertException {
        final String oldFieldString = (String) oldField.get(oldClassObject);
        final Class<?> newFieldType = newField.getType();
        if (String.class == newFieldType) {
            newField.set(newClassObject, oldFieldString);
        } else if (Integer.class == newFieldType) {
            newField.set(newClassObject, stringToInteger(oldFieldString));
        } else if (Long.class == newFieldType) {
            newField.set(newClassObject, stringToLong(oldFieldString));
        } else if (Boolean.class == newFieldType) {
            newField.set(newClassObject, stringToBoolean(oldFieldString));
        } else if (Date.class == newFieldType || java.sql.Date.class == newFieldType) {
            newField.set(newClassObject, stringToDate(oldFieldString));
        } else if (DigestTypeEnum.class == newFieldType) {
            newField.set(newClassObject, stringToDigestTypeEnum(oldFieldString));
        } else if (NotificationCategoryEnum.class == newFieldType) {
            newField.set(newClassObject, stringToNotificationCategoryEnum(oldFieldString));
        } else if (StatusEnum.class == newFieldType) {
            newField.set(newClassObject, stringToStatusEnum(oldFieldString));
        } else {
            throw new AlertException(String.format("Could not transform object %s to %s because of field %s : The transformer does not support turning %s into %s", oldClassObject.getClass().getSimpleName(),
                    newClassObject.getClass().getSimpleName(), oldField.getName(), oldField.getType().getSimpleName(), newFieldType.getSimpleName()));
        }
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
