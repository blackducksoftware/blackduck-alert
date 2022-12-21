package com.synopsys.integration.alert.authentication.saml.web;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.saml.model.SAMLGlobalConfigModel;
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
public class SAMLGlobalConfigController implements StaticUniqueConfigResourceController<SAMLGlobalConfigModel>, ValidateController<SAMLGlobalConfigModel> {
    private final String UNIQUE_ID = UUID.randomUUID().toString();

    @Autowired
    public SAMLGlobalConfigController() {
        // TODO Wire actions
    }

    @Override
    public SAMLGlobalConfigModel create(SAMLGlobalConfigModel resource) {
        return new SAMLGlobalConfigModel(UNIQUE_ID);
    }

    @Override
    public SAMLGlobalConfigModel getOne() {
        return new SAMLGlobalConfigModel(UNIQUE_ID);
    }

    @Override
    public void update(SAMLGlobalConfigModel resource) {

    }

    @Override
    public void delete() {

    }

    @Override
    public ValidationResponseModel validate(SAMLGlobalConfigModel requestBody) {
        return new ValidationResponseModel("Success", Map.of());
    }
}
