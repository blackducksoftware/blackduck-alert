/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.certificates.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.action.api.AbstractResourceActions;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.CustomCertificateAccessor;
import com.synopsys.integration.alert.common.persistence.model.CustomCertificateModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.certificates.AlertTrustStoreManager;
import com.synopsys.integration.alert.component.certificates.CertificatesDescriptor;
import com.synopsys.integration.alert.component.certificates.CertificatesDescriptorKey;
import com.synopsys.integration.util.IntegrationEscapeUtil;

@Component
public class CertificateActions extends AbstractResourceActions<CertificateModel, CustomCertificateModel, MultiCertificateModel> {
    private final Logger logger = LoggerFactory.getLogger(CertificateActions.class);
    private static final String ERROR_DUPLICATE_ALIAS = "A certificate with this alias already exists.";
    private final AlertTrustStoreManager trustStoreService;
    private final CustomCertificateAccessor certificateAccessor;
    private final IntegrationEscapeUtil escapeUtil;

    @Autowired
    public CertificateActions(CertificatesDescriptorKey descriptorKey, AuthorizationManager authorizationManager, CustomCertificateAccessor certificateAccessor, AlertTrustStoreManager trustStoreService) {
        super(descriptorKey, ConfigContextEnum.GLOBAL, authorizationManager);
        this.certificateAccessor = certificateAccessor;
        this.trustStoreService = trustStoreService;
        escapeUtil = new IntegrationEscapeUtil();
    }

    @Override
    protected Optional<CertificateModel> findExisting(Long id) {
        return certificateAccessor.getCertificate(id)
                   .map(this::convertDatabaseModelToRestModel);
    }

    @Override
    protected List<CustomCertificateModel> retrieveDatabaseModels() {
        return certificateAccessor.getCertificates();
    }

    @Override
    protected MultiCertificateModel createMultiResponseModel(List<CertificateModel> certificates) {
        return new MultiCertificateModel(certificates);
    }

    @Override
    protected CertificateModel convertDatabaseModelToRestModel(CustomCertificateModel databaseCertificateModel) {
        String id = databaseCertificateModel.getNullableId() != null ? Long.toString(databaseCertificateModel.getNullableId()) : null;
        return new CertificateModel(id, databaseCertificateModel.getAlias(), databaseCertificateModel.getCertificateContent(), databaseCertificateModel.getLastUpdated());
    }

    @Override
    protected ValidationActionResponse testWithoutChecks(CertificateModel resource) {
        return validateWithoutChecks(resource);
    }

    @Override
    protected ValidationActionResponse validateWithoutChecks(CertificateModel resource) {
        ValidationResponseModel responseModel;
        if (StringUtils.isNotBlank(resource.getId()) && !NumberUtils.isCreatable(resource.getId())) {
            responseModel = ValidationResponseModel.generalError("Invalid resource id");
            return new ValidationActionResponse(HttpStatus.BAD_REQUEST, responseModel);
        }
        List<AlertFieldStatus> fieldErrors = validateCertificateFields(resource);
        if (fieldErrors.isEmpty()) {
            responseModel = ValidationResponseModel.success("The certificate configuration is valid");
            return new ValidationActionResponse(HttpStatus.OK, responseModel);
        }
        responseModel = ValidationResponseModel.fromStatusCollection("There were problems with the certificate configuration", fieldErrors);
        return new ValidationActionResponse(HttpStatus.BAD_REQUEST, responseModel);
    }

    @Override
    protected ActionResponse<CertificateModel> createWithoutChecks(CertificateModel resource) {
        String loggableAlias = escapeUtil.replaceWithUnderscore(resource.getAlias());
        logger.info("Importing certificate with alias {}", loggableAlias);
        try {
            CertificateModel certificateModel = importCertificate(resource);
            return new ActionResponse<>(HttpStatus.CREATED, certificateModel);
        } catch (AlertException ex) {
            String message = ex.getMessage();
            logger.error("There was an issue importing the certificate. {}", message);
            logger.debug(message, ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("There was an issue importing the certificate. %s", message));
        }
    }

    @Override
    protected ActionResponse<CertificateModel> updateWithoutChecks(Long id, CertificateModel resource) {
        try {
            Optional<CustomCertificateModel> existingCertificate = certificateAccessor.getCertificate(id);
            String logableId = escapeUtil.replaceWithUnderscore(resource.getId());
            String loggableAlias = escapeUtil.replaceWithUnderscore(resource.getAlias());
            logger.info("Updating certificate with id: {} and alias: {}", logableId, loggableAlias);
            if (existingCertificate.isPresent()) {
                CertificateModel certificateModel = importCertificate(resource);
                return new ActionResponse<>(HttpStatus.NO_CONTENT, certificateModel);
            }
            logger.error("Certificate with id: {} missing.", logableId);
            return new ActionResponse<>(HttpStatus.NOT_FOUND, "Certificate not found.");
        } catch (AlertException ex) {
            logger.error("Error occurred updating certificate", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private CertificateModel importCertificate(CertificateModel certificateModel) throws AlertException {
        CustomCertificateModel certificateToStore = convertToDatabaseModel(certificateModel);
        try {
            CustomCertificateModel storedCertificate = certificateAccessor.storeCertificate(certificateToStore);
            trustStoreService.importCertificate(storedCertificate);
            return convertDatabaseModelToRestModel(storedCertificate);
        } catch (AlertException importException) {
            deleteByAlias(certificateToStore);
            throw importException;
        }
    }

    @Override
    protected ActionResponse<CertificateModel> deleteWithoutChecks(Long id) {
        try {
            Optional<CustomCertificateModel> certificate = certificateAccessor.getCertificate(id);
            if (certificate.isPresent()) {
                CustomCertificateModel certificateModel = certificate.get();
                logger.info("Delete certificate with id: {} and alias: {}", certificateModel.getNullableId(), certificateModel.getAlias());
                trustStoreService.removeCertificate(certificateModel);
                certificateAccessor.deleteCertificate(id);
            }
        } catch (AlertException ex) {
            logger.error("Error deleting certificate", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error deleting certificate: %s", ex.getMessage()));
        }

        return new ActionResponse<>(HttpStatus.NO_CONTENT);
    }

    private void deleteByAlias(CustomCertificateModel certificateModel) {
        try {
            certificateAccessor.deleteCertificate(certificateModel.getAlias());
            trustStoreService.removeCertificate(certificateModel.getAlias());
        } catch (AlertException deleteEx) {
            logger.error("Error deleting certificate with alias {}. Error: {}", certificateModel.getAlias(), deleteEx.getMessage());
            logger.debug("Caused by: ", deleteEx);
        }
    }

    private CustomCertificateModel convertToDatabaseModel(CertificateModel certificateModel) {
        Long id = StringUtils.isNotBlank(certificateModel.getId()) ? Long.valueOf(certificateModel.getId()) : null;
        return new CustomCertificateModel(id, certificateModel.getAlias(), certificateModel.getCertificateContent(), certificateModel.getLastUpdated());
    }

    private List<AlertFieldStatus> validateCertificateFields(CertificateModel certificateModel) {
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
            fieldErrors.add(AlertFieldStatus.error(CertificatesDescriptor.KEY_CERTIFICATE_CONTENT, "Certificate content cannot be empty."));
        } else {
            try {
                trustStoreService.validateCertificateContent(convertedModel);
            } catch (AlertException ex) {
                logger.error(ex.getMessage(), ex);
                fieldErrors.add(AlertFieldStatus.error(CertificatesDescriptor.KEY_CERTIFICATE_CONTENT, String.format("Certificate content not valid: %s", ex.getMessage())));
            }
        }
        return fieldErrors;
    }

}
