package com.blackduck.integration.alert.channel.azure.boards.oauth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class OAuthRequestValidatorTest {

    @Test
    void testAddKey() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        UUID requestKey = oAuthRequestValidator.generateRequestKey();
        oAuthRequestValidator.addAuthorizationRequest(requestKey, UUID.randomUUID());
        assertTrue(oAuthRequestValidator.hasRequestKey(requestKey));
    }

    @Test
    void addNullKeyTest() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        oAuthRequestValidator.addAuthorizationRequest(null, UUID.randomUUID());
        assertFalse(oAuthRequestValidator.hasRequestKey(null));
    }

    @Test
    void testRemoveKey() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        UUID requestKey = oAuthRequestValidator.generateRequestKey();
        oAuthRequestValidator.addAuthorizationRequest(requestKey, UUID.randomUUID());
        oAuthRequestValidator.removeAuthorizationRequest(requestKey);
        assertFalse(oAuthRequestValidator.hasRequestKey(requestKey));
    }

    @Test
    void removeNullKeyTest() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        UUID requestKey = oAuthRequestValidator.generateRequestKey();
        oAuthRequestValidator.addAuthorizationRequest(requestKey, UUID.randomUUID());
        oAuthRequestValidator.removeAuthorizationRequest(null);
        assertFalse(oAuthRequestValidator.hasRequestKey(null));
        assertTrue(oAuthRequestValidator.hasRequestKey(requestKey));
        assertTrue(oAuthRequestValidator.hasRequests());
    }

    @Test
    void removeDuplicateRequestsTest() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        UUID configId = UUID.randomUUID();
        UUID request1 = UUID.randomUUID();
        UUID request2 = UUID.randomUUID();
        oAuthRequestValidator.addAuthorizationRequest(request1, configId);
        oAuthRequestValidator.addAuthorizationRequest(request2, configId);

        assertTrue(oAuthRequestValidator.hasRequests());
        assertFalse(oAuthRequestValidator.hasRequestKey(request1));
        assertTrue(oAuthRequestValidator.hasRequestKey(request2));
    }
}
