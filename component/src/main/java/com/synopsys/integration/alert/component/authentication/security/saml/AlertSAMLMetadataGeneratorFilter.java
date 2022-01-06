/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.saml;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;

public class AlertSAMLMetadataGeneratorFilter extends MetadataGeneratorFilter {

    private final SAMLContext samlContext;

    public AlertSAMLMetadataGeneratorFilter(MetadataGenerator metadataGenerator, SAMLContext samlContext) {
        super(metadataGenerator);
        this.samlContext = samlContext;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (samlContext.isSAMLEnabledForRequest(request)) {
            super.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

}

