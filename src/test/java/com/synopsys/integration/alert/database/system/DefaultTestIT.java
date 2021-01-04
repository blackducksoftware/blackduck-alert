package com.synopsys.integration.alert.database.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.database.api.DefaultSystemStatusAccessor;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@Transactional
@AlertIntegrationTest
public class DefaultTestIT {
    @Autowired
    private SystemStatusRepository systemStatusRepository;

    @Autowired
    private DefaultSystemStatusAccessor defaultSystemStatusUtility;

    @BeforeEach
    public void init() {
        systemStatusRepository.deleteAllInBatch();
        systemStatusRepository.flush();
    }

    @AfterEach
    public void cleanup() {
        systemStatusRepository.flush();
        systemStatusRepository.deleteAllInBatch();
    }

    @Test
    public void testSetSystemInitialized() {
        defaultSystemStatusUtility.setSystemInitialized(false);
        List<SystemStatusEntity> statusList = systemStatusRepository.findAll();
        SystemStatusEntity systemStatus = systemStatusRepository.getOne(DefaultSystemStatusAccessor.SYSTEM_STATUS_ID);
        assertFalse(systemStatus.isInitialConfigurationPerformed());
        assertFalse(defaultSystemStatusUtility.isSystemInitialized());

        defaultSystemStatusUtility.setSystemInitialized(true);
        systemStatus = systemStatusRepository.getOne(DefaultSystemStatusAccessor.SYSTEM_STATUS_ID);
        assertTrue(systemStatus.isInitialConfigurationPerformed());
        assertTrue(defaultSystemStatusUtility.isSystemInitialized());
    }

    @Test
    public void testSaveStartupTime() {
        ZonedDateTime currentTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        defaultSystemStatusUtility.startupOccurred();
        SystemStatusEntity systemStatus = systemStatusRepository.getOne(DefaultSystemStatusAccessor.SYSTEM_STATUS_ID);
        OffsetDateTime date = systemStatus.getStartupTime();
        ZonedDateTime savedTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
        assertNotNull(date);

        assertEquals(currentTime.getDayOfYear(), savedTime.getDayOfYear());
        assertEquals(currentTime.getHour(), savedTime.getHour());
        assertEquals(currentTime.getMinute(), savedTime.getMinute());
        assertEquals(currentTime.getSecond(), savedTime.getSecond());
    }

    @Test
    public void testGetStartupTime() {
        defaultSystemStatusUtility.startupOccurred();
        SystemStatusEntity systemStatus = systemStatusRepository.getOne(DefaultSystemStatusAccessor.SYSTEM_STATUS_ID);
        OffsetDateTime expectedDate = systemStatus.getStartupTime();

        assertEquals(expectedDate, defaultSystemStatusUtility.getStartupTime());
    }

}
