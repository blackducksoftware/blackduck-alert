/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.home;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.synopsys.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;

import java.util.Optional;

@Component
public class HomeActions {
    public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";
    private final HttpSessionCsrfTokenRepository csrfTokenRespository;
    private final SAMLConfigAccessor samlConfigAccessor;

    @Autowired
    public HomeActions(HttpSessionCsrfTokenRepository csrfTokenRespository, SAMLConfigAccessor samlConfigAccessor) {
        this.csrfTokenRespository = csrfTokenRespository;
        this.samlConfigAccessor = samlConfigAccessor;
    }

    public ActionResponse<Void> verifyAuthentication(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        CsrfToken csrfToken = csrfTokenRespository.loadToken(servletRequest);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = authentication.getAuthorities().stream()
                                  .map(GrantedAuthority::getAuthority)
                                  .anyMatch(authority -> authority.equals(ROLE_ANONYMOUS));
        boolean authorized = authentication.isAuthenticated() && !isAnonymous && csrfToken != null && !authentication.getAuthorities().isEmpty();

        if (!authorized) {
            servletRequest.getSession().invalidate();
            return new ActionResponse<>(HttpStatus.UNAUTHORIZED);
        } else {
            servletResponse.addHeader(csrfToken.getHeaderName(), csrfToken.getToken());
        }
        return new ActionResponse<>(HttpStatus.OK);
    }
}
