/*
 * web
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.rest;

import java.net.MalformedURLException;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.synopsys.integration.alert.common.rest.AlertWebServerUrlManager;

@Component
public class AlertWebServerUrlManagerImpl implements AlertWebServerUrlManager {
    @Override
    public UriComponentsBuilder getServerComponentsBuilder() {
        return ServletUriComponentsBuilder.fromCurrentContextPath();
    }

    @Override
    public Optional<String> getServerUrl(String... pathSegments) {
        UriComponentsBuilder uriComponentsBuilder = getServerComponentsBuilder();
        uriComponentsBuilder.pathSegment(pathSegments);
        try {
            String serverUrl = uriComponentsBuilder.build().toUri().toURL().toString();
            return Optional.of(serverUrl);
        } catch (MalformedURLException e) {
            return Optional.empty();
        }
    }
}
