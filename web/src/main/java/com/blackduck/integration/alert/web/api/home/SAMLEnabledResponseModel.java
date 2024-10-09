package com.blackduck.integration.alert.web.api.home;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SAMLEnabledResponseModel extends AlertSerializableModel {
    @JsonAlias("saml_enabled")
    @JsonProperty("saml_enabled")
    private final Boolean samlEnabled;

    public SAMLEnabledResponseModel() {
        // For serialization
        this.samlEnabled = false;
    }

    public SAMLEnabledResponseModel(Boolean samlEnabled) {
        this.samlEnabled = samlEnabled;
    }

    public Boolean getSamlEnabled() {
        return samlEnabled;
    }

}
