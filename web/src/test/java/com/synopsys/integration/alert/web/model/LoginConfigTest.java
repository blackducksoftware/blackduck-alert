package com.synopsys.integration.alert.web.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.component.authentication.web.LoginConfig;

public class LoginConfigTest {

    @Test
    public void testPasswordIsExcludedToString() {
        String username = "username";
        String password = "password";
        LoginConfig loginConfig = new LoginConfig(username, password);

        Assertions.assertTrue(loginConfig.toString().contains(username));
        Assertions.assertFalse(loginConfig.toString().contains(password));
    }
}
