/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.saml.security;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.stereotype.Component;

@Component
public class AlertRelyingPartyRegistrationRepository implements RelyingPartyRegistrationRepository, Iterable<RelyingPartyRegistration> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, RelyingPartyRegistration> registrationMap = new ConcurrentHashMap<>();

    public void registerRelyingPartyRegistration(RelyingPartyRegistration relyingPartyRegistration) {
        String registrationId = relyingPartyRegistration.getRegistrationId();
        logger.info("Registering SAML party with id {}.", registrationId);
        if(StringUtils.isNotBlank(registrationId)) {
            registrationMap.put(relyingPartyRegistration.getRegistrationId(), relyingPartyRegistration);
        }
    }

    public void unregisterRelyingPartyRegistration() {
        logger.info("Unregistering SAML parties.");
        registrationMap.clear();
    }

    @Override
    public RelyingPartyRegistration findByRegistrationId(String registrationId) {
        logger.info("Finding registration with id {}.", registrationId);
        RelyingPartyRegistration registration =  registrationMap.get(registrationId);
        logger.info("Registration: {}", registration);
        return registration;
    }

    @Override
    public Iterator<RelyingPartyRegistration> iterator() {
        return registrationMap.values().iterator();
    }

    @Override
    public void forEach(Consumer<? super RelyingPartyRegistration> action) {
        registrationMap.values().forEach(action);
    }

    @Override
    public Spliterator<RelyingPartyRegistration> spliterator() {
        return registrationMap.values().spliterator();
    }
}
