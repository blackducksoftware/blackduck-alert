/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.startup.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StartupComponent {
    private final Logger logger = LoggerFactory.getLogger(StartupComponent.class);

    public void initializeComponent() {
        String runningStartupComponentLog = String.format("Running startup component: %s", getClass().getSimpleName());
        logger.info(runningStartupComponentLog);
        initialize();
    }

    protected abstract void initialize();

}
