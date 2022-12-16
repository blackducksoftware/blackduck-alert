package com.synopsys.integration.alert.component.saml.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.api.StaticUniqueConfigResourceController;
import com.synopsys.integration.alert.component.saml.model.SAMLGlobalConfigModel;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(AlertRestConstants.API + "/authentication/saml:") // TODO Create AlertRestConstants once path is confirmed
public class SAMLGlobalConfigController implements StaticUniqueConfigResourceController<SAMLGlobalConfigModel>, ValidateController<SAMLGlobalConfigModel> {
    @Override
    public SAMLGlobalConfigModel create(SAMLGlobalConfigModel resource) {
        return new SAMLGlobalConfigModel(UUID.randomUUID().toString());
    }

    @Override
    public SAMLGlobalConfigModel getOne() {
        return new SAMLGlobalConfigModel(UUID.randomUUID().toString());
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
