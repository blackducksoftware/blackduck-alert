package com.synopsys.integration.alert.web.certificates;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.accessor.CustomCertificateAccessor;
import com.synopsys.integration.alert.common.persistence.model.CustomCertificateModel;
import com.synopsys.integration.alert.common.security.CertificateUtility;
import com.synopsys.integration.alert.web.model.CertificateModel;

@Component
public class CertificateActions {
    private CertificateUtility certificateUtility;
    private CustomCertificateAccessor certificateAccessor;

    @Autowired
    public CertificateActions(CustomCertificateAccessor certificateAccessor, CertificateUtility certificateUtility) {
        this.certificateAccessor = certificateAccessor;
        this.certificateUtility = certificateUtility;
    }

    public List<CertificateModel> readCertificates() {
        return List.of();
    }

    public Optional<CertificateModel> readCertificate(Long id) {
        return Optional.empty();
    }

    public CertificateModel importCertificate(CertificateModel certificateModel) {
        return null;
    }

    public Optional<CertificateModel> updateCertificate(Long id) {
        return Optional.empty();
    }

    public Optional<CertificateModel> deleteCertificate(Long id) {
        return Optional.empty();
    }

    private CertificateModel convertDatabaseModel(CustomCertificateModel databaseCertifcateModel) {
        String id = databaseCertifcateModel.getNullableId() != null ? Long.toString(databaseCertifcateModel.getNullableId()) : null;
        return new CertificateModel(id, databaseCertifcateModel.getAlias(), databaseCertifcateModel.getCertificateContent());
    }
}
