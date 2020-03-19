/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.springframework.core.annotation.AnnotationUtils;

public class SensitiveFieldFinder {

    private SensitiveFieldFinder() {
        throw new IllegalStateException("Utility class");
    }

    public static Set<Field> findSensitiveFields(final Class<?> clazz) {
        final Set<Field> fields = new HashSet<>();
        for (final Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(SensitiveField.class) || hasParentSensitiveAnnotation(field.getAnnotations())) {
                fields.add(field);
            }
        }
        return fields;
    }

    public static boolean hasParentSensitiveAnnotation(final Annotation[] annotations) {
        for (final Annotation annotation : annotations) {
            final SensitiveField fieldAnnotation = AnnotationUtils.findAnnotation(annotation.getClass(), SensitiveField.class);
            if (fieldAnnotation != null) {
                return true;
            }
        }
        return false;
    }

}
