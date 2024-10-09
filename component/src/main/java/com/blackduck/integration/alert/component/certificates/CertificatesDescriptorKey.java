/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.certificates;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;

@Component
public class CertificatesDescriptorKey extends DescriptorKey {
    private static final String CERTIFICATES_COMPONENT = "component_certificates";

    public CertificatesDescriptorKey() {
        super(CERTIFICATES_COMPONENT, CertificatesDescriptor.CERTIFICATES_LABEL);
    }

}
