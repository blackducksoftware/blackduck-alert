/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.user;

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
