package com.blackduck.integration.alert.common.action;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;

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
