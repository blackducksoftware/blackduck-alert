package com.blackduck.integration.alert.component.certificates.web;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.certificates.AlertClientCertificateManager;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.logging.AlertLoggerFactory;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.component.certificates.CertificatesDescriptorKey;
import com.synopsys.integration.alert.database.job.api.ClientCertificateAccessor;

@Component
public class ClientCertificateCrudActions {
    private final Logger logger = AlertLoggerFactory.getLogger(getClass());
    private final AlertClientCertificateManager alertClientCertificateManager;
    private final ClientCertificateAccessor configurationAccessor;
    private final ConfigurationCrudHelper configurationCrudHelper;
    private final ClientCertificateConfigurationValidator configurationValidator;

    @Autowired
    public ClientCertificateCrudActions(
        AlertClientCertificateManager alertClientCertificateManager,
        AuthorizationManager authorizationManager,
        CertificatesDescriptorKey certificatesDescriptorKey,
        ClientCertificateAccessor configurationAccessor,
        ClientCertificateConfigurationValidator configurationValidator
    ) {
        this.alertClientCertificateManager = alertClientCertificateManager;
        this.configurationAccessor = configurationAccessor;
        this.configurationCrudHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, certificatesDescriptorKey);
        this.configurationValidator = configurationValidator;
    }

    public ActionResponse<ClientCertificateModel> getOne() {
        return configurationCrudHelper.getOne(configurationAccessor::getConfiguration);
    }

    public ActionResponse<ClientCertificateModel> create(ClientCertificateModel resource) {
        ActionResponse<ClientCertificateModel> response = configurationCrudHelper.create(
            () -> configurationValidator.validate(resource),
            configurationAccessor::doesConfigurationExist,
            () -> configurationAccessor.createConfiguration(resource)
        );

        if (response.isSuccessful()) {
            try {
                alertClientCertificateManager.importCertificate(resource);
            } catch (Exception e) {
                logger.error(e.getMessage());
                // If an error occurs while importing the certificate then we should remove the persisted configuration as it is invalid.
                configurationAccessor.deleteConfiguration();
                return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ClientCertificateConfigurationValidator.CERTIFICATE_VALIDATE_ERROR_MESSAGE);
            }
        }

        return response;
    }

    public ActionResponse<ClientCertificateModel> delete() {
        ActionResponse<ClientCertificateModel> response = configurationCrudHelper.delete(
            configurationAccessor::doesConfigurationExist,
            configurationAccessor::deleteConfiguration
        );

        if (response.isSuccessful()) {
            try {
                alertClientCertificateManager.removeCertificate();
            } catch (Exception e) {
                return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error deleting config: %s", e.getMessage()));
            }
        }

        return response;
    }
}
