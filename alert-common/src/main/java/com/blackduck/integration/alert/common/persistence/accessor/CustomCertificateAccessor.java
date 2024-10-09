package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.List;
import java.util.Optional;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.common.persistence.model.CustomCertificateModel;

public interface CustomCertificateAccessor {
    List<CustomCertificateModel> getCertificates();

    Optional<CustomCertificateModel> getCertificate(Long id);

    CustomCertificateModel storeCertificate(CustomCertificateModel certificateModel) throws AlertConfigurationException;

    void deleteCertificate(String certificateAlias);

    void deleteCertificate(Long certificateId);

}
