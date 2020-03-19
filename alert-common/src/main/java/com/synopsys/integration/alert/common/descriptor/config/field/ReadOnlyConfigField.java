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
package com.synopsys.integration.alert.common.descriptor.config.field;

import com.synopsys.integration.alert.common.enumeration.FieldType;

public class ReadOnlyConfigField extends ConfigField {
    public ReadOnlyConfigField(final String key, final String label, final String description, final boolean required, final boolean sensitive, final String subGroup) {
        super(key, label, description, FieldType.READ_ONLY.getFieldTypeName(), required, sensitive, subGroup);
    }

    public ReadOnlyConfigField(final String key, final String label, final String description, final boolean required, final boolean sensitive, final String subGroup, final ConfigValidationFunction validationFunction) {
        super(key, label, description, FieldType.READ_ONLY.getFieldTypeName(), required, sensitive, subGroup, validationFunction);
    }

    public ReadOnlyConfigField(final String key, final String label, final String description, final boolean required, final boolean sensitive) {
        super(key, label, description, FieldType.READ_ONLY.getFieldTypeName(), required, sensitive);
    }

    public ReadOnlyConfigField(final String key, final String label, final String description, final boolean required, final boolean sensitive, final ConfigValidationFunction validationFunction) {
        super(key, label, description, FieldType.READ_ONLY.getFieldTypeName(), required, sensitive, validationFunction);
    }

    public static ReadOnlyConfigField create(final String key, final String label, final String description) {
        return new ReadOnlyConfigField(key, label, description, false, false);
    }

    public static ReadOnlyConfigField create(final String key, final String label, final String description, final ConfigValidationFunction validationFunction) {
        return new ReadOnlyConfigField(key, label, description, false, false, validationFunction);
    }

    public static ReadOnlyConfigField createRequired(final String key, final String label, final String description) {
        return new ReadOnlyConfigField(key, label, description, true, false);
    }

    public static ReadOnlyConfigField createRequired(final String key, final String label, final String description, final ConfigValidationFunction validationFunction) {
        return new ReadOnlyConfigField(key, label, description, true, false, validationFunction);
    }

    public static ReadOnlyConfigField createGrouped(final String key, final String label, final String description, final String group) {
        return new ReadOnlyConfigField(key, label, description, false, false, group);
    }

    public static ReadOnlyConfigField createGrouped(final String key, final String label, final String description, final String group, final ConfigValidationFunction validationFunction) {
        return new ReadOnlyConfigField(key, label, description, false, false, group, validationFunction);
    }

    public static ReadOnlyConfigField createSensitiveGrouped(final String key, final String label, final String description, final String group) {
        return new ReadOnlyConfigField(key, label, description, false, true, group);
    }

    public static ReadOnlyConfigField createSensitiveGrouped(final String key, final String label, final String description, final String group, final ConfigValidationFunction validationFunction) {
        return new ReadOnlyConfigField(key, label, description, false, true, group, validationFunction);
    }
}
