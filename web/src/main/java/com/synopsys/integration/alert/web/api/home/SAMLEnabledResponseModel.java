/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.home;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

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
