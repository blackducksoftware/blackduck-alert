package com.synopsys.integration.alert.component.certificates.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.certificates.AlertClientCertificateManager;
import com.synopsys.integration.alert.component.certificates.CertificatesDescriptorKey;
import com.synopsys.integration.alert.database.api.ClientCertificateAccessor;

@Component
public class ClientCertificateCrudActions {
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
                return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Error creating config: %s", e.getMessage()));
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
