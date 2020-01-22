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
package com.synopsys.integration.alert.web.certificates;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.accessor.CustomCertificateAccessor;
import com.synopsys.integration.alert.common.persistence.model.CustomCertificateModel;
import com.synopsys.integration.alert.common.security.CertificateUtility;
import com.synopsys.integration.alert.web.model.CertificateModel;

@Component
public class CertificateActions {
    private CertificateUtility certificateUtility;
    private CustomCertificateAccessor certificateAccessor;

    @Autowired
    public CertificateActions(CustomCertificateAccessor certificateAccessor, CertificateUtility certificateUtility) {
        this.certificateAccessor = certificateAccessor;
        this.certificateUtility = certificateUtility;
    }

    public List<CertificateModel> readCertificates() {
        return List.of();
    }

    public Optional<CertificateModel> readCertificate(Long id) {
        return Optional.empty();
    }

    public CertificateModel importCertificate(CertificateModel certificateModel) {
        return null;
    }

    public Optional<CertificateModel> updateCertificate(Long id) {
        return Optional.empty();
    }

    public Optional<CertificateModel> deleteCertificate(Long id) {
        return Optional.empty();
    }

    private CertificateModel convertDatabaseModel(CustomCertificateModel databaseCertifcateModel) {
        String id = databaseCertifcateModel.getNullableId() != null ? Long.toString(databaseCertifcateModel.getNullableId()) : null;
        return new CertificateModel(id, databaseCertifcateModel.getAlias(), databaseCertifcateModel.getCertificateContent());
    }
}
