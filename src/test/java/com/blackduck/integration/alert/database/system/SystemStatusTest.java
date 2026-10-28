/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.common.util.DateUtils;

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
