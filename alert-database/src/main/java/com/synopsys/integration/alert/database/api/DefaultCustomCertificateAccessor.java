/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.database.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
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
