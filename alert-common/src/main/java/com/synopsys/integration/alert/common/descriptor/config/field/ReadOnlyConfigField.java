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

import com.synopsys.integration.alert.common.descriptor.config.field.validators.ConfigValidationFunction;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class ReadOnlyConfigField extends ConfigField {
    public ReadOnlyConfigField(String key, String label, String description, boolean required, boolean sensitive, String subGroup) {
        super(key, label, description, FieldType.READ_ONLY, required, sensitive, subGroup);
    }

    public ReadOnlyConfigField(String key, String label, String description, boolean required, boolean sensitive, String subGroup, ConfigValidationFunction... validationFunctions) {
        super(key, label, description, FieldType.READ_ONLY, required, sensitive, subGroup);
        this.setValidationFunctions(validationFunctions);
    }

    public ReadOnlyConfigField(String key, String label, String description, boolean required, boolean sensitive) {
        super(key, label, description, FieldType.READ_ONLY, required, sensitive);
    }

    public ReadOnlyConfigField(String key, String label, String description, boolean required, boolean sensitive, ConfigValidationFunction... validationFunctions) {
        super(key, label, description, FieldType.READ_ONLY, required, sensitive);
        this.setValidationFunctions(validationFunctions);
    }

    public static ReadOnlyConfigField create(String key, String label, String description) {
        return new ReadOnlyConfigField(key, label, description, false, false);
    }

    public static ReadOnlyConfigField create(String key, String label, String description, ConfigValidationFunction... validationFunctions) {
        return new ReadOnlyConfigField(key, label, description, false, false, validationFunctions);
    }

    public static ReadOnlyConfigField createRequired(String key, String label, String description) {
        return new ReadOnlyConfigField(key, label, description, true, false);
    }

    public static ReadOnlyConfigField createRequired(String key, String label, String description, ConfigValidationFunction... validationFunctions) {
        return new ReadOnlyConfigField(key, label, description, true, false, validationFunctions);
    }

    public static ReadOnlyConfigField createGrouped(String key, String label, String description, String group) {
        return new ReadOnlyConfigField(key, label, description, false, false, group);
    }

    public static ReadOnlyConfigField createGrouped(String key, String label, String description, String group, ConfigValidationFunction... validationFunctions) {
        return new ReadOnlyConfigField(key, label, description, false, false, group, validationFunctions);
    }

    public static ReadOnlyConfigField createSensitiveGrouped(String key, String label, String description, String group) {
        return new ReadOnlyConfigField(key, label, description, false, true, group);
    }

    public static ReadOnlyConfigField createSensitiveGrouped(String key, String label, String description, String group, ConfigValidationFunction... validationFunctions) {
        return new ReadOnlyConfigField(key, label, description, false, true, group, validationFunctions);
    }
}
