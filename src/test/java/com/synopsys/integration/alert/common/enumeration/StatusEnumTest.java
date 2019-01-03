package com.synopsys.integration.alert.common.enumeration;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class StatusEnumTest {

    @Test
    public void testValueOf() {
        final AuditEntryStatus pending = AuditEntryStatus.valueOf("PENDING");
        final AuditEntryStatus success = AuditEntryStatus.valueOf("SUCCESS");
        final AuditEntryStatus failure = AuditEntryStatus.valueOf("FAILURE");

        assertEquals(AuditEntryStatus.PENDING, pending);
        assertEquals(AuditEntryStatus.SUCCESS, success);
        assertEquals(AuditEntryStatus.FAILURE, failure);
    }
}
