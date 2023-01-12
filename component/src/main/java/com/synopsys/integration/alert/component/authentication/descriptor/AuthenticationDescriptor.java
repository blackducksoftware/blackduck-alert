/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.descriptor;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ComponentDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.component.authentication.validator.AuthenticationConfigurationFieldModelValidator;

@Component
public class AuthenticationDescriptor extends ComponentDescriptor {
    public static final String AUTHENTICATION_LABEL = "Authentication";
    public static final String AUTHENTICATION_URL = "authentication";
    public static final String AUTHENTICATION_DESCRIPTION = "This page allows you to configure user authentication for Alert.";

    // SAML Keys
    public static final String KEY_SAML_ENABLED = "settings.saml.enabled";
    public static final String KEY_SAML_FORCE_AUTH = "settings.saml.force.auth";
    public static final String KEY_SAML_METADATA_URL = "settings.saml.metadata.url";
    public static final String KEY_SAML_ENTITY_ID = "settings.saml.entity.id";
    public static final String KEY_SAML_ENTITY_BASE_URL = "settings.saml.entity.base.url";
    public static final String KEY_SAML_METADATA_FILE = "settings.saml.metadata.file";
    public static final String KEY_SAML_WANT_ASSERTIONS_SIGNED = "settings.saml.want.assertions.signed";

    // SAML Role Attribute Mapping
    public static final String KEY_SAML_ROLE_ATTRIBUTE_MAPPING = "settings.saml.role.attribute.mapping.name";

    public static final String FIELD_ERROR_SAML_METADATA_URL_MISSING = "SAML Metadata URL is missing and a Metadata file has not been uploaded.";
    public static final String FIELD_ERROR_SAML_METADATA_FILE_MISSING = "SAML Metadata file has not been uploaded and a Metadata URL has not been specified.";

    public static final String SAML_METADATA_FILE = "saml_metadata.xml";

    private final AuthenticationConfigurationFieldModelValidator authenticationValidator;

    @Autowired
    public AuthenticationDescriptor(AuthenticationDescriptorKey descriptorKey, AuthenticationConfigurationFieldModelValidator authenticationValidator) {
        super(descriptorKey);
        this.authenticationValidator = authenticationValidator;
    }

    @Override
    public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
        return Optional.of(authenticationValidator);
    }

}
