package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;

public interface ClientCertificateAccessor {
    List<ClientCertificateModel> getCertificates();

    Optional<ClientCertificateModel> getCertificate(UUID id);

    ClientCertificateModel storeCertificate(ClientCertificateModel certificateModel) throws AlertConfigurationException;

    void deleteCertificate(String certificateAlias);

    void deleteCertificate(UUID certificateId);
}
