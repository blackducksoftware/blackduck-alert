package com.synopsys.integration.alert.authentication.ldap.descriptor;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.authentication.ldap.validator.LDAPFieldModelValidator;
import com.synopsys.integration.alert.common.descriptor.ComponentDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;

@Component
public class LDAPDescriptor extends ComponentDescriptor {
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

    public static final String FIELD_ERROR_LDAP_SERVER_MISSING = "LDAP Server is missing";

    // Test fields
    public static final String TEST_FIELD_KEY_USERNAME = "test.field.user.name";
    public static final String TEST_FIELD_KEY_PASSWORD = "test.field.user.password";

    private final LDAPFieldModelValidator ldapFieldModelValidator;

    @Autowired
    public LDAPDescriptor(LDAPDescriptorKey ldapDescriptorKey, LDAPFieldModelValidator ldapFieldModelValidator) {
        super(ldapDescriptorKey);
        this.ldapFieldModelValidator = ldapFieldModelValidator;
    }

    @Override
    public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
        return Optional.of(ldapFieldModelValidator);
    }

}

