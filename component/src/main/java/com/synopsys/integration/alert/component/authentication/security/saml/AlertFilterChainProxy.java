/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.saml;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;

public class AlertFilterChainProxy extends FilterChainProxy {
    final SAMLContext samlContext;

    public AlertFilterChainProxy(List<SecurityFilterChain> chains, SAMLContext samlContext) {
        super(chains);
        this.samlContext = samlContext;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (samlContext.isSAMLEnabledForRequest(request)) {
            super.doFilter(request, response, chain);
            return;
        }
        chain.doFilter(request, response);
    }

}
