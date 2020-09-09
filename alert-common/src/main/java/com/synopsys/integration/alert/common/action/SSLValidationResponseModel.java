package com.synopsys.integration.alert.common.action;

import java.util.Map;

import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;

public class SSLValidationResponseModel extends ValidationResponseModel {
    private boolean isDetailed;

    public SSLValidationResponseModel(String message) {
        super(message, Map.of());
        this.isDetailed = true;
    }

    public boolean isDetailed() {
        return isDetailed;
    }
}
