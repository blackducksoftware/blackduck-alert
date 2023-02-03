package com.synopsys.integration.alert.authentication.saml.security;

import com.synopsys.integration.alert.api.authentication.security.event.AuthenticationEventManager;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SAMLGroupConverter {
    private final AuthenticationEventManager authenticationEventManager;

    public SAMLGroupConverter(AuthenticationEventManager authenticationEventManager) {
        this.authenticationEventManager = authenticationEventManager;
    }

    public Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication> groupsConverter() {
        Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication> delegate =
            OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter();

        return responseToken -> {
            Saml2Authentication authentication = delegate.convert(responseToken);
            Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
            List<String> groups = principal.getAttribute("groups");
            Set<GrantedAuthority> authorities = new HashSet<>();
            if (groups != null) {
                groups.stream().map(SimpleGrantedAuthority::new).forEach(authorities::add);
            } else {
                authorities.addAll(authentication.getAuthorities());
            }
            if (authentication.isAuthenticated()) {
                authenticationEventManager.sendAuthenticationEvent(authentication, AuthenticationType.SAML);
            }
            return new Saml2Authentication(principal, authentication.getSaml2Response(), authorities);
        };
    }
}
