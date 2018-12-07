package com.synopsys.integration.alert.database.user;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UserEntityTest {

    @Test
    public void testRoleEntity() {
        final String expectedUserName = "expected_user_name";
        final String expectedPassword = "expected_password";
        final Long expectedId = 25L;
        final UserEntity entity = new UserEntity(expectedUserName, expectedPassword);
        entity.setId(expectedId);
        assertEquals(expectedUserName, entity.getUserName());
        assertEquals(expectedPassword, entity.getPassword());
        assertEquals(expectedId, entity.getId());
    }
}
