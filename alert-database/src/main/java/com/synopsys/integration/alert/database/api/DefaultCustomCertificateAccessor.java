/**
 * alert-database
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
package com.synopsys.integration.alert.database.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.CustomCertificateAccessor;
import com.synopsys.integration.alert.common.persistence.model.CustomCertificateModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.certificates.CustomCertificateEntity;
import com.synopsys.integration.alert.database.certificates.CustomCertificateRepository;

@Component
public class DefaultCustomCertificateAccessor implements CustomCertificateAccessor {
    private CustomCertificateRepository customCertificateRepository;

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
    public CustomCertificateModel storeCertificate(CustomCertificateModel certificateModel) throws AlertDatabaseConstraintException {
        if (null == certificateModel) {
            throw new AlertDatabaseConstraintException("The certificate model cannot be null");
        }

        String alias = certificateModel.getAlias();
        if (StringUtils.isBlank(alias)) {
            throw new AlertDatabaseConstraintException("The field 'alias' cannot be blank");
        }

        String certificateContent = certificateModel.getCertificateContent();
        if (StringUtils.isBlank(certificateContent)) {
            throw new AlertDatabaseConstraintException("The field 'certificateContent' cannot be blank");
        }

        CustomCertificateEntity entityToSave = new CustomCertificateEntity(alias, certificateContent, DateUtils.createCurrentDateTimestamp());
        Long id = certificateModel.getNullableId();
        if (null == id) {
            // Mimic keystore functionality
            id = customCertificateRepository.findByAlias(alias).map(CustomCertificateEntity::getId).orElse(null);
        } else {
            if (!customCertificateRepository.existsById(id)) {
                throw new AlertDatabaseConstraintException("A custom certificate with that id did not exist");
            }
        }

        entityToSave.setId(id);
        CustomCertificateEntity updatedEntity = customCertificateRepository.save(entityToSave);
        return createModel(updatedEntity);
    }

    @Override
    public void deleteCertificate(String certificateAlias) throws AlertDatabaseConstraintException {
        if (StringUtils.isBlank(certificateAlias)) {
            throw new AlertDatabaseConstraintException("The field 'certificateAlias' cannot be blank");
        }
        CustomCertificateEntity customCertificateEntity = customCertificateRepository.findByAlias(certificateAlias)
                                                              .orElseThrow(() -> new AlertDatabaseConstraintException("A custom certificate with the alias " + certificateAlias + " did not exist"));
        deleteCertificate(customCertificateEntity.getId());
    }

    @Override
    public void deleteCertificate(Long certificateId) throws AlertDatabaseConstraintException {
        if (null == certificateId) {
            throw new AlertDatabaseConstraintException("The field 'certificateId' cannot be null");
        } else if (0 > certificateId) {
            throw new AlertDatabaseConstraintException("The field 'certificateId' must be greater than 0");
        }
        customCertificateRepository.deleteById(certificateId);
    }

    private CustomCertificateModel createModel(CustomCertificateEntity entity) {
        CustomCertificateModel customCertificateModel = new CustomCertificateModel(entity.getAlias(), entity.getCertificateContent(), DateUtils.formatDate(entity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        customCertificateModel.setId(entity.getId());
        return customCertificateModel;
    }

}
