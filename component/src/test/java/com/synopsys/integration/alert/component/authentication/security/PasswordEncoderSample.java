package com.synopsys.integration.alert.component.authentication.security;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderSample {
    private final Logger logger = LoggerFactory.getLogger(PasswordEncoderSample.class);

    @Test
    public void testEncodePassword() {
        PasswordEncoder encoder = new BCryptPasswordEncoder(16);
        String encodedString = encoder.encode("replace_me_with_a_password_to_get_encoded_value");
        logger.debug("Encoded String: {}", encodedString);
    }
}
