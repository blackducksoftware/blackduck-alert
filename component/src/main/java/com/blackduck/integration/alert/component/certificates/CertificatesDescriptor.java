/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.certificates;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.common.descriptor.ComponentDescriptor;
import com.blackduck.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;

@Component
public class CertificatesDescriptor extends ComponentDescriptor {
    public static final String CERTIFICATES_LABEL = "Certificates";
    public static final String CERTIFICATES_URL = "certificates";
    public static final String CERTIFICATES_DESCRIPTION = "This page allows you to configure certificates for Alert to establish secure communication.";
    public static final String CERTIFICATES_COMPONENT_NAMESPACE = "certificates.CertificatesPage";

    // these aren't stored in the database in the field values table.  The UI does have input fields, these strings are used to report field errors in the UI.
    public static final String KEY_ALIAS = "alias";
    public static final String KEY_CERTIFICATE_CONTENT = "certificateContent";

    // following fields are for client certificates
    public static final String KEY_CLIENT_CERTIFICATE_CONTENT = "clientCertificateContent";
    public static final String KEY_PRIVATE_KEY_PASSWORD = "keyPassword";
    public static final String KEY_PRIVATE_KEY_CONTENT = "keyContent";

    @Autowired
    public CertificatesDescriptor(CertificatesDescriptorKey descriptorKey) {
        super(descriptorKey);
    }

    @Override
    public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
        return Optional.empty();
    }

}
