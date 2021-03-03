/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertLoggerFactory {
    public static final String ALERT_AUDIT_LOGGER = "Alert Audit";

    public static Logger getLogger(Class<?> clazz) {
        Logger logger = LoggerFactory.getLogger(clazz);
        Logger auditLogger = LoggerFactory.getLogger(ALERT_AUDIT_LOGGER);

        return new AlertCompositeLogger(auditLogger, logger);
    }

    public static Logger getLogger(String name) {
        Logger logger = LoggerFactory.getLogger(name);
        Logger auditLogger = LoggerFactory.getLogger(ALERT_AUDIT_LOGGER);

        return new AlertCompositeLogger(auditLogger, logger);
    }
}
