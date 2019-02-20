package com.synopsys.integration.alert.database.relation;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.database.user.UserRoleRelation;
import com.synopsys.integration.alert.database.user.UserRoleRelationPK;

public class UserRoleRelationTest {

    @Test
    public void testRelation() {
        final Long expectedUserId = 99L;
        final Long expectedRoleId = 100L;

        final UserRoleRelation relation = new UserRoleRelation(expectedUserId, expectedRoleId);
        assertEquals(expectedUserId, relation.getUserId());
        assertEquals(expectedRoleId, relation.getRoleId());
    }

    @Test
    public void testRelationPK() {
        final Long expectedUserId = 99L;
        final Long expectedRoleId = 100L;

        final UserRoleRelationPK relation = new UserRoleRelationPK();
        relation.setUserId(expectedUserId);
        relation.setRoleId(expectedRoleId);
        assertEquals(expectedUserId, relation.getUserId());
        assertEquals(expectedRoleId, relation.getRoleId());
    }
}
