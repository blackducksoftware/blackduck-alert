package com.synopsys.integration.alert.authentication.saml.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

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

    private RelyingPartyRegistration buildMockRelyingPartyRegistration(String registrationId) {
        RelyingPartyRegistration mockRelyingPartyRegistration = Mockito.mock(RelyingPartyRegistration.class);
        Mockito.when(mockRelyingPartyRegistration.getRegistrationId()).thenReturn(registrationId);
        return mockRelyingPartyRegistration;
    }
}
