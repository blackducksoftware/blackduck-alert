/**
 * blackduck-alert
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

import java.net.Proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.AzureRedirectUtil;
import com.synopsys.integration.alert.channel.azure.boards.oauth.storage.AzureBoardsCredentialDataStoreFactory;
import com.synopsys.integration.alert.channel.azure.boards.service.AzureBoardsProperties;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.ConfigValidationFunction;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.ValidationResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class AzureOAuthTokenValidator implements ConfigValidationFunction {
    private final AzureRedirectUtil azureRedirectUtil;
    private final AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory;
    private final ConfigurationFieldModelConverter configurationFieldModelConverter;
    private final ProxyManager proxyManager;

    @Autowired
    public AzureOAuthTokenValidator(AzureRedirectUtil azureRedirectUtil, AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory,
        ConfigurationFieldModelConverter configurationFieldModelConverter, ProxyManager proxyManager) {
        this.azureRedirectUtil = azureRedirectUtil;
        this.azureBoardsCredentialDataStoreFactory = azureBoardsCredentialDataStoreFactory;
        this.configurationFieldModelConverter = configurationFieldModelConverter;
        this.proxyManager = proxyManager;
    }

    public ValidationResult validate(FieldValueModel fieldValueModel, FieldModel fieldModel) {
        return this.apply(fieldValueModel, fieldModel);
    }

    @Override
    public ValidationResult apply(FieldValueModel fieldValueModel, FieldModel fieldModel) {
        ValidationResult result = ValidationResult.success();
        try {
            Proxy proxy = proxyManager.createProxy();
            FieldAccessor fieldAccessor = configurationFieldModelConverter.convertToFieldAccessor(fieldModel);
            String oAuthRedirectUri = azureRedirectUtil.createOAuthRedirectUri();
            AzureBoardsProperties properties = AzureBoardsProperties.fromFieldAccessor(azureBoardsCredentialDataStoreFactory, oAuthRedirectUri, fieldAccessor);
            if (!properties.hasOAuthCredentials(proxy)) {
                result = ValidationResult.warnings("OAuth token credentials missing. Please save then authenticate.");
            }
        } catch (Exception ex) {
            result = ValidationResult.errors(ex.getMessage());
        }
        return result;
    }
}
