package com.blackduck.integration.alert.component.certificates.web;

import java.util.List;

import com.blackduck.integration.alert.common.rest.model.MultiResponseModel;

public class MultiCertificateModel extends MultiResponseModel<CertificateModel> {
    public MultiCertificateModel(final List<CertificateModel> models) {
        super(models);
    }

    public List<CertificateModel> getCertificates() {
        return getModels();
    }

}
