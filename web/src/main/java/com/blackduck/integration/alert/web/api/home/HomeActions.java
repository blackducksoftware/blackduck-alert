package com.blackduck.integration.alert.web.api.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.authentication.saml.security.SAMLManager;
import com.blackduck.integration.alert.common.action.ActionResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class HomeActions {
    public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";
    private final HttpSessionCsrfTokenRepository csrfTokenRepository;
    private final SAMLManager samlManager;

    @Autowired
    public HomeActions(HttpSessionCsrfTokenRepository csrfTokenRepository, SAMLManager samlManager) {
        this.csrfTokenRepository = csrfTokenRepository;
        this.samlManager = samlManager;
    }

    public ActionResponse<VerifyAuthenticationResponseModel> verifyAuthentication(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        CsrfToken csrfToken = csrfTokenRepository.loadToken(servletRequest);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(authority -> authority.equals(ROLE_ANONYMOUS));
        boolean authorized = authentication.isAuthenticated() && !isAnonymous && csrfToken != null && !authentication.getAuthorities().isEmpty();
        VerifyAuthenticationResponseModel responseModel = new VerifyAuthenticationResponseModel(authorized);
        if (!authorized) {
            servletRequest.getSession().invalidate();
        } else {
            servletResponse.addHeader(csrfToken.getHeaderName(), csrfToken.getToken());
        }
        return new ActionResponse<>(HttpStatus.OK, responseModel);
    }

    public ActionResponse<SAMLEnabledResponseModel> verifySaml() {
        return new ActionResponse<>(HttpStatus.OK, new SAMLEnabledResponseModel(samlManager.isSAMLEnabled()));
    }
}
