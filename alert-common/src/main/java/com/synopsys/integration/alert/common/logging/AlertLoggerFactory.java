/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AlertLoggerFactory {
    public static final String ALERT_AUDIT_LOGGER = "Alert Audit";
    public static final String ALERT_NOTIFICATION_LOGGER = "Alert Notification";

    private AlertLoggerFactory() {
        // This class should not be instantiated
    }

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

    public static Logger getNotificationLogger(Class<?> clazz) {
        Logger logger = LoggerFactory.getLogger(clazz);
        Logger notificationLogger = LoggerFactory.getLogger(ALERT_NOTIFICATION_LOGGER);

        return new AlertCompositeLogger(notificationLogger, logger);
    }

    public static Logger getNotificationLogger(String name) {
        Logger logger = LoggerFactory.getLogger(name);
        Logger notificationLogger = LoggerFactory.getLogger(ALERT_NOTIFICATION_LOGGER);

        return new AlertCompositeLogger(notificationLogger, logger);
    }

}
