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

public class TextInputConfigField extends ConfigField {
    public TextInputConfigField(String key, String label, String description, boolean required, boolean sensitive, String panel) {
        super(key, label, description, FieldType.TEXT_INPUT, required, sensitive, panel);
    }

    public TextInputConfigField(String key, String label, String description, boolean required, boolean sensitive, String panel, ConfigValidationFunction... validationFunctions) {
        super(key, label, description, FieldType.TEXT_INPUT, required, sensitive, panel);
        this.setValidationFunctions(validationFunctions);
    }

    public TextInputConfigField(String key, String label, String description, boolean required, boolean sensitive) {
        super(key, label, description, FieldType.TEXT_INPUT, required, sensitive);
    }

    public TextInputConfigField(String key, String label, String description, boolean required, boolean sensitive, ConfigValidationFunction... validationFunctions) {
        super(key, label, description, FieldType.TEXT_INPUT, required, sensitive);
        this.setValidationFunctions(validationFunctions);
    }

    public static TextInputConfigField create(String key, String label, String description) {
        return new TextInputConfigField(key, label, description, false, false);
    }

    public static TextInputConfigField create(String key, String label, String description, ConfigValidationFunction... validationFunctions) {
        return new TextInputConfigField(key, label, description, false, false, validationFunctions);
    }

    public static TextInputConfigField createPanel(String key, String label, String description, String panel) {
        return new TextInputConfigField(key, label, description, false, false, panel);
    }

    public static TextInputConfigField createPanel(String key, String label, String description, String panel, ConfigValidationFunction... validationFunctions) {
        return new TextInputConfigField(key, label, description, false, false, panel, validationFunctions);
    }

    public static TextInputConfigField createRequired(String key, String label, String description) {
        return new TextInputConfigField(key, label, description, true, false);
    }

    public static TextInputConfigField createRequired(String key, String label, String description, ConfigValidationFunction... validationFunctions) {
        return new TextInputConfigField(key, label, description, true, false, validationFunctions);
    }

}
