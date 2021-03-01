/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.exception;

import java.util.UUID;

public class AlertJobMissingException extends AlertException {
    private static final long serialVersionUID = -1163748183484212814L;

    private final UUID missingUUID;

    public AlertJobMissingException(final String message, final UUID missingUUID) {
        super(message);
        this.missingUUID = missingUUID;
    }

    public UUID getMissingUUID() {
        return missingUUID;
    }
}
