/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.authentication.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

public class RequireSecureChannelFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.isSecure()) {
            filterChain.doFilter(request, response);
            return;
        }

        String secureUrl = UriComponentsBuilder.fromUriString(request.getRequestURL().toString())
                .scheme("https")
                .port(-1)
                .replaceQuery(request.getQueryString())
                .build()
                .toUriString();
        response.sendRedirect(secureUrl);
    }
}