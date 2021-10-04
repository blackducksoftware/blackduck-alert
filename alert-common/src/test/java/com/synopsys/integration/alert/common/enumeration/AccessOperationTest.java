package com.synopsys.integration.alert.common.enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.util.BitwiseUtil;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;

public class AccessOperationTest {

    @Test
    public void validPermissionCheckTest() {
        int create = 1;
        int delete = 2;
        int deleteAndCreate = 3;

        assertTrue(AccessOperation.CREATE.isPermitted(create));
        assertTrue(AccessOperation.DELETE.isPermitted(delete));
        assertTrue(AccessOperation.CREATE.isPermitted(deleteAndCreate) && AccessOperation.DELETE.isPermitted(deleteAndCreate));

        assertFalse(AccessOperation.CREATE.isPermitted(delete));
        assertFalse(AccessOperation.EXECUTE.isPermitted(create));
    }

    @Test
    public void verifyBitsTest() {
        assertEquals(1, AccessOperation.CREATE.getBit());
        assertEquals(2, AccessOperation.DELETE.getBit());
        assertEquals(4, AccessOperation.READ.getBit());
        assertEquals(8, AccessOperation.WRITE.getBit());
        assertEquals(16, AccessOperation.EXECUTE.getBit());
        assertEquals(32, AccessOperation.UPLOAD_FILE_READ.getBit());
        assertEquals(64, AccessOperation.UPLOAD_FILE_WRITE.getBit());
        assertEquals(128, AccessOperation.UPLOAD_FILE_DELETE.getBit());

        assertNotEquals(2, AccessOperation.CREATE.getBit());
    }

    @Test
    public void verifyAddingOperationsTest() {
        int noPermissions = 0;
        for (AccessOperation accessOperation : AccessOperation.values()) {
            assertFalse(accessOperation.isPermitted(noPermissions));
        }

        int newPermissions = noPermissions;
        newPermissions = BitwiseUtil.combineBits(newPermissions, AccessOperation.CREATE.getBit());
        newPermissions = BitwiseUtil.combineBits(newPermissions, AccessOperation.EXECUTE.getBit());

        assertTrue(AccessOperation.CREATE.isPermitted(newPermissions));
        assertTrue(AccessOperation.EXECUTE.isPermitted(newPermissions));
        assertFalse(AccessOperation.DELETE.isPermitted(newPermissions));
    }

    @Test
    public void verifyRemovingOperationsTest() {
        int allPermissions = AuthenticationTestUtils.FULL_PERMISSIONS;
        for (AccessOperation accessOperation : AccessOperation.values()) {
            assertTrue(accessOperation.isPermitted(allPermissions));
        }

        int newPermissions = allPermissions;
        newPermissions = BitwiseUtil.removeBits(newPermissions, AccessOperation.CREATE.getBit());
        newPermissions = BitwiseUtil.removeBits(newPermissions, AccessOperation.DELETE.getBit());

        assertFalse(AccessOperation.CREATE.isPermitted(newPermissions));
        assertFalse(AccessOperation.DELETE.isPermitted(newPermissions));
        assertTrue(AccessOperation.EXECUTE.isPermitted(newPermissions));
    }
}
