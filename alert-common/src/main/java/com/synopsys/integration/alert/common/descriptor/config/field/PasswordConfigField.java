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

public class PasswordConfigField extends ConfigField {
    public PasswordConfigField(final String key, final String label, final String description, final boolean required) {
        super(key, label, description, FieldType.PASSWORD_INPUT.getFieldTypeName(), required, true);
    }

    public PasswordConfigField(final String key, final String label, final String description, final boolean required, final ConfigValidationFunction validationFunction) {
        super(key, label, description, FieldType.PASSWORD_INPUT.getFieldTypeName(), required, true, validationFunction);
    }

    public PasswordConfigField(final String key, final String label, final String description, final boolean required, final String panel) {
        super(key, label, description, FieldType.PASSWORD_INPUT.getFieldTypeName(), required, true, panel);
    }

    public PasswordConfigField(final String key, final String label, final String description, final boolean required, final String panel, final ConfigValidationFunction validationFunction) {
        super(key, label, description, FieldType.PASSWORD_INPUT.getFieldTypeName(), required, true, panel, validationFunction);
    }

    public static PasswordConfigField create(final String key, final String label, final String description) {
        return new PasswordConfigField(key, label, description, false);
    }

    public static PasswordConfigField create(final String key, final String label, final String description, final ConfigValidationFunction validationFunction) {
        return new PasswordConfigField(key, label, description, false, validationFunction);
    }

    public static PasswordConfigField createRequired(final String key, final String label, final String description) {
        return new PasswordConfigField(key, label, description, true);
    }

    public static PasswordConfigField createRequired(final String key, final String label, final String description, final ConfigValidationFunction validationFunction) {
        return new PasswordConfigField(key, label, description, true, validationFunction);
    }

    public static PasswordConfigField create(final String key, final String label, final String description, final String panel) {
        return new PasswordConfigField(key, label, description, false);
    }

    public static PasswordConfigField create(final String key, final String label, final String description, final String panel, final ConfigValidationFunction validationFunction) {
        return new PasswordConfigField(key, label, description, false, validationFunction);
    }

    public static PasswordConfigField createRequired(final String key, final String label, final String description, final String panel) {
        return new PasswordConfigField(key, label, description, true);
    }

    public static PasswordConfigField createRequired(final String key, final String label, final String description, final String panel, final ConfigValidationFunction validationFunction) {
        return new PasswordConfigField(key, label, description, true, validationFunction);
    }

}
