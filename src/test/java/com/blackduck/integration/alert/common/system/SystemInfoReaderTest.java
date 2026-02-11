/*
 * blackduck-alert
 *
 * Copyright (c) 2026 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.system;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

class SystemInfoReaderTest {

    @Test
    void testGsonNull() {
        SystemInfoReader systemInfoReader = new SystemInfoReader(null);
        Assertions.assertTrue(systemInfoReader.getSystemInfo().isEmpty());
    }

    @Test
    void testGetSystemInfo() {
        SystemInfoReader systemInfoReader = new SystemInfoReader(new Gson());
        Optional<SystemInfo> systemInfo = systemInfoReader.getSystemInfo();

        SystemInfo info = systemInfo.orElseThrow(() -> new AssertionError("SystemInfo is null when it should be populated."));
        Assertions.assertNotNull(info.getCommitHash());
        Assertions.assertNotNull(info.getCreated());
        Assertions.assertNotNull(info.getCopyrightYear());
        Assertions.assertNotNull(info.getDescription());
        Assertions.assertNotNull(info.getProjectUrl());
        Assertions.assertNotNull(info.getVersion());
    }

    @Test
    void testGetSystemInfoCachedValue() {
        SystemInfoReader systemInfoReader = new SystemInfoReader(new Gson());
        Optional<SystemInfo> systemInfo = systemInfoReader.getSystemInfo();
        Optional<SystemInfo> cachedSystemInfo = systemInfoReader.getSystemInfo();

        SystemInfo originalInfo = systemInfo.orElseThrow(() -> new AssertionError("SystemInfo is null when it should be populated."));
        SystemInfo cachedInfo = cachedSystemInfo.orElseThrow(() -> new AssertionError("Cached SystemInfo is null when it should be populated."));
        Assertions.assertEquals(originalInfo, cachedInfo);
        Assertions.assertSame(originalInfo, cachedInfo);
    }


}
