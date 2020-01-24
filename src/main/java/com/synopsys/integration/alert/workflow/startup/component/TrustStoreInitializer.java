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
package com.synopsys.integration.alert.workflow.startup.component;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.CustomCertificateAccessor;
import com.synopsys.integration.alert.common.persistence.model.CustomCertificateModel;
import com.synopsys.integration.alert.common.security.CertificateUtility;

@Component
@Order(41)
public class TrustStoreInitializer extends StartupComponent {
    private final Logger logger = LoggerFactory.getLogger(TrustStoreInitializer.class);

    private CertificateUtility certificateUtility;
    private CustomCertificateAccessor customCertificateAccessor;

    public TrustStoreInitializer(CertificateUtility certificateUtility, CustomCertificateAccessor customCertificateAccessor) {
        this.certificateUtility = certificateUtility;
        this.customCertificateAccessor = customCertificateAccessor;
    }

    @Override
    protected void initialize() {
        List<CustomCertificateModel> allCustomCertificates = customCertificateAccessor.getCertificates();
        if (allCustomCertificates.isEmpty()) {
            logger.info("No user-provided certificates have been supplied at this time. Skipping trust store initialization");
            return;
        } else {
            logger.info("Initializing trust store with user-provided certificates");
        }
        for (CustomCertificateModel cert : allCustomCertificates) {
            logger.debug(String.format("Importing '%s' into Alert's trust store", cert.getAlias()));
            try {
                certificateUtility.importCertificate(cert);
            } catch (AlertException e) {
                logger.error(String.format("Failed to import user-provided certificate: '%s'", cert.getAlias()), e);
            }
        }
    }

}
