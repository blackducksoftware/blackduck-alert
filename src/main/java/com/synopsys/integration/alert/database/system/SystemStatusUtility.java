/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.database.system;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SystemStatusUtility {
    private static final Long SYSTEM_STATUS_ID = 1L;
    private final SystemStatusRepository systemStatusRepository;
    private final SystemMessageRepository systemMessageRepository;

    @Autowired
    public SystemStatusUtility(final SystemStatusRepository systemStatusRepository, final SystemMessageRepository systemMessageRepository) {
        this.systemStatusRepository = systemStatusRepository;
        this.systemMessageRepository = systemMessageRepository;
    }

    @Transactional
    public boolean isSystemInitialized() {
        return getSystemStatus().isInitialConfigurationPerformed();
    }

    @Transactional
    public void setSystemInitialized(final boolean systemInitialized) {
        final SystemStatus systemStatus = getSystemStatus();
        final SystemStatus newSystemStatus = new SystemStatus(systemInitialized, systemStatus.getStartupTime());
        newSystemStatus.setId(SYSTEM_STATUS_ID);
        updateSystemStatus(newSystemStatus);
    }

    @Transactional
    public void startupOccurred() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
        final SystemStatus systemStatus = getSystemStatus();
        final SystemStatus newSystemStatus = new SystemStatus(systemStatus.isInitialConfigurationPerformed(), Date.from(zonedDateTime.toInstant()));
        newSystemStatus.setId(SYSTEM_STATUS_ID);
        updateSystemStatus(newSystemStatus);
    }

    @Transactional
    public Date getStartupTime() {
        return getSystemStatus().getStartupTime();
    }

    @Transactional
    public List<SystemMessage> getSystemMessages() {
        return systemMessageRepository.findAll();
    }

    @Transactional
    public void addSystemMessage(final String message, final String severity) {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
        final SystemMessage systemMessage = new SystemMessage(Date.from(zonedDateTime.toInstant()), severity, message);
        systemMessageRepository.save(systemMessage);
    }

    private SystemStatus getSystemStatus() {
        final SystemStatus systemStatus = systemStatusRepository.findById(SYSTEM_STATUS_ID).orElse(new SystemStatus());
        systemStatus.setId(SYSTEM_STATUS_ID);
        return systemStatus;
    }

    private void updateSystemStatus(final SystemStatus systemStatus) {
        systemStatus.setId(SYSTEM_STATUS_ID);
        systemStatusRepository.save(systemStatus);
    }
}
