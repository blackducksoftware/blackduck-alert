package com.synopsys.integration.alert.component.authentication.web;

import java.io.Serial;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class AuthenticationResponseModel extends AlertSerializableModel {

    @Serial
    private static final long serialVersionUID = -8585360211505351102L;

    private final int statusCode;
    private final String message;

    public AuthenticationResponseModel(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
