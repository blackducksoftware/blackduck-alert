/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.certificates;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@Component
public class CertificatesDescriptorKey extends DescriptorKey {
    private static final String CERTIFICATES_COMPONENT = "component_certificates";

    public CertificatesDescriptorKey() {
        super(CERTIFICATES_COMPONENT, CertificatesDescriptor.CERTIFICATES_LABEL);
    }

}
