/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.certificates.web;

import java.util.List;

import com.synopsys.integration.alert.common.rest.model.MultiResponseModel;

public class MultiCertificateModel extends MultiResponseModel<CertificateModel> {
    public MultiCertificateModel(final List<CertificateModel> models) {
        super(models);
    }

    public List<CertificateModel> getCertificates() {
        return getModels();
    }

}
