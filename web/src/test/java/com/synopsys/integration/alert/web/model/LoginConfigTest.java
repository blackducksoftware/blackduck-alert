package com.synopsys.integration.alert.web.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.component.authentication.web.LoginConfig;

public class LoginConfigTest {

    @Test
    public void testPasswordIsExcludedToString() {
        String username = "username";
        String password = "password";
        LoginConfig loginConfig = new LoginConfig(username, password);

        assertTrue(loginConfig.toString().contains(username));
        assertFalse(loginConfig.toString().contains(password));
    }
}
