/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.exception;

public class AlertNotificationPurgedException extends AlertException {
    private static final long serialVersionUID = -1163748183484212814L;

    public AlertNotificationPurgedException(final String message) {
        super(message);
    }

}
