/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.saml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml.websso.WebSSOProfileOptions;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;

public class AlertWebSSOProfileOptions extends WebSSOProfileOptions {
    private final transient Logger logger = LoggerFactory.getLogger(AlertWebSSOProfileOptions.class);
    private final SAMLContext samlContext;

    public AlertWebSSOProfileOptions(SAMLContext samlContext) {
        this.samlContext = samlContext;
    }

    @Override
    public Boolean getForceAuthN() {
        try {
            ConfigurationModel currentConfiguration = samlContext.getCurrentConfiguration();
            return samlContext.getFieldValueBoolean(currentConfiguration, AuthenticationDescriptor.KEY_SAML_FORCE_AUTH);
        } catch (AlertException e) {
            logger.error("Could not get the SAML force Auth.", e);
        }
        return false;
    }

}
