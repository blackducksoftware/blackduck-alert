package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Optional;
import java.util.UUID;

import com.synopsys.integration.alert.common.persistence.model.ClientCertificateKeyModel;

public interface ClientCertificateKeyAccessor {
    Optional<ClientCertificateKeyModel> getCertificateKey(UUID id);

    ClientCertificateKeyModel saveCertificateKey(ClientCertificateKeyModel certificateKeyModel);

    void deleteCertificateKey(UUID certificateKeyId);
}
