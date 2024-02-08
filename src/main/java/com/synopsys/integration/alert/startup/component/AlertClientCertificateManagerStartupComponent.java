package com.synopsys.integration.alert.startup.component;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.certificates.AlertClientCertificateManager;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateKeyModel;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.database.api.DefaultClientCertificateAccessor;
import com.synopsys.integration.alert.database.api.DefaultClientCertificateKeyAccessor;

@Component
@Order(42)
public class AlertClientCertificateManagerStartupComponent extends StartupComponent {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AlertClientCertificateManager alertClientCertificateManager;
    private final DefaultClientCertificateKeyAccessor clientCertificateKeyAccessor;
    private final DefaultClientCertificateAccessor clientCertificateAccessor;

    @Autowired
    public AlertClientCertificateManagerStartupComponent(
        AlertClientCertificateManager alertClientCertificateManager,
        DefaultClientCertificateKeyAccessor clientCertificateKeyAccessor,
        DefaultClientCertificateAccessor clientCertificateAccessor
    ) {
        this.alertClientCertificateManager = alertClientCertificateManager;
        this.clientCertificateKeyAccessor = clientCertificateKeyAccessor;
        this.clientCertificateAccessor = clientCertificateAccessor;
    }

    @Override
    protected void initialize() {
        logger.info("Alert client certificate initialization running.");
        Optional<ClientCertificateKeyModel> clientCertificateKeyModel = clientCertificateKeyAccessor.getConfiguration();
        if (clientCertificateKeyModel.isEmpty()) {
            logger.info("No client certificate key has been supplied at this time. Skipping client certificate initialization.");
            return;
        }
        Optional<ClientCertificateModel> clientCertificateModel = clientCertificateAccessor.getConfiguration();
        if (clientCertificateModel.isEmpty()) {
            logger.info("No client certificate has been supplied at this time. Skipping client certificate initialization.");
            return;
        }
        try {
            alertClientCertificateManager.importCertificate(clientCertificateModel.get(), clientCertificateKeyModel.get());
            logger.info("Client certificate successfully imported.");
        } catch (AlertException e) {
            logger.error("Failed to import client certificate.", e);
        }
    }
}
