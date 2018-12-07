package com.synopsys.integration.alert.database.user;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RoleEntityTest {

    @Test
    public void testRoleEntity() {
        final String expectedRoleName = "expected_role_name";
        final Long expectedId = 22L;
        final RoleEntity entity = new RoleEntity(expectedRoleName);
        entity.setId(expectedId);
        assertEquals(expectedRoleName, entity.getRoleName());
        assertEquals(expectedId, entity.getId());
    }
}
