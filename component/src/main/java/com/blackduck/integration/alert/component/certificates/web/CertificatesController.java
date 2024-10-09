/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.certificates.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.common.rest.api.BaseResourceController;
import com.blackduck.integration.alert.common.rest.api.ReadAllController;
import com.blackduck.integration.alert.common.rest.api.ValidateController;

@RestController
@RequestMapping(AlertRestConstants.CERTIFICATE_PATH)
public class CertificatesController implements ReadAllController<MultiCertificateModel>, BaseResourceController<CertificateModel>, ValidateController<CertificateModel> {
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
