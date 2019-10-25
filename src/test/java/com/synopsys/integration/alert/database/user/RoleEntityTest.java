package com.synopsys.integration.alert.database.user;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class RoleEntityTest {

    @Test
    public void testRoleEntity() {
        String expectedRoleName = "expected_role_name";
        Long expectedId = 22L;
        RoleEntity entity = new RoleEntity(expectedRoleName, true);
        entity.setId(expectedId);
        assertEquals(expectedRoleName, entity.getRoleName());
        assertEquals(expectedId, entity.getId());
    }
}
