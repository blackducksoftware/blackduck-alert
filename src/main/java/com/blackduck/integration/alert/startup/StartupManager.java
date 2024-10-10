/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.startup;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import com.blackduck.integration.alert.common.persistence.accessor.SystemStatusAccessor;
import com.blackduck.integration.alert.startup.component.StartupComponent;

import jakarta.annotation.PostConstruct;

@Configuration
public class StartupManager {
    private final Logger logger = LoggerFactory.getLogger(StartupManager.class);

    private final SystemStatusAccessor systemStatusAccessor;
    private final List<StartupComponent> startupComponents;

    @Autowired
    public StartupManager(SystemStatusAccessor systemStatusAccessor, List<StartupComponent> startupComponents) {
        this.systemStatusAccessor = systemStatusAccessor;
        this.startupComponents = startupComponents;
    }

    @PostConstruct
    @Transactional
    public void init() {
        startup();
    }

    public void startup() {
        logger.info("Alert Starting...");
        systemStatusAccessor.startupOccurred();
        startupComponents.forEach(StartupComponent::initializeComponent);
    }

}
