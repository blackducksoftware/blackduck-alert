/**
 * channel
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
package com.synopsys.integration.alert.channel.azure.boards.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.validation.ConfigValidationFunction;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.ValidationResult;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class AzureOAuthAuthenticateValidator implements ConfigValidationFunction {
    private final OAuthRequestValidator oAuthRequestValidator;

    @Autowired
    public AzureOAuthAuthenticateValidator(OAuthRequestValidator oAuthRequestValidator) {
        this.oAuthRequestValidator = oAuthRequestValidator;
    }

    @Override
    public ValidationResult apply(FieldValueModel fieldValueModel, FieldModel fieldModel) {
        if (oAuthRequestValidator.hasRequests()) {
            return ValidationResult.errors("Authentication in Progress cannot perform current action.");
        }
        return ValidationResult.success();
    }
}
