/*
 * blackduck-alert
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
import com.synopsys.integration.alert.startup.component.StartupComponent;

@Component
@Order(41)
public class TrustStoreInitializer extends StartupComponent {
    private final Logger logger = LoggerFactory.getLogger(TrustStoreInitializer.class);

    private final CertificateUtility certificateUtility;
    private final CustomCertificateAccessor customCertificateAccessor;

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
