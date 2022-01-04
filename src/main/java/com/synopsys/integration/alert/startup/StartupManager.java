/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.startup;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.SystemStatusAccessor;
import com.synopsys.integration.alert.startup.component.StartupComponent;

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
