/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.startup.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.component.authentication.security.saml.SAMLManager;

@Component
@Order(70)
public class SAMLStartupComponent extends StartupComponent {
    private final Logger logger = LoggerFactory.getLogger(SAMLStartupComponent.class);
    private final SAMLManager samlManager;

    @Autowired
    public SAMLStartupComponent(SAMLManager samlManager) {
        this.samlManager = samlManager;
    }

    @Override
    protected void initialize() {
        logger.info("SAML startup initialization running.");
        samlManager.initializeConfiguration();
    }

}
