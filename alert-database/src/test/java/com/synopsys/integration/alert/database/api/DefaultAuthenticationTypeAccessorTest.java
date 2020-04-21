package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.persistence.model.AuthenticationTypeDetails;
import com.synopsys.integration.alert.database.user.AuthenticationTypeEntity;
import com.synopsys.integration.alert.database.user.AuthenticationTypeRepository;

public class DefaultAuthenticationTypeAccessorTest {

    @Test
    public void getAuthenticationTypeDetailsTest() {
        AuthenticationTypeRepository authenticationTypeRepository = Mockito.mock(AuthenticationTypeRepository.class);
        DefaultAuthenticationTypeAccessor authenticationTypeAccessor = new DefaultAuthenticationTypeAccessor(authenticationTypeRepository);
        AuthenticationTypeEntity authenticationTypeEntity = new AuthenticationTypeEntity("name-test");

        Mockito.when(authenticationTypeRepository.findById(Mockito.any())).thenReturn(Optional.of(authenticationTypeEntity));

        Optional<AuthenticationTypeDetails> testAuthenticationTypeDetails = authenticationTypeAccessor.getAuthenticationTypeDetails(AuthenticationType.DATABASE);

        assertTrue(testAuthenticationTypeDetails.isPresent());
        AuthenticationTypeDetails authenticationTypeDetails = testAuthenticationTypeDetails.get();
        assertEquals(authenticationTypeEntity.getName(), authenticationTypeDetails.getName());
        assertEquals(authenticationTypeEntity.getId(), authenticationTypeDetails.getId());
    }

    @Test
    public void getAuthenticationTypeDetailsNullTest() {
        AuthenticationTypeRepository authenticationTypeRepository = Mockito.mock(AuthenticationTypeRepository.class);
        DefaultAuthenticationTypeAccessor authenticationTypeAccessor = new DefaultAuthenticationTypeAccessor(authenticationTypeRepository);

        Mockito.when(authenticationTypeRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        Optional<AuthenticationTypeDetails> testAuthenticationTypeDetails = authenticationTypeAccessor.getAuthenticationTypeDetails(AuthenticationType.DATABASE);
        assertFalse(testAuthenticationTypeDetails.isPresent());
    }

    @Test
    public void getAuthenticationTypeTest() {
        AuthenticationTypeRepository authenticationTypeRepository = Mockito.mock(AuthenticationTypeRepository.class);
        DefaultAuthenticationTypeAccessor authenticationTypeAccessor = new DefaultAuthenticationTypeAccessor(authenticationTypeRepository);

        assertEquals(AuthenticationType.DATABASE, authenticationTypeAccessor.getAuthenticationType(1L).get());
        assertEquals(AuthenticationType.LDAP, authenticationTypeAccessor.getAuthenticationType(2L).get());
        assertEquals(AuthenticationType.SAML, authenticationTypeAccessor.getAuthenticationType(3L).get());
        assertFalse(authenticationTypeAccessor.getAuthenticationType(5L).isPresent());
    }
}
