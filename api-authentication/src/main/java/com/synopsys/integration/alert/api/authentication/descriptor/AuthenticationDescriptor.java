package com.synopsys.integration.alert.api.authentication.descriptor;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.authentication.validator.AuthenticationConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.descriptor.ComponentDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;

@Component
public class AuthenticationDescriptor extends ComponentDescriptor {
    public static final String AUTHENTICATION_LABEL = "Authentication";
    public static final String AUTHENTICATION_URL = "authentication";
    public static final String AUTHENTICATION_DESCRIPTION = "This page allows you to configure user authentication for Alert.";

    // LDAP Keys
    public static final String KEY_LDAP_ENABLED = "settings.ldap.enabled";
    public static final String KEY_LDAP_SERVER = "settings.ldap.server";
    public static final String KEY_LDAP_MANAGER_DN = "settings.ldap.manager.dn";
    public static final String KEY_LDAP_MANAGER_PWD = "settings.ldap.manager.password";
    public static final String KEY_LDAP_AUTHENTICATION_TYPE = "settings.ldap.authentication.type";
    public static final String KEY_LDAP_REFERRAL = "settings.ldap.referral";
    public static final String KEY_LDAP_USER_SEARCH_BASE = "settings.ldap.user.search.base";
    public static final String KEY_LDAP_USER_SEARCH_FILTER = "settings.ldap.user.search.filter";
    public static final String KEY_LDAP_USER_DN_PATTERNS = "settings.ldap.user.dn.patterns";
    public static final String KEY_LDAP_USER_ATTRIBUTES = "settings.ldap.user.attributes";
    public static final String KEY_LDAP_GROUP_SEARCH_BASE = "settings.ldap.group.search.base";
    public static final String KEY_LDAP_GROUP_SEARCH_FILTER = "settings.ldap.group.search.filter";
    public static final String KEY_LDAP_GROUP_ROLE_ATTRIBUTE = "settings.ldap.group.role.attribute";

    // SAML Keys
    public static final String KEY_SAML_ENABLED = "settings.saml.enabled";
    public static final String KEY_SAML_FORCE_AUTH = "settings.saml.force.auth";
    public static final String KEY_SAML_METADATA_URL = "settings.saml.metadata.url";
    public static final String KEY_SAML_ENTITY_ID = "settings.saml.entity.id";
    public static final String KEY_SAML_ENTITY_BASE_URL = "settings.saml.entity.base.url";
    public static final String KEY_SAML_METADATA_FILE = "settings.saml.metadata.file";  // Field renamed to metadata_file_path for SAMLConfigModel
    public static final String KEY_SAML_WANT_ASSERTIONS_SIGNED = "settings.saml.want.assertions.signed";

    // SAML Role Attribute Mapping
    public static final String KEY_SAML_ROLE_ATTRIBUTE_MAPPING = "settings.saml.role.attribute.mapping.name";

    public static final String FIELD_ERROR_LDAP_SERVER_MISSING = "LDAP Server is missing";

    public static final String FIELD_ERROR_SAML_METADATA_URL_MISSING = "SAML Metadata URL is selected but missing.";
    public static final String FIELD_ERROR_SAML_METADATA_FILE_MISSING = "SAML Metadata file is selected but has not been uploaded.";

    public static final String SAML_METADATA_FILE = "saml_metadata.xml";
    public static final String SAML_ENCRYPTION_CERT_FILE = "saml_encryption.cert";
    public static final String SAML_ENCRYPTION_PRIVATE_KEY_FILE = "saml_encryption_private_key.pem";
    public static final String SAML_SIGNING_CERT_FILE = "saml_signing.cert";
    public static final String SAML_SIGNING_PRIVATE_KEY_FILE = "saml_signing_private_key.pem";
    public static final String SAML_VERIFICATION_CERT_FILE = "saml_verification.cert";

    // Test fields
    public static final String TEST_FIELD_KEY_USERNAME = "test.field.user.name";
    public static final String TEST_FIELD_KEY_PASSWORD = "test.field.user.password";

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
