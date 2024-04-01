package com.synopsys.integration.alert.authentication.saml.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertRelyingPartRegistrationRepositoryTest {
    private AlertRelyingPartyRegistrationRepository alertRelyingPartyRegistrationRepository;
    private RelyingPartyRegistration defaultRelyingParty;

    private final String DEFAULT_ID = "default";

    @BeforeEach
    void init() {
        alertRelyingPartyRegistrationRepository = new AlertRelyingPartyRegistrationRepository();
        defaultRelyingParty = buildMockRelyingPartyRegistration(DEFAULT_ID);
    }

    @Test
    void registerAddsRegistrationToRepository() {
        assertNull(alertRelyingPartyRegistrationRepository.findByRegistrationId(DEFAULT_ID));

        alertRelyingPartyRegistrationRepository.registerRelyingPartyRegistration(defaultRelyingParty);
        assertEquals(defaultRelyingParty.getRegistrationId(), alertRelyingPartyRegistrationRepository.findByRegistrationId(DEFAULT_ID).getRegistrationId());
    }

    @Test
    void unregisterRemovesRegistrationFromRepository() {
        alertRelyingPartyRegistrationRepository.registerRelyingPartyRegistration(defaultRelyingParty);
        assertEquals(defaultRelyingParty.getRegistrationId(), alertRelyingPartyRegistrationRepository.findByRegistrationId(DEFAULT_ID).getRegistrationId());

        alertRelyingPartyRegistrationRepository.unregisterRelyingPartyRegistration();
        assertNull(alertRelyingPartyRegistrationRepository.findByRegistrationId(DEFAULT_ID));
    }

    @Test
    void iteratorAppliesToRegistered() {
        alertRelyingPartyRegistrationRepository.registerRelyingPartyRegistration(defaultRelyingParty);
        Iterator<RelyingPartyRegistration> itr = alertRelyingPartyRegistrationRepository.iterator();
        assertEquals(defaultRelyingParty.getRegistrationId(), itr.next().getRegistrationId());
        assertFalse(itr.hasNext());
    }

    @Test
    void forEachAppliesToRegistered() {
        Set<String> registeredIds = new HashSet<>();
        alertRelyingPartyRegistrationRepository.registerRelyingPartyRegistration(defaultRelyingParty);
        alertRelyingPartyRegistrationRepository.forEach(registeredParty -> registeredIds.add(registeredParty.getRegistrationId()));
        assertTrue(registeredIds.contains(defaultRelyingParty.getRegistrationId()));
        assertEquals(1, registeredIds.size());
    }

    @Test
    void spliteratorAppliesToRegistered() {
        Set<String> registeredIds = new HashSet<>();
        alertRelyingPartyRegistrationRepository.registerRelyingPartyRegistration(defaultRelyingParty);
        Spliterator<RelyingPartyRegistration> itr = alertRelyingPartyRegistrationRepository.spliterator();
        itr.forEachRemaining(relyingParty -> registeredIds.add(relyingParty.getRegistrationId()));
        assertTrue(registeredIds.contains(defaultRelyingParty.getRegistrationId()));
    }

    private RelyingPartyRegistration buildMockRelyingPartyRegistration(String registrationId) {
        RelyingPartyRegistration mockRelyingPartyRegistration = Mockito.mock(RelyingPartyRegistration.class);
        Mockito.when(mockRelyingPartyRegistration.getRegistrationId()).thenReturn(registrationId);
        return mockRelyingPartyRegistration;
    }
}
