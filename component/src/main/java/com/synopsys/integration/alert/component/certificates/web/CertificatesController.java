/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.certificates.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.BaseResourceController;
import com.synopsys.integration.alert.common.rest.api.ReadAllController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;

@RestController
@RequestMapping(CertificatesController.API_BASE_URL)
public class CertificatesController implements ReadAllController<MultiCertificateModel>, BaseResourceController<CertificateModel>, ValidateController<CertificateModel> {
    public static final String API_BASE_URL = AlertRestConstants.BASE_PATH + "/certificates";
    private final CertificateActions actions;

    @Autowired
    public CertificatesController(CertificateActions actions) {
        this.actions = actions;
    }

    @Override
    public CertificateModel create(CertificateModel resource) {
        return ResponseFactory.createContentResponseFromAction(actions.create(resource));
    }

    @Override
    public CertificateModel getOne(Long id) {
        return ResponseFactory.createContentResponseFromAction(actions.getOne(id));
    }

    @Override
    public void update(Long id, CertificateModel resource) {
        ResponseFactory.createResponseFromAction(actions.update(id, resource));
    }

    @Override
    public void delete(Long id) {
        ResponseFactory.createResponseFromAction(actions.delete(id));
    }

    @Override
    public MultiCertificateModel getAll() {
        return ResponseFactory.createContentResponseFromAction(actions.getAll());
    }

    @Override
    public ValidationResponseModel validate(CertificateModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(actions.validate(requestBody));
    }
}
