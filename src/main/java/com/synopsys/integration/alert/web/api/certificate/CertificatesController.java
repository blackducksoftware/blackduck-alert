/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.web.api.certificate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.BaseResourceController;
import com.synopsys.integration.alert.common.rest.api.ReadAllController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.web.common.BaseController;

@RestController
@RequestMapping(CertificatesController.API_BASE_URL)
public class CertificatesController implements ReadAllController<CertificateModel>, BaseResourceController<CertificateModel>, ValidateController<CertificateModel> {
    public static final String API_BASE_URL = BaseController.BASE_PATH + "/certificates";
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
    public List<CertificateModel> getAll() {
        return ResponseFactory.createContentResponseFromAction(actions.getAll());
    }

    @Override
    public ValidationResponseModel validate(CertificateModel requestBody) {
        return ResponseFactory.createContentResponseFromAction(actions.validate(requestBody));
    }
}
