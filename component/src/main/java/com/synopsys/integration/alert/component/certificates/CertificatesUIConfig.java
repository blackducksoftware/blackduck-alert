/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.certificates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.ui.CustomUIConfig;

@Component
public class CertificatesUIConfig extends CustomUIConfig {
    @Autowired
    public CertificatesUIConfig() {
        super(CertificatesDescriptor.CERTIFICATES_LABEL, CertificatesDescriptor.CERTIFICATES_DESCRIPTION, CertificatesDescriptor.CERTIFICATES_URL, CertificatesDescriptor.CERTIFICATES_COMPONENT_NAMESPACE);
    }

}
