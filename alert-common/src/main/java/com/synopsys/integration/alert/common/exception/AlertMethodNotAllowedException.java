/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.exception;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;

public class AlertMethodNotAllowedException extends AlertException {

    public AlertMethodNotAllowedException(String message) {
        super("Method not allowed. - " + message);
    }

}
