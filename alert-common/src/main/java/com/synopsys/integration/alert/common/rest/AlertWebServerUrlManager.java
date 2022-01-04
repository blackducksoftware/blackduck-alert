/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest;

import java.util.Optional;

import org.springframework.web.util.UriComponentsBuilder;

public interface AlertWebServerUrlManager {

    public UriComponentsBuilder getServerComponentsBuilder();

    public Optional<String> getServerUrl(String... pathSegments);

}
