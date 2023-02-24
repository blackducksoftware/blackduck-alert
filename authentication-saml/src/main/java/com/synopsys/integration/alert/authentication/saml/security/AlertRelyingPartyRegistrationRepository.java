package com.synopsys.integration.alert.authentication.saml.security;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

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
    public RelyingPartyRegistration findByRegistrationId(final String registrationId) {
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
    public void forEach(final Consumer<? super RelyingPartyRegistration> action) {
        registrationMap.values().forEach(action);
    }

    @Override
    public Spliterator<RelyingPartyRegistration> spliterator() {
        return registrationMap.values().spliterator();
    }
}
