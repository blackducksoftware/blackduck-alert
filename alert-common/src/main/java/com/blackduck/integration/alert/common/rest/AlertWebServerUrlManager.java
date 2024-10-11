/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest;

import java.util.Optional;

import org.springframework.web.util.UriComponentsBuilder;

public interface AlertWebServerUrlManager {

    public UriComponentsBuilder getServerComponentsBuilder();

    public Optional<String> getServerUrl(String... pathSegments);

}
