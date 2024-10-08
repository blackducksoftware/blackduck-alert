package com.blackduck.integration.alert.web.api.home;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class HomeActionsTest {
    private HttpServletRequest servletRequest;
    private HttpServletResponse servletResponse;
    private HttpSessionCsrfTokenRepository csrfTokenRepository;

    @BeforeEach
    public void init() {
        servletRequest = new MockHttpServletRequest();
        servletResponse = new MockHttpServletResponse();
        csrfTokenRepository = new HttpSessionCsrfTokenRepository();
    }

    @Test
    void testVerifyAuthenticationValid() {
        CsrfToken token = Mockito.mock(CsrfToken.class);
        GrantedAuthority grantedAuthority = Mockito.mock(GrantedAuthority.class);
        Collection<GrantedAuthority> grantedAuthorities = List.of(grantedAuthority);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        csrfTokenRepository.saveToken(token, servletRequest, servletResponse);
        Mockito.when(token.getHeaderName()).thenReturn("csrfHeaderName");
        Mockito.when(token.getToken()).thenReturn("csrftoken");
        Mockito.when(grantedAuthority.getAuthority()).thenReturn("ValidRole");
        Mockito.doReturn(grantedAuthorities).when(authentication).getAuthorities();
        Mockito.when(authentication.isAuthenticated()).thenReturn(Boolean.TRUE);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        HomeActions actions = new HomeActions(csrfTokenRepository, null);

        ActionResponse<VerifyAuthenticationResponseModel> response = actions.verifyAuthentication(servletRequest, servletResponse);
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertTrue(response.getContent().orElseThrow(() -> new AssertionError("Content is missing from the response.")).isAuthenticated());
    }

    @Test
    void testVerifyAuthenticationInvalidAnonymousUser() {
        CsrfToken token = Mockito.mock(CsrfToken.class);
        GrantedAuthority grantedAuthority = Mockito.mock(GrantedAuthority.class);
        Collection<GrantedAuthority> grantedAuthorities = List.of(grantedAuthority);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        csrfTokenRepository.saveToken(token, servletRequest, servletResponse);
        Mockito.when(token.getHeaderName()).thenReturn("csrfHeaderName");
        Mockito.when(token.getToken()).thenReturn("csrftoken");
        Mockito.when(grantedAuthority.getAuthority()).thenReturn(HomeActions.ROLE_ANONYMOUS);
        Mockito.doReturn(grantedAuthorities).when(authentication).getAuthorities();
        Mockito.when(authentication.isAuthenticated()).thenReturn(Boolean.TRUE);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        HomeActions actions = new HomeActions(csrfTokenRepository, null);

        ActionResponse<VerifyAuthenticationResponseModel> response = actions.verifyAuthentication(servletRequest, servletResponse);
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertFalse(response.getContent().orElseThrow(() -> new AssertionError("Content is missing from the response.")).isAuthenticated());
    }

    @Test
    void testVerifyAuthenticationInvalidAuthentication() {
        CsrfToken token = Mockito.mock(CsrfToken.class);
        GrantedAuthority grantedAuthority = Mockito.mock(GrantedAuthority.class);
        Collection<GrantedAuthority> grantedAuthorities = List.of(grantedAuthority);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        csrfTokenRepository.saveToken(token, servletRequest, servletResponse);
        Mockito.when(token.getHeaderName()).thenReturn("csrfHeaderName");
        Mockito.when(token.getToken()).thenReturn("csrftoken");
        Mockito.when(grantedAuthority.getAuthority()).thenReturn("ValidRole");
        Mockito.doReturn(grantedAuthorities).when(authentication).getAuthorities();
        Mockito.when(authentication.isAuthenticated()).thenReturn(Boolean.FALSE);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        HomeActions actions = new HomeActions(csrfTokenRepository, null);

        ActionResponse<VerifyAuthenticationResponseModel> response = actions.verifyAuthentication(servletRequest, servletResponse);
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertFalse(response.getContent().orElseThrow(() -> new AssertionError("Content is missing from the response.")).isAuthenticated());
    }

    @Test
    void testVerifyAuthenticationInvalidCSRFToken() {
        GrantedAuthority grantedAuthority = Mockito.mock(GrantedAuthority.class);
        Collection<GrantedAuthority> grantedAuthorities = List.of(grantedAuthority);
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(grantedAuthority.getAuthority()).thenReturn("ValidRole");
        Mockito.doReturn(grantedAuthorities).when(authentication).getAuthorities();
        Mockito.when(authentication.isAuthenticated()).thenReturn(Boolean.TRUE);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);

        HomeActions actions = new HomeActions(csrfTokenRepository, null);

        ActionResponse<VerifyAuthenticationResponseModel> response = actions.verifyAuthentication(servletRequest, servletResponse);
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertFalse(response.getContent().orElseThrow(() -> new AssertionError("Content is missing from the response.")).isAuthenticated());
    }
}
