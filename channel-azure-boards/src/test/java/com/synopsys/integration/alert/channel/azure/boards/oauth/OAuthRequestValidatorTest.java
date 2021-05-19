package com.synopsys.integration.alert.channel.azure.boards.oauth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

public class OAuthRequestValidatorTest {

    @Test
    public void testAddKey() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        String requestKey = "request-key-1";
        oAuthRequestValidator.addAuthorizationRequest(requestKey);
        assertTrue(oAuthRequestValidator.hasRequestKey(requestKey));
    }

    @Test
    public void testRemoveKey() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        String requestKey = "request-key-1";
        oAuthRequestValidator.addAuthorizationRequest(requestKey);
        oAuthRequestValidator.removeAuthorizationRequest(requestKey);
        assertFalse(oAuthRequestValidator.hasRequestKey(requestKey));
    }

    @Test
    public void testRemoveAll() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        String requestKey = "request-key-1";
        String requestKey2 = "request-key-2";
        oAuthRequestValidator.addAuthorizationRequest(requestKey);
        oAuthRequestValidator.addAuthorizationRequest(requestKey2);

        oAuthRequestValidator.removeAllRequests();
        assertFalse(oAuthRequestValidator.hasRequestKey(requestKey));
        assertFalse(oAuthRequestValidator.hasRequestKey(requestKey2));
    }

    @Test
    public void testRemoveKeysByInstant() {
        OAuthRequestValidator oAuthRequestValidator = new OAuthRequestValidator();
        String requestKey = "request-key-1";
        String requestKey2 = "request-key-2";
        oAuthRequestValidator.addAuthorizationRequest(requestKey);
        oAuthRequestValidator.addAuthorizationRequest(requestKey2);

        Instant instant = Instant.now();
        oAuthRequestValidator.removeRequestsOlderThanInstant(instant);
        assertFalse(oAuthRequestValidator.hasRequestKey(requestKey));
        assertFalse(oAuthRequestValidator.hasRequestKey(requestKey2));
    }

}
