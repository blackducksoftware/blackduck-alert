/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.descriptor.config.field.validators.ConfigValidationFunction;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.EncryptionValidationFunction;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class PasswordConfigField extends ConfigField {
    private final EncryptionValidationFunction encryptionValidation;

    public PasswordConfigField(String key, String label, String description, boolean required, EncryptionValidationFunction encryptionValidation) {
        super(key, label, description, FieldType.PASSWORD_INPUT, required, true);
        this.encryptionValidation = encryptionValidation;
        this.createValidators(ConfigField.NO_VALIDATION);
    }

    public PasswordConfigField(String key, String label, String description, boolean required, EncryptionValidationFunction encryptionValidation, ConfigValidationFunction... validationFunctions) {
        super(key, label, description, FieldType.PASSWORD_INPUT, required, true);
        this.encryptionValidation = encryptionValidation;
        this.createValidators(validationFunctions);
    }

    public static PasswordConfigField create(String key, String label, String description, EncryptionValidationFunction encryptionValidation) {
        return new PasswordConfigField(key, label, description, false, encryptionValidation);
    }

    public static PasswordConfigField create(String key, String label, String description, EncryptionValidationFunction encryptionValidation, ConfigValidationFunction... validationFunctions) {
        return new PasswordConfigField(key, label, description, false, encryptionValidation, validationFunctions);
    }

    public static PasswordConfigField createRequired(String key, String label, String description, EncryptionValidationFunction encryptionValidation) {
        return new PasswordConfigField(key, label, description, true, encryptionValidation);
    }

    public static PasswordConfigField createRequired(String key, String label, String description, EncryptionValidationFunction encryptionValidation, ConfigValidationFunction... validationFunctions) {
        return new PasswordConfigField(key, label, description, true, encryptionValidation, validationFunctions);
    }

    public static PasswordConfigField create(String key, String label, String description, String panel, EncryptionValidationFunction encryptionValidation) {
        return new PasswordConfigField(key, label, description, false, encryptionValidation);
    }

    public static PasswordConfigField create(String key, String label, String description, String panel, EncryptionValidationFunction encryptionValidation, ConfigValidationFunction... validationFunctions) {
        return new PasswordConfigField(key, label, description, false, encryptionValidation, validationFunctions);
    }

    public static PasswordConfigField createRequired(String key, String label, String description, String panel, EncryptionValidationFunction encryptionValidation) {
        return new PasswordConfigField(key, label, description, true, encryptionValidation);
    }

    public static PasswordConfigField createRequired(String key, String label, String description, String panel, EncryptionValidationFunction encryptionValidation, ConfigValidationFunction... validationFunctions) {
        return new PasswordConfigField(key, label, description, true, encryptionValidation, validationFunctions);
    }

    private void createValidators(ConfigValidationFunction[] validationFunctions) {
        List<ConfigValidationFunction> validators = new ArrayList<>();
        validators.add(encryptionValidation);
        if (null != validationFunctions) {
            validators.addAll(Arrays.asList(validationFunctions));
        }
        this.setValidationFunctions(validators.stream().collect(Collectors.toUnmodifiableList()));
    }
}
