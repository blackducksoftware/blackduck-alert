package com.synopsys.integration.alert.database.user;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class UserEntityTest {

    @Test
    public void testRoleEntity() {
        final String expectedUserName = "expected_user_name";
        final String expectedPassword = "expected_password";
        final String expectedEmail = "expected_email";
        final Long expectedId = 25L;
        final UserEntity entity = new UserEntity(expectedUserName, expectedPassword, expectedEmail);
        entity.setId(expectedId);
        assertEquals(expectedUserName, entity.getUserName());
        assertEquals(expectedPassword, entity.getPassword());
        assertEquals(expectedEmail, entity.getEmailAddress());
        assertEquals(expectedId, entity.getId());
    }
}
