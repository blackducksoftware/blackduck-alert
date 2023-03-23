package com.synopsys.integration.alert.authentication.saml.security;

import com.synopsys.integration.alert.api.authentication.security.event.AuthenticationEventManager;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(SpringExtension.class)
class SAMLGroupConverterTest {
    @Mock
    private AuthenticationEventManager authenticationEventManager;
    @Mock
    private Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication> delegate;
    @Mock
    private Saml2AuthenticatedPrincipal principal;
    @Mock
    private OpenSaml4AuthenticationProvider.ResponseToken responseToken;

    private final String ALERT_ROLE_KEY = "AlertRoles";
    private final String GROUPS_ROLE_KEY = "groups";
    private final String EXTERNAL_ROLE = "external";
    private final Map<String, List<String>> ATTRIBUTES = Map.of(
        GROUPS_ROLE_KEY, List.of("everyone"),
        ALERT_ROLE_KEY, List.of("ALERT_ADMIN")
    );

    private SAMLGroupConverter samlGroupConverter;
    private Saml2Authentication authentication;

    @BeforeEach
    void init() {
        samlGroupConverter = new SAMLGroupConverter(authenticationEventManager);
        authentication = new Saml2Authentication(principal, "response",
            Set.of(new SimpleGrantedAuthority(EXTERNAL_ROLE))
        );
    }

    @Test
    void groupsConverterAuthenticatesWithAlertAndGroupAuthorities() {
        Mockito.when(principal.getAttribute(anyString())).thenAnswer(invocation -> ATTRIBUTES.get((String) invocation.getArguments()[0]));
        authentication.setAuthenticated(true);

        try (MockedStatic<OpenSaml4AuthenticationProvider> openSaml4AuthenticationProvider = Mockito.mockStatic(OpenSaml4AuthenticationProvider.class)) {
            openSaml4AuthenticationProvider.when(OpenSaml4AuthenticationProvider::createDefaultResponseAuthenticationConverter)
                .thenReturn(delegate);
            Mockito.when(delegate.convert(responseToken)).thenReturn(authentication);

            Saml2Authentication saml2Authentication = samlGroupConverter.groupsConverter().convert(responseToken);
            Mockito.verify(authenticationEventManager, Mockito.times(1)).sendAuthenticationEvent(any(), eq(AuthenticationType.SAML));

            assert saml2Authentication != null;
            Set<String> grantedAuthorities = saml2Authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
            assertEquals(2, grantedAuthorities.size());
            assertTrue(grantedAuthorities.contains(UserModel.ROLE_PREFIX + ATTRIBUTES.get(ALERT_ROLE_KEY).get(0)));
            assertTrue(grantedAuthorities.contains(ATTRIBUTES.get(GROUPS_ROLE_KEY).get(0)));
            assertFalse(grantedAuthorities.contains(EXTERNAL_ROLE));
        }
    }

    @Test
    void groupsConverterAddsAuthoritiesOnEmptyAtTributeValues() {
        Map<String, List<String>> EMPTY_ATTRIBUTES = new HashMap<>();
        EMPTY_ATTRIBUTES.put(ALERT_ROLE_KEY, null);
        EMPTY_ATTRIBUTES.put(GROUPS_ROLE_KEY, null);

        Mockito.when(principal.getAttribute(anyString())).thenAnswer(invocation -> EMPTY_ATTRIBUTES.get((String) invocation.getArguments()[0]));
        authentication.setAuthenticated(false);

        try (MockedStatic<OpenSaml4AuthenticationProvider> openSaml4AuthenticationProvider = Mockito.mockStatic(OpenSaml4AuthenticationProvider.class)) {
            openSaml4AuthenticationProvider.when(OpenSaml4AuthenticationProvider::createDefaultResponseAuthenticationConverter)
                .thenReturn(delegate);
            Mockito.when(delegate.convert(responseToken)).thenReturn(authentication);

            Saml2Authentication saml2Authentication = samlGroupConverter.groupsConverter().convert(responseToken);
            Mockito.verify(authenticationEventManager, Mockito.times(0)).sendAuthenticationEvent(any(), eq(AuthenticationType.SAML));

            assert saml2Authentication != null;
            Set<String> grantedAuthorities = saml2Authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
            assertEquals(1, grantedAuthorities.size());
            assertTrue(grantedAuthorities.contains(EXTERNAL_ROLE));
        }
    }
}
