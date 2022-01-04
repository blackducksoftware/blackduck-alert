/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.action;

import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;

public class SSLValidationResponseModel extends ValidationResponseModel {
    private final boolean isDetailed;

    public SSLValidationResponseModel(String message) {
        super(message, true);
        this.isDetailed = true;
    }

    public boolean isDetailed() {
        return isDetailed;
    }
}
