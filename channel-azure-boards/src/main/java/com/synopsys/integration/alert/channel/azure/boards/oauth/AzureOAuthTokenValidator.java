/*
 * channel-azure-boards
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsProperties;
import com.synopsys.integration.alert.channel.azure.boards.AzureRedirectUrlCreator;
import com.synopsys.integration.alert.channel.azure.boards.oauth.storage.AzureBoardsCredentialDataStoreFactory;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.ConfigValidationFunction;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.ValidationResult;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class AzureOAuthTokenValidator implements ConfigValidationFunction {
    private final AzureRedirectUrlCreator azureRedirectUrlCreator;
    private final AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory;
    private final ConfigurationFieldModelConverter configurationFieldModelConverter;
    private final ProxyManager proxyManager;

    @Autowired
    public AzureOAuthTokenValidator(AzureRedirectUrlCreator azureRedirectUrlCreator, AzureBoardsCredentialDataStoreFactory azureBoardsCredentialDataStoreFactory,
        ConfigurationFieldModelConverter configurationFieldModelConverter, ProxyManager proxyManager) {
        this.azureRedirectUrlCreator = azureRedirectUrlCreator;
        this.azureBoardsCredentialDataStoreFactory = azureBoardsCredentialDataStoreFactory;
        this.configurationFieldModelConverter = configurationFieldModelConverter;
        this.proxyManager = proxyManager;
    }

    @Override
    public ValidationResult apply(FieldValueModel fieldValueModel, FieldModel fieldModel) {
        ValidationResult result = ValidationResult.success();
        try {
            ProxyInfo proxyInfo = proxyManager.createProxyInfo();
            FieldUtility fieldUtility = configurationFieldModelConverter.convertToFieldAccessor(fieldModel);
            String oAuthRedirectUri = azureRedirectUrlCreator.createOAuthRedirectUri();
            AzureBoardsProperties properties = AzureBoardsProperties.fromFieldAccessor(azureBoardsCredentialDataStoreFactory, oAuthRedirectUri, fieldUtility);
            if (!properties.hasOAuthCredentials(proxyInfo)) {
                result = ValidationResult.warnings("OAuth token credentials missing. Please save then authenticate.");
            }
        } catch (Exception ex) {
            result = ValidationResult.errors(ex.getMessage());
        }
        return result;
    }

}
