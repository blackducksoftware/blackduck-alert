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

import java.util.List;

import com.synopsys.integration.alert.common.descriptor.config.field.validators.ConfigValidationFunction;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.EncryptionValidator;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class PasswordConfigField extends ConfigField {

    public PasswordConfigField(String key, String label, String description, boolean required, EncryptionValidator encryptionValidation) {
        this(key, label, description, required, null, encryptionValidation);
    }

    public PasswordConfigField(String key, String label, String description, boolean required, EncryptionValidator encryptionValidation, ConfigValidationFunction... validationFunctions) {
        this(key, label, description, required, null, encryptionValidation, validationFunctions);
    }

    public PasswordConfigField(String key, String label, String description, boolean required, String panel, EncryptionValidator encryptionValidation) {
        super(key, label, description, FieldType.PASSWORD_INPUT, required, true, panel);
        createValidators(List.of(encryptionValidation), ConfigField.NO_VALIDATION);
    }

    public PasswordConfigField(String key, String label, String description, boolean required, String panel, EncryptionValidator encryptionValidation, ConfigValidationFunction... validationFunctions) {
        super(key, label, description, FieldType.PASSWORD_INPUT, required, true, panel);
        createValidators(List.of(encryptionValidation), validationFunctions);
    }

    public static PasswordConfigField create(String key, String label, String description, EncryptionValidator encryptionValidation) {
        return new PasswordConfigField(key, label, description, false, encryptionValidation);
    }

    public static PasswordConfigField create(String key, String label, String description, EncryptionValidator encryptionValidation, ConfigValidationFunction... validationFunctions) {
        return new PasswordConfigField(key, label, description, false, encryptionValidation, validationFunctions);
    }

    public static PasswordConfigField createRequired(String key, String label, String description, EncryptionValidator encryptionValidation) {
        return new PasswordConfigField(key, label, description, true, encryptionValidation);
    }

    public static PasswordConfigField createRequired(String key, String label, String description, EncryptionValidator encryptionValidation, ConfigValidationFunction... validationFunctions) {
        return new PasswordConfigField(key, label, description, true, encryptionValidation, validationFunctions);
    }

    public static PasswordConfigField create(String key, String label, String description, String panel, EncryptionValidator encryptionValidation) {
        return new PasswordConfigField(key, label, description, false, panel, encryptionValidation);
    }

    public static PasswordConfigField create(String key, String label, String description, String panel, EncryptionValidator encryptionValidation, ConfigValidationFunction... validationFunctions) {
        return new PasswordConfigField(key, label, description, false, panel, encryptionValidation, validationFunctions);
    }

    public static PasswordConfigField createRequired(String key, String label, String description, String panel, EncryptionValidator encryptionValidation) {
        return new PasswordConfigField(key, label, description, true, panel, encryptionValidation);
    }

    public static PasswordConfigField createRequired(String key, String label, String description, String panel, EncryptionValidator encryptionValidation, ConfigValidationFunction... validationFunctions) {
        return new PasswordConfigField(key, label, description, true, panel, encryptionValidation, validationFunctions);
    }
}
