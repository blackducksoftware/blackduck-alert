package com.synopsys.integration.alert.authentication.saml.web;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.saml.action.AuthenticationSAMLCrudActions;
import com.synopsys.integration.alert.authentication.saml.action.AuthenticationSAMLValidationAction;
import com.synopsys.integration.alert.authentication.saml.model.AuthenticationSAMLConfigModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.StaticUniqueConfigResourceController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AlertRestConstants.SAML_PATH)
public class AuthenticationSAMLConfigController implements StaticUniqueConfigResourceController<AuthenticationSAMLConfigModel>, ValidateController<AuthenticationSAMLConfigModel> {
    private final AuthenticationSAMLCrudActions configActions;
    private final AuthenticationSAMLValidationAction validationAction;

    @Autowired
    public AuthenticationSAMLConfigController(AuthenticationSAMLCrudActions configActions, AuthenticationSAMLValidationAction validationAction) {
        this.configActions = configActions;
        this.validationAction = validationAction;
    }

    @Override
    public AuthenticationSAMLConfigModel create(AuthenticationSAMLConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public AuthenticationSAMLConfigModel getOne() {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne());
    }

    @Override
    public void update(AuthenticationSAMLConfigModel resource) {
        ResponseFactory.createContentResponseFromAction(configActions.update(resource));
    }

    @Override
    public void delete() {
        ResponseFactory.createContentResponseFromAction(configActions.delete());
    }

    @Override
    public ValidationResponseModel validate(AuthenticationSAMLConfigModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(validationAction.validate(requestBody));
    }
}
