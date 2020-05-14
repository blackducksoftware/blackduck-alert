/**
 * alert-database
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.database.api;

import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.SystemStatusUtility;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.system.SystemStatus;
import com.synopsys.integration.alert.database.system.SystemStatusRepository;

@Component
public class DefaultSystemStatusUtility implements SystemStatusUtility {
    public static final Long SYSTEM_STATUS_ID = 1L;
    private final SystemStatusRepository systemStatusRepository;

    @Autowired
    public DefaultSystemStatusUtility(SystemStatusRepository systemStatusRepository) {
        this.systemStatusRepository = systemStatusRepository;
    }

    @Override
    @Transactional
    public boolean isSystemInitialized() {
        return getSystemStatus().isInitialConfigurationPerformed();
    }

    @Override
    @Transactional
    public void setSystemInitialized(boolean systemInitialized) {
        SystemStatus systemStatus = getSystemStatus();
        SystemStatus newSystemStatus = new SystemStatus(systemInitialized, systemStatus.getStartupTime());
        updateSystemStatus(newSystemStatus);
    }

    @Override
    @Transactional
    public void startupOccurred() {
        SystemStatus systemStatus = getSystemStatus();
        SystemStatus newSystemStatus = new SystemStatus(systemStatus.isInitialConfigurationPerformed(), createCurrentDateTimestamp());
        updateSystemStatus(newSystemStatus);
    }

    @Override
    @Transactional
    public OffsetDateTime getStartupTime() {
        return getSystemStatus().getStartupTime();
    }

    private OffsetDateTime createCurrentDateTimestamp() {
        return DateUtils.createCurrentDateTimestamp();
    }

    private SystemStatus getSystemStatus() {
        return systemStatusRepository.findById(SYSTEM_STATUS_ID).orElse(new SystemStatus());
    }

    private void updateSystemStatus(SystemStatus systemStatus) {
        systemStatus.setId(SYSTEM_STATUS_ID);
        systemStatusRepository.save(systemStatus);
    }
}
