package com.synopsys.integration.alert.database.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;

public class SystemStatusUtilityTestIT extends AlertIntegrationTest {
    @Autowired
    private SystemStatusRepository systemStatusRepository;

    @Autowired
    private SystemStatusUtility systemStatusUtility;

    @Before
    public void initializeTest() {
        systemStatusRepository.deleteAll();
    }

    @Test
    public void testSetSystemInitialized() {
        systemStatusUtility.setSystemInitialized(false);
        final List<SystemStatus> statusList = systemStatusRepository.findAll();
        SystemStatus systemStatus = systemStatusRepository.getOne(SystemStatusUtility.SYSTEM_STATUS_ID);
        assertFalse(systemStatus.isInitialConfigurationPerformed());
        assertFalse(systemStatusUtility.isSystemInitialized());

        systemStatusUtility.setSystemInitialized(true);
        systemStatus = systemStatusRepository.getOne(SystemStatusUtility.SYSTEM_STATUS_ID);
        assertTrue(systemStatus.isInitialConfigurationPerformed());
        assertTrue(systemStatusUtility.isSystemInitialized());
    }

    @Test
    public void testSaveStartupTime() {
        systemStatusUtility.startupOccurred();
        final SystemStatus systemStatus = systemStatusRepository.getOne(SystemStatusUtility.SYSTEM_STATUS_ID);
        final ZonedDateTime currentTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        final Date date = systemStatus.getStartupTime();
        final ZonedDateTime savedTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
        assertNotNull(date);

        assertEquals(currentTime.getDayOfYear(), savedTime.getDayOfYear());
        assertEquals(currentTime.getHour(), savedTime.getHour());
        assertEquals(currentTime.getMinute(), savedTime.getMinute());
        assertEquals(currentTime.getSecond(), savedTime.getSecond());
    }

    @Test
    public void testGetStartupTime() {
        systemStatusUtility.startupOccurred();
        final SystemStatus systemStatus = systemStatusRepository.getOne(SystemStatusUtility.SYSTEM_STATUS_ID);
        final Date expectedDate = systemStatus.getStartupTime();

        assertEquals(expectedDate, systemStatusUtility.getStartupTime());
    }
}
