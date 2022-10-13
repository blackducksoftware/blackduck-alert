package com.synopsys.integration.alert.channel.azure.boards.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class OAuthRequestValidatorLegacyTest {

    @Test
    void testAddKey() {
        OAuthRequestValidatorLegacy oAuthRequestValidator = new OAuthRequestValidatorLegacy();
        String requestKey = oAuthRequestValidator.generateRequestKey();
        oAuthRequestValidator.addAuthorizationRequest(requestKey);
        assertTrue(oAuthRequestValidator.hasRequestKey(requestKey));
    }

    @Test
    void addNullKeyTest() {
        OAuthRequestValidatorLegacy oAuthRequestValidator = new OAuthRequestValidatorLegacy();
        oAuthRequestValidator.addAuthorizationRequest(null);
        assertFalse(oAuthRequestValidator.hasRequestKey(null));
    }

    @Test
    void testRemoveKey() {
        OAuthRequestValidatorLegacy oAuthRequestValidator = new OAuthRequestValidatorLegacy();
        String requestKey = oAuthRequestValidator.generateRequestKey();
        oAuthRequestValidator.addAuthorizationRequest(requestKey);
        oAuthRequestValidator.removeAuthorizationRequest(requestKey);
        assertFalse(oAuthRequestValidator.hasRequestKey(requestKey));
    }

    @Test
    void removeNullKeyTest() {
        OAuthRequestValidatorLegacy oAuthRequestValidator = new OAuthRequestValidatorLegacy();
        String requestKey = oAuthRequestValidator.generateRequestKey();
        oAuthRequestValidator.addAuthorizationRequest(requestKey);
        oAuthRequestValidator.removeAuthorizationRequest(null);
        assertFalse(oAuthRequestValidator.hasRequestKey(null));
        assertTrue(oAuthRequestValidator.hasRequestKey(requestKey));
        assertTrue(oAuthRequestValidator.hasRequests());
    }

    @Test
    void testRemoveAll() {
        OAuthRequestValidatorLegacy oAuthRequestValidator = new OAuthRequestValidatorLegacy();
        String requestKey = oAuthRequestValidator.generateRequestKey();
        String requestKey2 = oAuthRequestValidator.generateRequestKey();
        oAuthRequestValidator.addAuthorizationRequest(requestKey);
        oAuthRequestValidator.addAuthorizationRequest(requestKey2);

        oAuthRequestValidator.removeAllRequests();
        assertFalse(oAuthRequestValidator.hasRequestKey(requestKey));
        assertFalse(oAuthRequestValidator.hasRequestKey(requestKey2));
    }

    @Test
    void testRemoveKeysByInstant() {
        OAuthRequestValidatorLegacy oAuthRequestValidator = new OAuthRequestValidatorLegacy();
        String requestKey = oAuthRequestValidator.generateRequestKey();
        String requestKey2 = oAuthRequestValidator.generateRequestKey();
        oAuthRequestValidator.addAuthorizationRequest(requestKey);
        oAuthRequestValidator.addAuthorizationRequest(requestKey2);

        Instant instant = Instant.now();
        oAuthRequestValidator.removeRequestsOlderThanInstant(instant);
        assertFalse(oAuthRequestValidator.hasRequestKey(requestKey));
        assertFalse(oAuthRequestValidator.hasRequestKey(requestKey2));
    }

    @Test
    void parseValidKeyTest() {
        OAuthRequestValidatorLegacy oAuthRequestValidator = new OAuthRequestValidatorLegacy();
        String requestKey = oAuthRequestValidator.generateRequestKey();
        String oAuthRequestId = oAuthRequestValidator.parseRequestIdString(requestKey);
        assertNotEquals(OAuthRequestValidatorLegacy.UNKNOWN_OAUTH_ID, oAuthRequestId);
    }

    @Test
    void parseInvalidKeyTest() {
        OAuthRequestValidatorLegacy oAuthRequestValidator = new OAuthRequestValidatorLegacy();
        String oAuthRequestId = oAuthRequestValidator.parseRequestIdString("bad-request-key");
        assertEquals(OAuthRequestValidatorLegacy.UNKNOWN_OAUTH_ID, oAuthRequestId);
    }

}
