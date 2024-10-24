/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.web;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.channel.azure.boards.action.AzureBoardsGlobalCrudActions;
import com.blackduck.integration.alert.channel.azure.boards.action.AzureBoardsGlobalTestAction;
import com.blackduck.integration.alert.channel.azure.boards.action.AzureBoardsGlobalValidationAction;
import com.blackduck.integration.alert.channel.azure.boards.action.AzureBoardsOAuthAuthenticateAction;
import com.blackduck.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.blackduck.integration.alert.common.descriptor.config.field.endpoint.oauth.OAuthEndpointResponse;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.common.rest.api.ReadPageController;
import com.blackduck.integration.alert.common.rest.api.StaticConfigResourceController;
import com.blackduck.integration.alert.common.rest.api.ValidateController;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;

@RestController
@RequestMapping(AlertRestConstants.AZURE_BOARDS_CONFIGURATION_PATH)
public class AzureBoardsGlobalConfigController implements StaticConfigResourceController<AzureBoardsGlobalConfigModel>, ValidateController<AzureBoardsGlobalConfigModel>,
    ReadPageController<AlertPagedModel<AzureBoardsGlobalConfigModel>> {
    private final AzureBoardsGlobalCrudActions configActions;
    private final AzureBoardsGlobalTestAction azureBoardsGlobalTestAction;
    private final AzureBoardsGlobalValidationAction azureBoardsGlobalValidationAction;
    private final AzureBoardsOAuthAuthenticateAction authenticateAction;

    @Autowired
    public AzureBoardsGlobalConfigController(
        AzureBoardsGlobalCrudActions configActions,
        AzureBoardsGlobalTestAction azureBoardsGlobalTestAction,
        AzureBoardsGlobalValidationAction azureBoardsGlobalValidationAction,
        AzureBoardsOAuthAuthenticateAction authenticateAction
    ) {
        this.configActions = configActions;
        this.azureBoardsGlobalTestAction = azureBoardsGlobalTestAction;
        this.azureBoardsGlobalValidationAction = azureBoardsGlobalValidationAction;
        this.authenticateAction = authenticateAction;
    }

    @Override
    public AzureBoardsGlobalConfigModel getOne(UUID id) {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne(id));
    }

    @Override
    public AlertPagedModel<AzureBoardsGlobalConfigModel> getPage(Integer pageNumber, Integer pageSize, String searchTerm, String sortName, String sortOrder) {
        return ResponseFactory.createContentResponseFromAction(configActions.getPaged(pageNumber, pageSize, searchTerm, sortName, sortOrder));
    }

    @Override
    public AzureBoardsGlobalConfigModel create(AzureBoardsGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public void update(UUID id, AzureBoardsGlobalConfigModel resource) {
        ResponseFactory.createContentResponseFromAction(configActions.update(id, resource));
    }

    @Override
    public void delete(UUID id) {
        ResponseFactory.createContentResponseFromAction(configActions.delete(id));
    }

    @Override
    public ValidationResponseModel validate(AzureBoardsGlobalConfigModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(azureBoardsGlobalValidationAction.validate(requestBody));
    }

    @PostMapping("/test")
    public ValidationResponseModel test(@RequestBody AzureBoardsGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(azureBoardsGlobalTestAction.testWithPermissionCheck(resource));
    }

    @PostMapping("/oauth/authenticate")
    public OAuthEndpointResponse oAuthAuthenticate(@RequestBody AzureBoardsGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(authenticateAction.authenticate(resource));
    }
}
