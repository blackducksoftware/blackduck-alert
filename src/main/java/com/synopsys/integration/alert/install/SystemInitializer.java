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
package com.synopsys.integration.alert.install;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;

@Component
public class SystemInitializer {
    private final SystemStatusUtility systemStatusUtility;
    private final AlertProperties alertProperties;
    private final GlobalBlackDuckRepository globalBlackDuckRepository;
    private final EncryptionUtility encryptionUtility;

    @Autowired
    public SystemInitializer(final SystemStatusUtility systemStatusUtility, final AlertProperties alertProperties, final GlobalBlackDuckRepository globalBlackDuckRepository, final EncryptionUtility encryptionUtility) {
        this.systemStatusUtility = systemStatusUtility;
        this.alertProperties = alertProperties;
        this.globalBlackDuckRepository = globalBlackDuckRepository;
        this.encryptionUtility = encryptionUtility;
    }

    @Transactional
    public void updateRequiredConfiguration(final RequiredSystemConfiguration requiredSystemConfiguration) {
        saveEncryptionProperties(requiredSystemConfiguration);
        saveBlackDuckConfiguration(requiredSystemConfiguration);
        systemStatusUtility.setSystemInitialized(true);
    }

    private Optional<GlobalBlackDuckConfigEntity> getGlobalBlackDuckConfigEntity() {
        final List<GlobalBlackDuckConfigEntity> globalConfigList = globalBlackDuckRepository.findAll();
        if (null == globalConfigList || globalConfigList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(globalConfigList.get(0));
        }
    }

    private void saveEncryptionProperties(final RequiredSystemConfiguration requiredSystemConfiguration) {
        // TODO implement this later with the UI changes to perform an initial setup of alert.

    }

    private void saveBlackDuckConfiguration(final RequiredSystemConfiguration requiredSystemConfiguration) {
        final Optional<GlobalBlackDuckConfigEntity> blackDuckConfigEntity = getGlobalBlackDuckConfigEntity();
        final GlobalBlackDuckConfigEntity blackDuckConfigToSave = new GlobalBlackDuckConfigEntity(requiredSystemConfiguration.getBlackDuckConnectionTimeout(),
            requiredSystemConfiguration.getBlackDuckApiToken(),
            requiredSystemConfiguration.getBlackDuckProviderUrl());

        if (blackDuckConfigEntity.isPresent()) {
            blackDuckConfigToSave.setId(blackDuckConfigEntity.get().getId());
        }
        globalBlackDuckRepository.save(blackDuckConfigToSave);
    }
}
