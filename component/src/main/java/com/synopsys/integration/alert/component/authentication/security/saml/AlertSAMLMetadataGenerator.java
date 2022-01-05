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
import org.springframework.security.saml.metadata.MetadataGenerator;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;

public class AlertSAMLMetadataGenerator extends MetadataGenerator {
    private final Logger logger = LoggerFactory.getLogger(AlertSAMLMetadataGenerator.class);
    private final SAMLContext samlContext;

    public AlertSAMLMetadataGenerator(SAMLContext samlContext) {
        this.samlContext = samlContext;
    }

    @Override
    public boolean isWantAssertionSigned() {
        String wantAssertionsSigned = getEntityString(AuthenticationDescriptor.KEY_SAML_WANT_ASSERTIONS_SIGNED);
        return Boolean.parseBoolean(wantAssertionsSigned);
    }

    @Override
    public String getEntityId() {
        return getEntityString(AuthenticationDescriptor.KEY_SAML_ENTITY_ID);
    }

    @Override
    public String getEntityBaseURL() {
        return getEntityString(AuthenticationDescriptor.KEY_SAML_ENTITY_BASE_URL);
    }

    private String getEntityString(String entityKey) {
        try {
            ConfigurationModel currentConfiguration = samlContext.getCurrentConfiguration();
            return samlContext.getFieldValueOrEmpty(currentConfiguration, entityKey);
        } catch (AlertException e) {
            logger.error("Could not get the SAML entity.", e);
        }
        return "";
    }

}
