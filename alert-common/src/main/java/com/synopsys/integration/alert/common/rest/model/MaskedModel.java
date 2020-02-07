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
package com.synopsys.integration.alert.common.rest.model;

import java.lang.annotation.Annotation;
import java.util.Collection;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.alert.common.annotation.SensitiveField;
import com.synopsys.integration.alert.common.annotation.SensitiveFieldFinder;

public abstract class MaskedModel extends AlertSerializableModel {
    @Override
    public String toString() {
        final Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(final FieldAttributes f) {
                final boolean hasSensitiveField = null != f.getAnnotation(SensitiveField.class);
                final Collection<Annotation> annotations = f.getAnnotations();
                return hasSensitiveField || SensitiveFieldFinder.hasParentSensitiveAnnotation(annotations.toArray(new Annotation[annotations.size()]));
            }

            @Override
            public boolean shouldSkipClass(final Class<?> clazz) {
                return false;
            }
        }).create();

        return gson.toJson(this);
    }
}
