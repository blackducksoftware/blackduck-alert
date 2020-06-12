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
package com.synopsys.integration.alert.common.descriptor.config.field.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.EncryptionUtility;

@Component
public final class EncryptionSettingsValidator extends EncryptionValidator {
    public static final String ENCRYPTION_MISSING = "Encryption configuration missing.";
    private EncryptionUtility encryptionUtility;

    @Autowired
    public EncryptionSettingsValidator(EncryptionUtility encryptionUtility) {
        this.encryptionUtility = encryptionUtility;
    }

    @Override
    public ValidationResult apply(FieldValueModel fieldValueModel, FieldModel fieldModel) {
        if (encryptionUtility.isInitialized()) {
            return ValidationResult.success();
        }
        return ValidationResult.errors(ENCRYPTION_MISSING);
    }

}
