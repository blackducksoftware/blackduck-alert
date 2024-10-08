package com.synopsys.integration.alert.authentication.saml.security;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.authentication.security.event.AuthenticationEventManager;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.persistence.model.UserModel;

@Component
public class SAMLGroupConverter {
    private final AuthenticationEventManager authenticationEventManager;
    private UserDetailsService userDetailsService;

    public SAMLGroupConverter(UserDetailsService userDetailsService, AuthenticationEventManager authenticationEventManager) {
        this.authenticationEventManager = authenticationEventManager;
        this.userDetailsService = userDetailsService;
    }

    public Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication> groupsConverter() {
        Converter<OpenSaml4AuthenticationProvider.ResponseToken, Saml2Authentication> delegate =
            OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter();

        return responseToken -> {
            Saml2Authentication authentication = delegate.convert(responseToken);
            Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
            Set<GrantedAuthority> authorities = createGrantedAuthoritiesSet(authentication, principal);

            Saml2Authentication saml2Authentication = new Saml2Authentication(principal, authentication.getSaml2Response(), authorities);

            if (authentication.isAuthenticated()) {
                authenticationEventManager.sendAuthenticationEvent(saml2Authentication, AuthenticationType.SAML);
            }
            return saml2Authentication;
        };
    }

    private Set<GrantedAuthority> createGrantedAuthoritiesSet(Saml2Authentication authentication, Saml2AuthenticatedPrincipal principal) {
        List<String> groups = principal.getAttribute("groups");
        Set<GrantedAuthority> authorities = new HashSet<>();
        List<String> alertRoles = principal.getAttribute("AlertRoles");

        // Grant the user its existing authorities as well
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(principal.getName());
            authorities.addAll(userDetails.getAuthorities());
        } catch (UsernameNotFoundException ignored) {
            // username not found means it has not been previously granted authorities which is ok
        }

        if (alertRoles != null) {
            alertRoles.stream()
                .map(attr -> StringUtils.join(UserModel.ROLE_PREFIX, attr))
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        }
        if (groups != null) {
            groups.stream().map(SimpleGrantedAuthority::new).forEach(authorities::add);
        }
        if (alertRoles == null && groups == null) {
            authorities.addAll(authentication.getAuthorities());
        }

        return authorities;
    }
}
