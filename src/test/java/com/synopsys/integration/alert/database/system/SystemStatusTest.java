package com.synopsys.integration.alert.database.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

public class SystemStatusTest {

    @Test
    public void testConstructor() {
        final SystemStatus systemStatus = new SystemStatus();
        assertFalse(systemStatus.isInitialConfigurationPerformed());
        assertNull(systemStatus.getStartupTime());
        assertNull(systemStatus.getId());
    }

    @Test
    public void testGetters() {
        final Date date = new Date();
        final boolean initialized = true;
        final Long id = 22L;
        final SystemStatus systemStatus = new SystemStatus(initialized, date);
        systemStatus.setId(id);
        assertTrue(systemStatus.isInitialConfigurationPerformed());
        assertEquals(date, systemStatus.getStartupTime());
        assertEquals(id, systemStatus.getId());
    }
}
