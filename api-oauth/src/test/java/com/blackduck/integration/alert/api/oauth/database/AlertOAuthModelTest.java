package com.blackduck.integration.alert.api.oauth.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class AlertOAuthModelTest {

    @Test
    void checkOptionalFieldsEmpty() {
        UUID id = UUID.randomUUID();
        AlertOAuthModel model = new AlertOAuthModel(id, null, null, null);

        assertEquals(id, model.getId());
        assertTrue(model.getAccessToken().isEmpty());
        assertTrue(model.getRefreshToken().isEmpty());
        assertTrue(model.getExirationTimeMilliseconds().isEmpty());
    }

    @Test
    void checkOptionalFieldPopulated() {
        UUID id = UUID.randomUUID();
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        Long expirationTime = 5000L;
        AlertOAuthModel model = new AlertOAuthModel(id, accessToken, refreshToken, expirationTime);

        assertEquals(id, model.getId());
        assertEquals(accessToken, model.getAccessToken().orElseThrow(() -> new AssertionError("Access Token not found.")));
        assertEquals(refreshToken, model.getRefreshToken().orElseThrow(() -> new AssertionError("Refresh Token not found.")));
        assertEquals(refreshToken, model.getRefreshToken().orElseThrow(() -> new AssertionError("Expiration Time not found.")));
    }
}
