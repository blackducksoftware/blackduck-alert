/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.ldap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.blackduck.integration.alert.authentication.ldap.database.accessor.LDAPConfigAccessor;
import com.blackduck.integration.alert.authentication.ldap.database.configuration.MockLDAPConfigurationRepository;
import com.blackduck.integration.alert.authentication.ldap.model.LDAPAuthenticationType;
import com.blackduck.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.model.PermissionKey;
import com.blackduck.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.blackduck.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.blackduck.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.test.common.AuthenticationTestUtils;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

public class LDAPTestHelper {
    public static final String DEFAULT_CONFIG_ID = UUID.randomUUID().toString();
    public static final String DEFAULT_DATE_STRING = DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
    public static final boolean DEFAULT_ENABLED = true;
    public static final String DEFAULT_SERVER_NAME = "ldap://alert.blackduck.com:389";
    public static final String DEFAULT_MANAGER_DN = "cn=Alert Manager,ou=Black Duck,ou=people,dc=blackduck,dc=com";
    public static final String DEFAULT_MANAGER_PASSWORD = "managerPassword";
    public static final String DEFAULT_AUTH_TYPE_SIMPLE = LDAPAuthenticationType.SIMPLE.getAuthenticationType();
    public static final String DEFAULT_AUTH_TYPE_DIGEST = LDAPAuthenticationType.DIGEST.getAuthenticationType();
    public static final String DEFAULT_AUTH_TYPE_NONE = LDAPAuthenticationType.NONE.getAuthenticationType();
    public static final String DEFAULT_REFERRAL = "follow";
    public static final String DEFAULT_USER_SEARCH_BASE = "ou=people,dc=blackduck,dc=com";
    public static final String DEFAULT_USER_SEARCH_FILTER = "cn={0}";
    public static final String DEFAULT_USER_DN_PATTERNS = "";
    public static final String DEFAULT_USER_ATTRIBUTES = "";
    public static final String DEFAULT_GROUP_SEARCH_BASE = "ou=groups,dc=blackduck,dc=com";
    public static final String DEFAULT_GROUp_SEARCH_FILTER = "uniquemember={0}";
    public static final String DEFAULT_GROUP_ROLE_ATTRIBUTES = "cn";

    public static LDAPConfigModel createValidLDAPConfigModel() {
        return new LDAPConfigModel(
            DEFAULT_CONFIG_ID,
            DEFAULT_DATE_STRING,
            DEFAULT_DATE_STRING,
            DEFAULT_ENABLED,
            DEFAULT_SERVER_NAME,
            DEFAULT_MANAGER_DN,
            DEFAULT_MANAGER_PASSWORD,
            StringUtils.isNotBlank(DEFAULT_MANAGER_PASSWORD),
            DEFAULT_AUTH_TYPE_SIMPLE,
            DEFAULT_REFERRAL,
            DEFAULT_USER_SEARCH_BASE,
            DEFAULT_USER_SEARCH_FILTER,
            DEFAULT_USER_DN_PATTERNS,
            DEFAULT_USER_ATTRIBUTES,
            DEFAULT_GROUP_SEARCH_BASE,
            DEFAULT_GROUp_SEARCH_FILTER,
            DEFAULT_GROUP_ROLE_ATTRIBUTES
        );
    }

    public static LDAPConfigModel createInvalidLDAPConfigModel() {
        return new LDAPConfigModel(
            "",
            "",
            "",
            false,
            "",
            "",
            "",
            false,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
        );
    }

    public static AuthorizationManager createAuthorizationManager() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        AuthenticationDescriptorKey authenticationDescriptorKey = new AuthenticationDescriptorKey();
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), authenticationDescriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
        return authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet(
            "admin",
            "admin",
            () -> new PermissionMatrixModel(permissions)
        );
    }

    public static EncryptionUtility createEncryptionUtility() {
        AlertProperties alertProperties = new MockAlertProperties();
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, BlackDuckServicesFactory.createDefaultGson());
        return new EncryptionUtility(alertProperties, filePersistenceUtil);
    }

    public static LDAPConfigAccessor createTestLDAPConfigAccessor() {
        MockLDAPConfigurationRepository mockLDAPConfigurationRepository = new MockLDAPConfigurationRepository();
        return new LDAPConfigAccessor(LDAPTestHelper.createEncryptionUtility(), mockLDAPConfigurationRepository);
    }

    public static void assertOptionalField(Supplier<Optional<String>> actualFieldSupplier, Supplier<Optional<String>> expectedFieldSupplier) {
        Optional<String> actualOptional = actualFieldSupplier.get();
        assertTrue(actualOptional.isPresent());
        assertOptionalField(actualOptional.get(), expectedFieldSupplier);
    }

    public static void assertOptionalField(String expectedValue, Supplier<Optional<String>> expectedFieldSupplier) {
        Optional<String> expectedOptional = expectedFieldSupplier.get();
        assertTrue(expectedOptional.isPresent());
        assertEquals(expectedValue, expectedOptional.get());
    }

}
