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
package com.synopsys.integration.alert.workflow.startup.install;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.api.user.UserAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.workflow.startup.SystemValidator;

@Component
public class SystemInitializer {
    private final Logger logger = LoggerFactory.getLogger(SystemInitializer.class);
    private final SystemStatusUtility systemStatusUtility;
    private final AlertProperties alertProperties;
    private final GlobalBlackDuckRepository globalBlackDuckRepository;
    private final EncryptionUtility encryptionUtility;
    private final SystemValidator systemValidator;
    private final UserAccessor userAccessor;

    @Autowired
    public SystemInitializer(final SystemStatusUtility systemStatusUtility, final AlertProperties alertProperties, final GlobalBlackDuckRepository globalBlackDuckRepository, final EncryptionUtility encryptionUtility,
        final SystemValidator systemValidator, final UserAccessor userAccessor) {
        this.systemStatusUtility = systemStatusUtility;
        this.alertProperties = alertProperties;
        this.globalBlackDuckRepository = globalBlackDuckRepository;
        this.encryptionUtility = encryptionUtility;
        this.systemValidator = systemValidator;
        this.userAccessor = userAccessor;
    }

    public boolean isSystemInitialized() {
        return systemStatusUtility.isSystemInitialized();
    }

    @Transactional
    public RequiredSystemConfiguration getCurrentSystemSetup() {
        final Optional<String> proxyHost = alertProperties.getAlertProxyHost();
        final Optional<String> proxyPort = alertProperties.getAlertProxyPort();
        final Optional<String> proxyUsername = alertProperties.getAlertProxyUsername();
        final Optional<String> proxyPassword = alertProperties.getAlertProxyPassword();
        final Optional<GlobalBlackDuckConfigEntity> blackDuckConfigEntity = getGlobalBlackDuckConfigEntity();
        String blackDuckUrl = null;
        Integer blackDuckConnectionTimeout = null;
        String blackDuckApiToken = null;
        if (blackDuckConfigEntity.isPresent()) {
            final GlobalBlackDuckConfigEntity blackDuckEntity = blackDuckConfigEntity.get();
            blackDuckUrl = blackDuckEntity.getBlackDuckUrl();
            blackDuckConnectionTimeout = blackDuckEntity.getBlackDuckTimeout();
            blackDuckApiToken = blackDuckEntity.getBlackDuckApiKey();
        }

        return new RequiredSystemConfiguration(
            true,
            blackDuckUrl,
            blackDuckConnectionTimeout,
            blackDuckApiToken,
            encryptionUtility.isPasswordSet(),
            encryptionUtility.isGlobalSaltSet(),
            proxyHost.orElse(null),
            proxyPort.orElse(null),
            proxyUsername.orElse(null),
            proxyPassword.orElse(null));

    }

    @Transactional
    public boolean updateRequiredConfiguration(final RequiredSystemConfiguration requiredSystemConfiguration, final Map<String, String> fieldErrors) {
        logger.info("updating required configuration for initialization");
        saveEncryptionProperties(requiredSystemConfiguration);
        saveProxySettings(requiredSystemConfiguration);
        saveBlackDuckConfiguration(requiredSystemConfiguration);
        return systemValidator.validate(fieldErrors);
    }

    private void saveProxySettings(final RequiredSystemConfiguration requiredSystemConfiguration) {
        alertProperties.setAlertProxyHost(requiredSystemConfiguration.getProxyHost());
        alertProperties.setAlertProxyPort(requiredSystemConfiguration.getProxyPort());
        alertProperties.setAlertProxyUsername(requiredSystemConfiguration.getProxyUsername());
        alertProperties.setAlertProxyPassword(requiredSystemConfiguration.getProxyPassword());
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
        try {
            encryptionUtility.updateEncryptionFields(requiredSystemConfiguration.getGlobalEncryptionPassword(), requiredSystemConfiguration.getGlobalEncryptionSalt());
        } catch (final IllegalArgumentException | IOException ex) {
            logger.error("Error saving encryption configuration during intialization.", ex);
        }
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
        if (StringUtils.isNotBlank(requiredSystemConfiguration.getDefaultAdminPassword())) {
            userAccessor.changeUserPassword(UserAccessor.DEFAULT_ADMIN_USER, requiredSystemConfiguration.getDefaultAdminPassword());
        }
    }
}
