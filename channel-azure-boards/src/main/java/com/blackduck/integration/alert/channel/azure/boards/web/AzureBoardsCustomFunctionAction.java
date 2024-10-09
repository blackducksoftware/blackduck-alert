/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.web;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.channel.azure.boards.action.AzureBoardsOAuthAuthenticateAction;
import com.blackduck.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.blackduck.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.blackduck.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationFieldModelValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.action.CustomFunctionAction;
import com.blackduck.integration.alert.common.action.api.ConfigResourceActions;
import com.blackduck.integration.alert.common.descriptor.config.field.endpoint.oauth.OAuthEndpointResponse;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.FieldUtility;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.HttpServletContentWrapper;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;

/**
 * @deprecated This class will be replaced by AzureBoardsOAuthAuthenticateAction. It is planned for removal in 8.0.0.
 */
@Component
@Deprecated(forRemoval = true)
public class AzureBoardsCustomFunctionAction extends CustomFunctionAction<OAuthEndpointResponse> {
    private final Logger logger = LoggerFactory.getLogger(AzureBoardsCustomFunctionAction.class);

    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final ConfigurationFieldModelConverter modelConverter;
    private final AzureBoardsGlobalConfigurationFieldModelValidator globalConfigurationValidator;
    private final ConfigResourceActions configActions;
    private final AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;
    private final AzureBoardsOAuthAuthenticateAction azureBoardsOAuthAuthenticateAction;

    @Autowired
    public AzureBoardsCustomFunctionAction(
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor,
        ConfigurationFieldModelConverter modelConverter,
        AzureBoardsGlobalConfigurationFieldModelValidator globalConfigurationValidator,
        ConfigResourceActions configActions,
        AuthorizationManager authorizationManager,
        AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor,
        AzureBoardsOAuthAuthenticateAction azureBoardsOAuthAuthenticateAction
    ) {
        super(authorizationManager);
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.modelConverter = modelConverter;
        this.globalConfigurationValidator = globalConfigurationValidator;
        this.configActions = configActions;
        this.azureBoardsGlobalConfigAccessor = azureBoardsGlobalConfigAccessor;
        this.azureBoardsOAuthAuthenticateAction = azureBoardsOAuthAuthenticateAction;
    }

    @Override
    public ActionResponse<OAuthEndpointResponse> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper servletContentWrapper) {
        try {
            Optional<FieldModel> savedFieldModel = saveIfValid(fieldModel);
            if (savedFieldModel.isEmpty()) {
                return createErrorResponse("The configuration is invalid. Please test the configuration.");
            }
            FieldUtility fieldUtility = createFieldAccessor(savedFieldModel.get());

            String organizationName = fieldUtility.getStringOrNull(AzureBoardsDescriptor.KEY_ORGANIZATION_NAME);
            String clientId = fieldUtility.getStringOrNull(AzureBoardsDescriptor.KEY_CLIENT_ID);
            String clientSecret = fieldUtility.getStringOrNull(AzureBoardsDescriptor.KEY_CLIENT_SECRET);
            AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModelSaved = azureBoardsGlobalConfigAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
                .orElseThrow(() -> new AlertConfigurationException("Missing Azure Boards global configuration"));
            if (organizationName == null) {
                organizationName = azureBoardsGlobalConfigModelSaved.getOrganizationName();
            }
            if (clientId == null) {
                clientId = azureBoardsGlobalConfigModelSaved.getAppId().orElse("");
            }
            if (clientSecret == null) {
                clientSecret = azureBoardsGlobalConfigModelSaved.getClientSecret().orElse("");
            }
            AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel = new AzureBoardsGlobalConfigModel(
                azureBoardsGlobalConfigModelSaved.getId(),
                AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
                organizationName,
                clientId,
                clientSecret
            );

            return azureBoardsOAuthAuthenticateAction.authenticate(azureBoardsGlobalConfigModel);
        } catch (Exception ex) {
            logger.error("Error activating Azure Boards", ex);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error activating azure oauth.");
        }
    }

    @Override
    protected Collection<AlertFieldStatus> validateRelatedFields(FieldModel fieldModel) {
        return globalConfigurationValidator.validate(fieldModel);
    }

    private ActionResponse<OAuthEndpointResponse> createErrorResponse(String errorMessage) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    private ActionResponse<OAuthEndpointResponse> createErrorResponse(HttpStatus httpStatus, String errorMessage) {
        OAuthEndpointResponse oAuthEndpointResponse = new OAuthEndpointResponse(false, "", errorMessage);
        return new ActionResponse<>(httpStatus, errorMessage, oAuthEndpointResponse);
    }

    private Optional<FieldModel> saveIfValid(FieldModel fieldModel) {
        if (StringUtils.isNotBlank(fieldModel.getId())) {
            Long id = Long.parseLong(fieldModel.getId());
            ActionResponse<FieldModel> response = configActions.update(id, fieldModel);
            return response.getContent();
        } else {
            ActionResponse<FieldModel> response = configActions.create(fieldModel);
            return response.getContent();
        }
    }

    private FieldUtility createFieldAccessor(FieldModel fieldModel) {
        Map<String, ConfigurationFieldModel> fields = new HashMap<>(modelConverter.convertToConfigurationFieldModelMap(fieldModel));
        // check if a configuration exists because the client id is a sensitive field and won't have a value in the field model if updating.
        if (StringUtils.isNotBlank(fieldModel.getId())) {
            configurationModelConfigurationAccessor.getConfigurationById(Long.valueOf(fieldModel.getId()))
                .map(ConfigurationModel::getCopyOfKeyToFieldMap)
                .ifPresent(fields::putAll);
        }
        return new FieldUtility(fields);
    }
}
