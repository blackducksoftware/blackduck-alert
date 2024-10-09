package com.blackduck.integration.alert.common.rest;

import java.util.Optional;

import org.springframework.web.util.UriComponentsBuilder;

public interface AlertWebServerUrlManager {

    public UriComponentsBuilder getServerComponentsBuilder();

    public Optional<String> getServerUrl(String... pathSegments);

}
