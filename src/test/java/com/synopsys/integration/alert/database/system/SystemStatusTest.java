package com.synopsys.integration.alert.database.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

public class SystemStatusTest {

    @Test
    public void testConstructor() {
        SystemStatus systemStatus = new SystemStatus();
        assertFalse(systemStatus.isInitialConfigurationPerformed());
        assertNull(systemStatus.getStartupTime());
        assertNull(systemStatus.getId());
    }

    @Test
    public void testGetters() {
        OffsetDateTime date = OffsetDateTime.now();
        final boolean initialized = true;
        final Long id = 22L;
        SystemStatus systemStatus = new SystemStatus(initialized, date);
        systemStatus.setId(id);
        assertTrue(systemStatus.isInitialConfigurationPerformed());
        assertEquals(date, systemStatus.getStartupTime());
        assertEquals(id, systemStatus.getId());
    }
}
