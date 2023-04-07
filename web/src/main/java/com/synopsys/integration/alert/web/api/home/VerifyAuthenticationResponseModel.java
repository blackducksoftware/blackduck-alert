package com.synopsys.integration.alert.web.api.home;

import java.io.Serial;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class VerifyAuthenticationResponseModel extends AlertSerializableModel {
    @Serial
    private static final long serialVersionUID = -513519901474390784L;
    
    public final boolean authenticated;

    public VerifyAuthenticationResponseModel() {
        this.authenticated = false;
    }

    public VerifyAuthenticationResponseModel(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
