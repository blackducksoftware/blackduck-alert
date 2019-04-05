package com.synopsys.integration.alert.database.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.database.api.SystemStatusUtility;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@Transactional
public class SystemStatusUtilityTestIT extends AlertIntegrationTest {
    @Autowired
    private SystemStatusRepository systemStatusRepository;

    @Autowired
    private SystemStatusUtility systemStatusUtility;

    @BeforeEach
    public void init() {
        systemStatusRepository.deleteAllInBatch();
        systemStatusRepository.flush();
    }

    @AfterEach
    public void cleanup() {
        systemStatusRepository.deleteAllInBatch();
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
        final ZonedDateTime currentTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        systemStatusUtility.startupOccurred();
        final SystemStatus systemStatus = systemStatusRepository.getOne(SystemStatusUtility.SYSTEM_STATUS_ID);
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
