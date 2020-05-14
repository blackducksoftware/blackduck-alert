package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.database.api.mock.MockSystemStatusRepository;
import com.synopsys.integration.alert.database.system.SystemStatus;
import com.synopsys.integration.alert.database.system.SystemStatusRepository;

public class DefaultSystemStatusUtilityTest {

    @Test
    public void isSystemInitializedTest() {
        SystemStatusRepository systemStatusRepository = Mockito.mock(SystemStatusRepository.class);
        SystemStatus systemStatus = new SystemStatus(Boolean.TRUE, new Date());
        Mockito.when(systemStatusRepository.findById(Mockito.any())).thenReturn(Optional.of(systemStatus));

        DefaultSystemStatusUtility systemStatusUtility = new DefaultSystemStatusUtility(systemStatusRepository);
        assertTrue(systemStatusUtility.isSystemInitialized());
    }

    @Test
    public void setSystemInitializedTest() {
        SystemStatusRepository systemStatusRepository = new MockSystemStatusRepository(Boolean.FALSE);
        DefaultSystemStatusUtility systemStatusUtility = new DefaultSystemStatusUtility(systemStatusRepository);
        systemStatusUtility.setSystemInitialized(Boolean.TRUE);

        SystemStatus testSystemStatus = systemStatusRepository.findAll().get(0);
        assertTrue(testSystemStatus.isInitialConfigurationPerformed());
    }

    @Test
    public void startupOccurred() {
        SystemStatusRepository systemStatusRepository = new MockSystemStatusRepository(Boolean.FALSE);
        DefaultSystemStatusUtility systemStatusUtility = new DefaultSystemStatusUtility(systemStatusRepository);
        systemStatusUtility.startupOccurred();

        //createCurrentDateTimestamp can't be modified, so the expected values for getStartupTime must be estimated
        LocalDateTime estimatedDate = LocalDateTime.now();
        SystemStatus testSystemStatus = systemStatusRepository.findAll().get(0);
        LocalDateTime systemStatusLocalDateTime = testSystemStatus.getStartupTime()
                                                      .toInstant()
                                                      .atZone(ZoneId.systemDefault())
                                                      .toLocalDateTime();

        assertFalse(testSystemStatus.isInitialConfigurationPerformed());
        assertNotNull(testSystemStatus.getStartupTime());
        assertEquals(estimatedDate.getHour(), systemStatusLocalDateTime.getHour());
        assertEquals(estimatedDate.getMinute(), systemStatusLocalDateTime.getMinute());
    }

    @Test
    public void getStartupTime() {
        SystemStatusRepository systemStatusRepository = Mockito.mock(SystemStatusRepository.class);
        OffsetDateTime date = OffsetDateTime.now();
        SystemStatus systemStatus = new SystemStatus(Boolean.TRUE, date);
        Mockito.when(systemStatusRepository.findById(Mockito.any())).thenReturn(Optional.of(systemStatus));

        DefaultSystemStatusUtility systemStatusUtility = new DefaultSystemStatusUtility(systemStatusRepository);
        assertEquals(date, systemStatusUtility.getStartupTime());
    }
}
