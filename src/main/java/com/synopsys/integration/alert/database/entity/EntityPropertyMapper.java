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
package com.synopsys.integration.alert.database.entity;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.workflow.startup.AlertStartupProperty;

@Component
public class EntityPropertyMapper {
    public static final String ALERT_PROPERTY_PREFIX = "ALERT_";

    public Map<String, AlertStartupProperty> mapEntityToProperties(final String entityName, final Class<?> entityClass) {
        final String propertyNamePrefix = ALERT_PROPERTY_PREFIX + entityName + "_";
        final Map<String, AlertStartupProperty> fieldMapping = new HashMap<>();
        for (final Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                final String propertyKey = (propertyNamePrefix + field.getAnnotation(Column.class).name()).toUpperCase();
                final String fieldName = field.getName();
                final AlertStartupProperty startupProperty = new AlertStartupProperty(propertyKey, fieldName);
                fieldMapping.put(startupProperty.getFieldName(), startupProperty);
            }
        }
        return fieldMapping;
    }
}
