package com.blackduck.integration.alert.component.certificates.web;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.certificates.AlertClientCertificateManager;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.common.persistence.model.ClientCertificateModel;
import com.blackduck.integration.alert.component.certificates.CertificatesDescriptor;

@Component
public class ClientCertificateConfigurationValidator {
    public static final String CERTIFICATE_VALIDATE_ERROR_MESSAGE = "Error creating config: Error reading certificate or key.";
    private final AlertClientCertificateManager alertClientCertificateManager;

    @Autowired
    ClientCertificateConfigurationValidator(AlertClientCertificateManager alertClientCertificateManager) {
        this.alertClientCertificateManager = alertClientCertificateManager;
    }

    public ValidationResponseModel validate(ClientCertificateModel model) {
        Set<AlertFieldStatus> statuses = new HashSet<>();

        if (StringUtils.isBlank(model.getKeyPassword())) {
            statuses.add(AlertFieldStatus.error(CertificatesDescriptor.KEY_PRIVATE_KEY_PASSWORD, "Private key password cannot be empty."));
        }
        if (StringUtils.isBlank(model.getKeyContent())) {
            statuses.add(AlertFieldStatus.error(CertificatesDescriptor.KEY_PRIVATE_KEY_CONTENT, "Private key content cannot be empty."));
        }
        if (StringUtils.isBlank(model.getClientCertificateContent())) {
            statuses.add(AlertFieldStatus.error(CertificatesDescriptor.KEY_CLIENT_CERTIFICATE_CONTENT, "Certificate content cannot be empty."));
        }

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        if (!alertClientCertificateManager.validateCertificate(model)) {
            return ValidationResponseModel.generalError(CERTIFICATE_VALIDATE_ERROR_MESSAGE);
        }

        return ValidationResponseModel.success();
    }
}
