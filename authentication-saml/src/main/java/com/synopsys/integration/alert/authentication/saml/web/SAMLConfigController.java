package com.synopsys.integration.alert.authentication.saml.web;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.saml.action.SAMLCrudActions;
import com.synopsys.integration.alert.authentication.saml.action.SAMLValidationAction;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.StaticUniqueConfigResourceController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AlertRestConstants.SAML_PATH)
public class SAMLConfigController implements StaticUniqueConfigResourceController<SAMLConfigModel>, ValidateController<SAMLConfigModel> {
    private final SAMLCrudActions configActions;
    private final SAMLValidationAction validationAction;

    @Autowired
    public SAMLConfigController(SAMLCrudActions configActions, SAMLValidationAction validationAction) {
        this.configActions = configActions;
        this.validationAction = validationAction;
    }

    @Override
    public SAMLConfigModel create(SAMLConfigModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public SAMLConfigModel getOne() {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne());
    }

    @Override
    public void update(SAMLConfigModel resource) {
        ResponseFactory.createContentResponseFromAction(configActions.update(resource));
    }

    @Override
    public void delete() {
        ResponseFactory.createContentResponseFromAction(configActions.delete());
    }

    @Override
    public ValidationResponseModel validate(SAMLConfigModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(validationAction.validate(requestBody));
    }
}
