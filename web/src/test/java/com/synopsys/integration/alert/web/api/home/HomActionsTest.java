package com.synopsys.integration.alert.web.api.home;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.component.authentication.security.saml.SAMLContext;

public class HomActionsTest {
    @Test
    public void testVerifyAuthenticationValid() {
        CsrfToken token = Mockito.mock(CsrfToken.class);
        HttpSessionCsrfTokenRepository csrfTokenRepository = Mockito.mock(HttpSessionCsrfTokenRepository.class);
        GrantedAuthority grantedAuthority = Mockito.mock(GrantedAuthority.class);
        Collection<GrantedAuthority> grantedAuthorities = List.of(grantedAuthority);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(csrfTokenRepository.loadToken(Mockito.any())).thenReturn(token);
        Mockito.when(token.getHeaderName()).thenReturn("csrfHeaderName");
        Mockito.when(token.getToken()).thenReturn("csrftoken");
        Mockito.when(grantedAuthority.getAuthority()).thenReturn("ValidRole");
        Mockito.doReturn(grantedAuthorities).when(authentication).getAuthorities();
        Mockito.when(authentication.isAuthenticated()).thenReturn(Boolean.TRUE);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();

        HomeActions actions = new HomeActions(csrfTokenRepository, null);

        ActionResponse<Void> response = actions.verifyAuthentication(servletRequest, servletResponse);
        Assertions.assertTrue(response.isSuccessful());
        Assertions.assertFalse(response.hasContent());
    }

    @Test
    public void testVerifyAuthenticationInvalidAnonymousUser() {
        CsrfToken token = Mockito.mock(CsrfToken.class);
        HttpSessionCsrfTokenRepository csrfTokenRepository = Mockito.mock(HttpSessionCsrfTokenRepository.class);
        GrantedAuthority grantedAuthority = Mockito.mock(GrantedAuthority.class);
        Collection<GrantedAuthority> grantedAuthorities = List.of(grantedAuthority);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(csrfTokenRepository.loadToken(Mockito.any())).thenReturn(token);
        Mockito.when(token.getHeaderName()).thenReturn("csrfHeaderName");
        Mockito.when(token.getToken()).thenReturn("csrftoken");
        Mockito.when(grantedAuthority.getAuthority()).thenReturn(HomeActions.ROLE_ANONYMOUS);
        Mockito.doReturn(grantedAuthorities).when(authentication).getAuthorities();
        Mockito.when(authentication.isAuthenticated()).thenReturn(Boolean.TRUE);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();

        HomeActions actions = new HomeActions(csrfTokenRepository, null);

        ActionResponse<Void> response = actions.verifyAuthentication(servletRequest, servletResponse);
        Assertions.assertTrue(response.isError());
        Assertions.assertFalse(response.hasContent());
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getHttpStatus());
    }

    @Test
    public void testVerifyAuthenticationInvalidAuthentication() {
        CsrfToken token = Mockito.mock(CsrfToken.class);
        HttpSessionCsrfTokenRepository csrfTokenRepository = Mockito.mock(HttpSessionCsrfTokenRepository.class);
        GrantedAuthority grantedAuthority = Mockito.mock(GrantedAuthority.class);
        Collection<GrantedAuthority> grantedAuthorities = List.of(grantedAuthority);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(csrfTokenRepository.loadToken(Mockito.any())).thenReturn(token);
        Mockito.when(token.getHeaderName()).thenReturn("csrfHeaderName");
        Mockito.when(token.getToken()).thenReturn("csrftoken");
        Mockito.when(grantedAuthority.getAuthority()).thenReturn("ValidRole");
        Mockito.doReturn(grantedAuthorities).when(authentication).getAuthorities();
        Mockito.when(authentication.isAuthenticated()).thenReturn(Boolean.FALSE);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();

        HomeActions actions = new HomeActions(csrfTokenRepository, null);

        ActionResponse<Void> response = actions.verifyAuthentication(servletRequest, servletResponse);
        Assertions.assertTrue(response.isError());
        Assertions.assertFalse(response.hasContent());
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getHttpStatus());
    }

    @Test
    public void testVerifyAuthenticationInvalidCSRFToken() {
        CsrfToken token = Mockito.mock(CsrfToken.class);
        HttpSessionCsrfTokenRepository csrfTokenRepository = Mockito.mock(HttpSessionCsrfTokenRepository.class);
        GrantedAuthority grantedAuthority = Mockito.mock(GrantedAuthority.class);
        Collection<GrantedAuthority> grantedAuthorities = List.of(grantedAuthority);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(csrfTokenRepository.loadToken(Mockito.any())).thenReturn(null);
        Mockito.when(grantedAuthority.getAuthority()).thenReturn("ValidRole");
        Mockito.doReturn(grantedAuthorities).when(authentication).getAuthorities();
        Mockito.when(authentication.isAuthenticated()).thenReturn(Boolean.TRUE);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();

        HomeActions actions = new HomeActions(csrfTokenRepository, null);

        ActionResponse<Void> response = actions.verifyAuthentication(servletRequest, servletResponse);
        Assertions.assertTrue(response.isError());
        Assertions.assertFalse(response.hasContent());
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getHttpStatus());
    }

    @Test
    public void testVerifySaml() {
        SAMLContext samlContext = Mockito.mock(SAMLContext.class);
        Mockito.when(samlContext.isSAMLEnabled()).thenReturn(Boolean.TRUE);
        HomeActions actions = new HomeActions(null, samlContext);
        ActionResponse<SAMLEnabledResponseModel> response = actions.verifySaml();
        Assertions.assertTrue(response.isSuccessful());
        Assertions.assertTrue(response.hasContent());
        SAMLEnabledResponseModel samlEnabledResponseModel = response.getContent().orElse(null);
        Assertions.assertTrue(samlEnabledResponseModel.getSamlEnabled());
    }
}
