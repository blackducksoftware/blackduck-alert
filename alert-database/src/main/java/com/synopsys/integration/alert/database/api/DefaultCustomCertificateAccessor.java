/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.accessor.CustomCertificateAccessor;
import com.synopsys.integration.alert.common.persistence.model.CustomCertificateModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.certificates.CustomCertificateEntity;
import com.synopsys.integration.alert.database.certificates.CustomCertificateRepository;

@Component
public class DefaultCustomCertificateAccessor implements CustomCertificateAccessor {
    private final CustomCertificateRepository customCertificateRepository;

    @Autowired
    public DefaultCustomCertificateAccessor(CustomCertificateRepository customCertificateRepository) {
        this.customCertificateRepository = customCertificateRepository;
    }

    @Override
    public List<CustomCertificateModel> getCertificates() {
        return customCertificateRepository.findAll()
                   .stream()
                   .map(this::createModel)
                   .collect(Collectors.toList());
    }

    @Override
    public Optional<CustomCertificateModel> getCertificate(Long id) {
        return customCertificateRepository.findById(id)
                   .map(this::createModel);
    }

    @Override
    public CustomCertificateModel storeCertificate(CustomCertificateModel certificateModel) throws AlertConfigurationException {
        String alias = certificateModel.getAlias();
        String certificateContent = certificateModel.getCertificateContent();

        CustomCertificateEntity entityToSave = new CustomCertificateEntity(alias, certificateContent, DateUtils.createCurrentDateTimestamp());
        Long id = certificateModel.getNullableId();
        if (null == id) {
            // Mimic keystore functionality
            id = customCertificateRepository.findByAlias(alias).map(CustomCertificateEntity::getId).orElse(null);
        } else if (!customCertificateRepository.existsById(id)) {
            throw new AlertConfigurationException("A custom certificate with that id did not exist");
        }

        entityToSave.setId(id);
        CustomCertificateEntity updatedEntity = customCertificateRepository.save(entityToSave);
        return createModel(updatedEntity);
    }

    @Override
    public void deleteCertificate(String certificateAlias) {
        customCertificateRepository.findByAlias(certificateAlias)
            .map(CustomCertificateEntity::getId)
            .ifPresent(this::deleteCertificate);
    }

    @Override
    public void deleteCertificate(Long certificateId) {
        customCertificateRepository.deleteById(certificateId);
    }

    private CustomCertificateModel createModel(CustomCertificateEntity entity) {
        CustomCertificateModel customCertificateModel = new CustomCertificateModel(entity.getAlias(), entity.getCertificateContent(), DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        customCertificateModel.setId(entity.getId());
        return customCertificateModel;
    }

}
