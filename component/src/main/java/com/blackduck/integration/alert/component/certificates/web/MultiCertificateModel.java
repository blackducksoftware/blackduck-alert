/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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
