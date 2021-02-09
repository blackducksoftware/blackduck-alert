/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.component.authentication.security.saml;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class SamlAntMatcher implements RequestMatcher {
    private final SAMLContext context;
    private final Set<String> enabledPatterns;
    private final Set<String> disabledPatterns;
    private Collection<RequestMatcher> enabledMatchers;
    private Collection<RequestMatcher> disabledMatchers;

    public SamlAntMatcher(final SAMLContext context, final String[] samlEnabledPatterns, final String[] samlDisabledPattern) {
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
    public boolean matches(final HttpServletRequest request) {
        Collection<RequestMatcher> requestMatchers = disabledMatchers;

        if (context.isSAMLEnabled()) {
            requestMatchers = enabledMatchers;
        }

        return requestMatchers.stream().anyMatch(requestMatcher -> requestMatcher.matches(request));
    }
}
