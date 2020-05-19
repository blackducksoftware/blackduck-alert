package com.synopsys.integration.alert.database.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.util.DateUtils;

public class SystemStatusTest {

    @Test
    public void testConstructor() {
        SystemStatusEntity systemStatus = new SystemStatusEntity();
        assertFalse(systemStatus.isInitialConfigurationPerformed());
        assertNull(systemStatus.getStartupTime());
        assertNull(systemStatus.getId());
    }

    @Test
    public void testGetters() {
        OffsetDateTime date = DateUtils.createCurrentDateTimestamp();
        final boolean initialized = true;
        final Long id = 22L;
        SystemStatusEntity systemStatus = new SystemStatusEntity(initialized, date);
        systemStatus.setId(id);
        assertTrue(systemStatus.isInitialConfigurationPerformed());
        assertEquals(date, systemStatus.getStartupTime());
        assertEquals(id, systemStatus.getId());
    }
}
