package com.synopsys.integration.alert.common.enumeration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
}
