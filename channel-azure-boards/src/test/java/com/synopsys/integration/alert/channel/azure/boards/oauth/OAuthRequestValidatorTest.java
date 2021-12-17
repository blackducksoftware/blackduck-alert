package com.synopsys.integration.alert.channel.azure.boards.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class OAuthRequestValidatorTest {

    @Test
    void testAddKey() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        String requestKey = oAuthRequestValidator.generateRequestKey();
        oAuthRequestValidator.addAuthorizationRequest(requestKey);
        assertTrue(oAuthRequestValidator.hasRequestKey(requestKey));
    }

    @Test
    void addNullKeyTest() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        oAuthRequestValidator.addAuthorizationRequest(null);
        assertFalse(oAuthRequestValidator.hasRequestKey(null));
    }

    @Test
    void testRemoveKey() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        String requestKey = oAuthRequestValidator.generateRequestKey();
        oAuthRequestValidator.addAuthorizationRequest(requestKey);
        oAuthRequestValidator.removeAuthorizationRequest(requestKey);
        assertFalse(oAuthRequestValidator.hasRequestKey(requestKey));
    }

    @Test
    void removeNullKeyTest() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        String requestKey = oAuthRequestValidator.generateRequestKey();
        oAuthRequestValidator.addAuthorizationRequest(requestKey);
        oAuthRequestValidator.removeAuthorizationRequest(null);
        assertFalse(oAuthRequestValidator.hasRequestKey(null));
        assertTrue(oAuthRequestValidator.hasRequestKey(requestKey));
        assertTrue(oAuthRequestValidator.hasRequests());
    }

    @Test
    void testRemoveAll() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
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
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
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
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        String requestKey = oAuthRequestValidator.generateRequestKey();
        String oAuthRequestId = oAuthRequestValidator.parseRequestIdString(requestKey);
        assertNotEquals(OAuthRequestValidator.UNKNOWN_OAUTH_ID, oAuthRequestId);
    }

    @Test
    void parseInvalidKeyTest() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        String oAuthRequestId = oAuthRequestValidator.parseRequestIdString("bad-request-key");
        assertEquals(OAuthRequestValidator.UNKNOWN_OAUTH_ID, oAuthRequestId);
    }

}
