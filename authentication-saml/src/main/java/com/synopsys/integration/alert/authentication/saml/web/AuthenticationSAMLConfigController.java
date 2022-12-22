package com.synopsys.integration.alert.authentication.saml.web;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.saml.model.AuthenticationSAMLConfigModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.api.StaticUniqueConfigResourceController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(AlertRestConstants.SAML_PATH)
public class AuthenticationSAMLConfigController implements StaticUniqueConfigResourceController<AuthenticationSAMLConfigModel>, ValidateController<AuthenticationSAMLConfigModel> {
    private static final String UNIQUE_ID = UUID.randomUUID().toString();

    @Autowired
    public AuthenticationSAMLConfigController() {
        // TODO Wire actions
    }

    @Override
    public AuthenticationSAMLConfigModel create(AuthenticationSAMLConfigModel resource) {
        return new AuthenticationSAMLConfigModel(UNIQUE_ID);
    }

    @Override
    public AuthenticationSAMLConfigModel getOne() {
        return new AuthenticationSAMLConfigModel(UNIQUE_ID);
    }

    @Override
    public void update(AuthenticationSAMLConfigModel resource) {

    }

    @Override
    public void delete() {

    }

    @Override
    public ValidationResponseModel validate(AuthenticationSAMLConfigModel requestBody) {
        return new ValidationResponseModel("Success", Map.of());
    }
}
