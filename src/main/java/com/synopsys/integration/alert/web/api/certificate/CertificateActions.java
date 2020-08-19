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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.CustomCertificateAccessor;
import com.synopsys.integration.alert.common.persistence.model.CustomCertificateModel;
import com.synopsys.integration.alert.common.security.CertificateUtility;
import com.synopsys.integration.alert.component.certificates.CertificatesDescriptor;
import com.synopsys.integration.util.IntegrationEscapeUtil;

@Component
public class CertificateActions {
    private final Logger logger = LoggerFactory.getLogger(CertificateActions.class);
    private static final String ERROR_DUPLICATE_ALIAS = "A certificate with this alias already exists.";
    private final CertificateUtility certificateUtility;
    private final CustomCertificateAccessor certificateAccessor;
    private final IntegrationEscapeUtil escapeUtil;

    @Autowired
    public CertificateActions(CustomCertificateAccessor certificateAccessor, CertificateUtility certificateUtility) {
        this.certificateAccessor = certificateAccessor;
        this.certificateUtility = certificateUtility;
        escapeUtil = new IntegrationEscapeUtil();
    }

    public List<CertificateModel> readCertificates() {
        return certificateAccessor.getCertificates().stream()
                   .map(this::convertFromDatabaseModel)
                   .collect(Collectors.toList());
    }

    public Optional<CertificateModel> readCertificate(Long id) {
        return certificateAccessor.getCertificate(id)
                   .map(this::convertFromDatabaseModel);
    }

    public CertificateModel createCertificate(CertificateModel certificateModel) throws AlertException {
        if (null != certificateModel.getId()) {
            throw new AlertDatabaseConstraintException("id cannot be present to create a new certificate on the server.");
        }
        validateCertificateModel(certificateModel);
        String loggableAlias = escapeUtil.replaceWithUnderscore(certificateModel.getAlias());
        logger.info("Importing certificate with alias {}", loggableAlias);
        return importCertificate(certificateModel);
    }

    public Optional<CertificateModel> updateCertificate(Long id, CertificateModel certificateModel) throws AlertException {
        validateCertificateModel(certificateModel);
        Optional<CustomCertificateModel> existingCertificate = certificateAccessor.getCertificate(id);
        String logableId = escapeUtil.replaceWithUnderscore(certificateModel.getId());
        String loggableAlias = escapeUtil.replaceWithUnderscore(certificateModel.getAlias());
        logger.info("Updating certificate with id: {} and alias: {}", logableId, loggableAlias);
        if (existingCertificate.isPresent()) {
            return Optional.ofNullable(importCertificate(certificateModel));
        }
        logger.error("Certificate with id: {} missing.", logableId);
        return Optional.empty();
    }

    private CertificateModel importCertificate(CertificateModel certificateModel) throws AlertException {
        CustomCertificateModel certificateToStore = convertToDatabaseModel(certificateModel);
        try {
            CustomCertificateModel storedCertificate = certificateAccessor.storeCertificate(certificateToStore);
            certificateUtility.importCertificate(storedCertificate);
            return convertFromDatabaseModel(storedCertificate);
        } catch (AlertException importException) {
            deleteByAlias(certificateToStore);
            throw importException;
        }
    }

    public void deleteCertificate(Long id) throws AlertException {
        Optional<CustomCertificateModel> certificate = certificateAccessor.getCertificate(id);
        if (certificate.isPresent()) {
            CustomCertificateModel certificateModel = certificate.get();
            logger.info("Delete certificate with id: {} and alias: {}", certificateModel.getNullableId(), certificateModel.getAlias());
            certificateUtility.removeCertificate(certificateModel);
            certificateAccessor.deleteCertificate(id);
        }
    }

    private void deleteByAlias(CustomCertificateModel certificateModel) {
        try {
            certificateAccessor.deleteCertificate(certificateModel.getAlias());
            certificateUtility.removeCertificate(certificateModel.getAlias());
        } catch (AlertException deleteEx) {
            logger.error("Error deleting certificate with alias {}. Error: {}", certificateModel.getAlias(), deleteEx.getMessage());
            logger.debug("Caused by: ", deleteEx);
        }
    }

    private CertificateModel convertFromDatabaseModel(CustomCertificateModel databaseCertifcateModel) {
        String id = databaseCertifcateModel.getNullableId() != null ? Long.toString(databaseCertifcateModel.getNullableId()) : null;
        return new CertificateModel(id, databaseCertifcateModel.getAlias(), databaseCertifcateModel.getCertificateContent(), databaseCertifcateModel.getLastUpdated());
    }

    private CustomCertificateModel convertToDatabaseModel(CertificateModel certificateModel) {
        Long id = StringUtils.isNotBlank(certificateModel.getId()) ? Long.valueOf(certificateModel.getId()) : null;
        return new CustomCertificateModel(id, certificateModel.getAlias(), certificateModel.getCertificateContent(), certificateModel.getLastUpdated());
    }

    private void validateCertificateModel(CertificateModel certificateModel) throws AlertFieldException {
        CustomCertificateModel convertedModel = convertToDatabaseModel(certificateModel);
        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        if (StringUtils.isBlank(certificateModel.getAlias())) {
            fieldErrors.add(AlertFieldStatus.error(CertificatesDescriptor.KEY_ALIAS, "Alias cannot be empty."));
        } else {
            List<CustomCertificateModel> duplicateCertificates = certificateAccessor.getCertificates().stream()
                                                                     .filter(certificate -> certificate.getAlias().equals(certificateModel.getAlias()))
                                                                     .collect(Collectors.toList());
            if (duplicateCertificates.size() > 1) {
                fieldErrors.add(AlertFieldStatus.error(CertificatesDescriptor.KEY_ALIAS, ERROR_DUPLICATE_ALIAS));
            } else if (duplicateCertificates.size() == 1) {
                boolean sameConfig = convertedModel.getNullableId() != null
                                         && duplicateCertificates.get(0).getNullableId().equals(convertedModel.getNullableId());
                if (!sameConfig) {
                    fieldErrors.add(AlertFieldStatus.error(CertificatesDescriptor.KEY_ALIAS, ERROR_DUPLICATE_ALIAS));
                }
            }
        }

        if (StringUtils.isBlank(certificateModel.getCertificateContent())) {
            fieldErrors.add(AlertFieldStatus.error(CertificatesDescriptor.KEY_CERTIFICATE_CONTENT, "Certification content cannot be empty."));
        } else {
            try {
                certificateUtility.validateCertificateContent(convertedModel);
            } catch (AlertException ex) {
                logger.error(ex.getMessage(), ex);
                fieldErrors.add(AlertFieldStatus.error(CertificatesDescriptor.KEY_CERTIFICATE_CONTENT, String.format("Certificate content not valid: %s", ex.getMessage())));
            }
        }

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
    }

}
