/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest.proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.common.rest.proxy.ProxyManager.NonProxyHostChecker;

public class NonProxyHostCheckerTest {
    private static final String EXPECTED_TRUE = "Expected a true response for non-proxy host check";
    private static final String EXPECTED_FALSE = "Expected a false response for non-proxy host check";

    @Test
    public void isNonProxyHostEmptyTest() {
        NonProxyHostChecker nonProxyHostChecker = new NonProxyHostChecker(Set.of());
        boolean nonProxyHost = nonProxyHostChecker.isNonProxyHost("this should be ignored because the set is empty");
        assertFalse(nonProxyHost, EXPECTED_FALSE);
    }

    @Test
    public void isNonProxyHostSimpleTest() {
        String matchingHost = "matching.host";
        Set<String> nonProxyHosts = Set.of("fake", "decoy", matchingHost, "pattern.*.com");
        NonProxyHostChecker nonProxyHostChecker = new NonProxyHostChecker(nonProxyHosts);

        boolean nonProxyHost = nonProxyHostChecker.isNonProxyHost(matchingHost);
        assertTrue(nonProxyHost, EXPECTED_TRUE);
    }

    @Test
    public void isNonProxyHostPatternTest() {
        String matchingHost = "https://subdomain.blackduck.com";
        Set<String> nonProxyHosts = Set.of("example", "*.blackduck.com");
        NonProxyHostChecker nonProxyHostChecker = new NonProxyHostChecker(nonProxyHosts);

        boolean nonProxyHost = nonProxyHostChecker.isNonProxyHost(matchingHost);
        assertTrue(nonProxyHost, EXPECTED_TRUE);
    }

    @Test
    public void isNonProxyHostComplexUrlTest() {
        String matchingHost = "https://subdomain.blackduck.com/some/path/or/page/file.pdf";
        Set<String> nonProxyHosts = Set.of("example", "*.blackduck.com");
        NonProxyHostChecker nonProxyHostChecker = new NonProxyHostChecker(nonProxyHosts);

        boolean nonProxyHost = nonProxyHostChecker.isNonProxyHost(matchingHost);
        assertTrue(nonProxyHost, EXPECTED_TRUE);
    }

    @Test
    public void isNonProxyHostNoMatchTest() {
        String matchingHost = "not a url";
        Set<String> nonProxyHosts = Set.of("example", "*.blackduck.com");
        NonProxyHostChecker nonProxyHostChecker = new NonProxyHostChecker(nonProxyHosts);

        boolean nonProxyHost = nonProxyHostChecker.isNonProxyHost(matchingHost);
        assertFalse(nonProxyHost, EXPECTED_FALSE);
    }

}
