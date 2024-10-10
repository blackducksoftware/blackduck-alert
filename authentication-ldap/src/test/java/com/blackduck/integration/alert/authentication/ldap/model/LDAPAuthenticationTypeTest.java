/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.ldap.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class LDAPAuthenticationTypeTest {
    public static final ArrayList<String> TEST_STRINGS_DEFAULT = new ArrayList<>(
        Arrays.asList(null, "", "simpl", "simplee"));
    public static final ArrayList<String> TEST_STRINGS_SIMPLE = new ArrayList<>(
        Arrays.asList("simple", "SIMPLE", "SIMple", "simPLE", "sImPlE"));
    public static final ArrayList<String> TEST_STRINGS_DIGEST = new ArrayList<>(
        Arrays.asList("digest", "DIGEST", "DIGest", "digEST", "dIgEsT"));
    public static final ArrayList<String> TEST_STRINGS_NONE = new ArrayList<>(
        Arrays.asList("none", "NONE", "NOne", "noNE", "nOnE"));

    @Test
    void testDefault() {
        for (String testValue : TEST_STRINGS_DEFAULT) {
            assertEquals("simple", LDAPAuthenticationType.fromString(testValue));
        }
    }

    @Test
    void testSimple() {
        for (String testValue : TEST_STRINGS_SIMPLE) {
            assertEquals("simple", LDAPAuthenticationType.fromString(testValue));
        }

        assertEquals("simple", LDAPAuthenticationType.SIMPLE.getAuthenticationType());
    }

    @Test
    void testDigest() {
        for (String testValue : TEST_STRINGS_DIGEST) {
            assertEquals("digest", LDAPAuthenticationType.fromString(testValue));
        }

        assertEquals("digest", LDAPAuthenticationType.DIGEST.getAuthenticationType());
    }

    @Test
    void testNone() {
        for (String testValue : TEST_STRINGS_NONE) {
            assertEquals("none", LDAPAuthenticationType.fromString(testValue));
        }

        assertEquals("none", LDAPAuthenticationType.NONE.getAuthenticationType());
    }
}
