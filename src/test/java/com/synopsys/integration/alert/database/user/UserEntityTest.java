package com.synopsys.integration.alert.database.user;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserEntityTest {

    @Test
    void testRoleEntity() {
        final String expectedUserName = "expected_user_name";
        final String expectedPassword = "expected_password";
        final String expectedEmail = "expected_email";
        Long expectedAuthType = 1L;
        final Long expectedId = 25L;
        OffsetDateTime expectedLastLogin = OffsetDateTime.now();
        final long expectedFailedLoginCount = 0L;
        UserEntity entity = new UserEntity(expectedUserName, expectedPassword, expectedEmail, expectedAuthType, expectedLastLogin, null, expectedFailedLoginCount);
        entity.setId(expectedId);
        Assertions.assertEquals(expectedUserName, entity.getUserName());
        Assertions.assertEquals(expectedPassword, entity.getPassword());
        Assertions.assertEquals(expectedEmail, entity.getEmailAddress());
        Assertions.assertEquals(expectedId, entity.getId());
        Assertions.assertEquals(expectedAuthType, entity.getAuthenticationType());
        Assertions.assertEquals(expectedLastLogin, entity.getLastLogin());
        Assertions.assertNull(entity.getLastFailedLogin());
        Assertions.assertEquals(expectedFailedLoginCount, entity.getFailedLoginAttempts());
    }
}
