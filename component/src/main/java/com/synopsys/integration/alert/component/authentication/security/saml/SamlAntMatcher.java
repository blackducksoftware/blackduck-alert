/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.saml;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class SamlAntMatcher implements RequestMatcher {
    private final SAMLContext context;
    private final Set<String> enabledPatterns;
    private final Set<String> disabledPatterns;
    private Collection<RequestMatcher> enabledMatchers;
    private Collection<RequestMatcher> disabledMatchers;

    public SamlAntMatcher(SAMLContext context, String[] samlEnabledPatterns, String[] samlDisabledPattern) {
        this.context = context;
        this.enabledPatterns = Set.of(samlEnabledPatterns);
        this.disabledPatterns = Set.of(samlDisabledPattern);
        createAntMatchers();
    }

    private void createAntMatchers() {
        enabledMatchers = enabledPatterns.stream().map(AntPathRequestMatcher::new).collect(Collectors.toList());
        disabledMatchers = disabledPatterns.stream().map(AntPathRequestMatcher::new).collect(Collectors.toList());
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        Collection<RequestMatcher> requestMatchers = disabledMatchers;

        String ignoreSAMLParam = request.getParameter("ignoreSAML");
        boolean ignoreSAML = BooleanUtils.toBoolean(ignoreSAMLParam);
        if (context.isSAMLEnabled() && !ignoreSAML) {
            requestMatchers = enabledMatchers;
        }

        return requestMatchers.stream().anyMatch(requestMatcher -> requestMatcher.matches(request));
    }

}
