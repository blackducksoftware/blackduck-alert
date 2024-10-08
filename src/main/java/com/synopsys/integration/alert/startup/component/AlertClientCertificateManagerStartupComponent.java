package com.synopsys.integration.alert.startup.component;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.certificates.AlertClientCertificateManager;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.persistence.model.ClientCertificateModel;
import com.blackduck.integration.alert.database.job.api.ClientCertificateAccessor;

@Component
@Order(42)
public class AlertClientCertificateManagerStartupComponent extends StartupComponent {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AlertClientCertificateManager alertClientCertificateManager;
    private final ClientCertificateAccessor clientCertificateAccessor;

    @Autowired
    public AlertClientCertificateManagerStartupComponent(
        AlertClientCertificateManager alertClientCertificateManager,
        ClientCertificateAccessor clientCertificateAccessor
    ) {
        this.alertClientCertificateManager = alertClientCertificateManager;
        this.clientCertificateAccessor = clientCertificateAccessor;
    }

    @Override
    protected void initialize() {
        logger.info("Alert client certificate initialization running.");
        Optional<ClientCertificateModel> clientCertificateModel = clientCertificateAccessor.getConfiguration();
        if (clientCertificateModel.isEmpty()) {
            logger.info("No client certificate has been supplied at this time. Skipping client certificate initialization.");
            return;
        }
        try {
            alertClientCertificateManager.importCertificate(clientCertificateModel.get());
            logger.info("Client certificate successfully imported.");
        } catch (AlertException e) {
            logger.error("Failed to import client certificate.", e);
        }
    }
}
