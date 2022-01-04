/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.rest;

import java.net.MalformedURLException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.rest.AlertWebServerUrlManager;

@Component
public class AlertWebServerUrlManagerImpl implements AlertWebServerUrlManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AlertProperties alertProperties;

    @Autowired
    public AlertWebServerUrlManagerImpl(AlertProperties alertProperties) {
        this.alertProperties = alertProperties;
    }

    @Override
    public UriComponentsBuilder getServerComponentsBuilder() {
        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath();
        } catch (Exception e) {
            logger.warn("Could not get Alert's URL from the current servlet context. Falling back to AlertProperties...");
            return alertProperties.createPopulatedUriComponentsBuilderForServerURL();
        }
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
