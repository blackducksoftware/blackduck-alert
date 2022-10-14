package com.synopsys.integration.alert.channel.azure.boards.web;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.channel.azure.boards.action.AzureBoardsGlobalCrudActions;
import com.synopsys.integration.alert.channel.azure.boards.action.AzureBoardsOAuthAuthenticateAction;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.oauth.OAuthEndpointResponse;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.ReadPageController;
import com.synopsys.integration.alert.common.rest.api.StaticConfigResourceController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

@RestController
@RequestMapping(AlertRestConstants.AZURE_BOARDS_CONFIGURATION_PATH)
public class AzureBoardsGlobalConfigController implements StaticConfigResourceController<AzureBoardsGlobalConfigModel>, ValidateController<AzureBoardsGlobalConfigModel>,
    ReadPageController<AlertPagedModel<AzureBoardsGlobalConfigModel>> {
    private final AzureBoardsGlobalCrudActions configActions;
    private final AzureBoardsOAuthAuthenticateAction authenticateAction;

    @Autowired
    public AzureBoardsGlobalConfigController(AzureBoardsGlobalCrudActions configActions, AzureBoardsOAuthAuthenticateAction authenticateAction) {
        this.configActions = configActions;
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
        return ValidationResponseModel.success(); // TODO: AzureBoardsGlobalValidationAction
    }

    @PostMapping("/oauth/authenticate")
    public OAuthEndpointResponse oAuthAuthenticate(AzureBoardsGlobalConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(authenticateAction.authenticate(resource));
    }
}
