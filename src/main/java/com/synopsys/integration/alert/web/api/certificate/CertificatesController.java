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
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.certificates.CertificatesDescriptorKey;
import com.synopsys.integration.alert.web.common.BaseController;

@RestController
@RequestMapping(CertificatesController.API_BASE_URL)
public class CertificatesController extends BaseController {
    public static final String API_BASE_URL = BaseController.BASE_PATH + "/certificates";
    private static final String CERTIFICATE_IMPORT_ERROR_FORMAT = "There was an issue importing the certificate: {}";
    private final Logger logger = LoggerFactory.getLogger(CertificatesController.class);

    private final DescriptorKey descriptorKey;
    private final AuthorizationManager authorizationManager;
    private final CertificateActions actions;

    @Autowired
    public CertificatesController(CertificatesDescriptorKey descriptorKey, AuthorizationManager authorizationManager, CertificateActions actions) {
        this.descriptorKey = descriptorKey;
        this.authorizationManager = authorizationManager;
        this.actions = actions;
    }

    @GetMapping
    public List<CertificateModel> readCertificates() {
        if (!hasGlobalPermission(authorizationManager::hasReadPermission, descriptorKey)) {
            throw ResponseFactory.createForbiddenException();
        }
        return actions.readCertificates();
    }

    @GetMapping(value = "/{id}")
    public CertificateModel readCertificate(@PathVariable Long id) {
        if (!hasGlobalPermission(authorizationManager::hasReadPermission, descriptorKey)) {
            throw ResponseFactory.createForbiddenException();
        }
        return actions.readCertificate(id)
                   .orElseThrow(() -> ResponseFactory.createNotFoundException("Certificate resource not found"));
    }

    @PostMapping("/validate")
    public MessageResult validateCertificateModel(@RequestBody CertificateModel certificateModel) {
        return actions.validateCertificate(certificateModel);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CertificateModel importCertificate(@RequestBody CertificateModel certificateModel) {
        if (!hasGlobalPermission(authorizationManager::hasCreatePermission, descriptorKey)) {
            throw ResponseFactory.createForbiddenException();
        }
        try {
            return actions.createCertificate(certificateModel);
        } catch (AlertFieldException fieldException) {
            String message = fieldException.getMessage();
            logger.error(CERTIFICATE_IMPORT_ERROR_FORMAT, message);
            logger.debug(message, fieldException);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("There were issues importing the certificate: %s", fieldException.getFieldErrors()));
        } catch (AlertException alertException) {
            String message = alertException.getMessage();
            logger.error(CERTIFICATE_IMPORT_ERROR_FORMAT, message);
            logger.debug(message, alertException);
            throw ResponseFactory.createInternalServerErrorException(String.format("There was an issue importing the certificate. %s", message));
        }
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCertificate(@PathVariable Long id, @RequestBody CertificateModel certificateModel) {
        if (!hasGlobalPermission(authorizationManager::hasWritePermission, descriptorKey)) {
            throw ResponseFactory.createForbiddenException();
        }
        try {
            Optional<CertificateModel> certificate = actions.updateCertificate(id, certificateModel);
            if (certificate.isEmpty()) {
                throw ResponseFactory.createNotFoundException("Certificate resource not found");
            }
        } catch (AlertFieldException fieldException) {
            String message = fieldException.getMessage();
            logger.error(CERTIFICATE_IMPORT_ERROR_FORMAT, message);
            logger.debug(message, fieldException);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("There were issues updating the certificate: %s", fieldException.getFieldErrors()));
        } catch (AlertException alertException) {
            String message = alertException.getMessage();
            logger.error("There was an issue updating the certificate: {}", message);
            logger.debug(message, alertException);
            throw ResponseFactory.createInternalServerErrorException(String.format("There was an issue updating the certificate. %s", message));
        }
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCertificate(@PathVariable Long id) {
        if (!hasGlobalPermission(authorizationManager::hasDeletePermission, descriptorKey)) {
            throw ResponseFactory.createForbiddenException();
        }
        try {
            boolean existed = actions.deleteCertificate(id);
            if (!existed) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        } catch (AlertException ex) {
            String message = ex.getMessage();
            logger.error("There was an issue deleting the certificate: {}", message);
            logger.debug(message, ex);
            throw ResponseFactory.createInternalServerErrorException(String.format("There was an issue deleting the certificate. %s", message));
        }
    }

}
