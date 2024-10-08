package com.blackduck.integration.alert.component.certificates.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.StaticUniqueConfigResourceController;

@RestController
@RequestMapping(AlertRestConstants.CLIENT_CERTIFICATE_PATH)
public class ClientCertificateController implements StaticUniqueConfigResourceController<ClientCertificateModel> {
    ClientCertificateCrudActions configActions;

    public ClientCertificateController(ClientCertificateCrudActions configActions) {
        this.configActions = configActions;
    }

    @Override
    public ClientCertificateModel create(ClientCertificateModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public ClientCertificateModel getOne() {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne());
    }

    @Override
    public void update(ClientCertificateModel resource) {
        throw ResponseFactory.createMethodNotAllowedException("Updates of client certificate are not supported. Please delete and create a new configuration.");
    }

    @Override
    public void delete() {
       ResponseFactory.createContentResponseFromAction(configActions.delete());
    }
}
