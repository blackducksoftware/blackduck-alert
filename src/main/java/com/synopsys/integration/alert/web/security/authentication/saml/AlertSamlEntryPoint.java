package com.synopsys.integration.alert.web.security.authentication.saml;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.common.xml.SAMLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.websso.WebSSOProfileOptions;

import com.google.common.net.HttpHeaders;
import com.synopsys.integration.rest.RestConstants;

public class AlertSamlEntryPoint extends SAMLEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(AlertSamlEntryPoint.class);
    private static final String SAML_PROVIDER_NAME = "Synopsys - Alert";
    private static final String REQUEST_HEADER_FOR_UI_XHR = "XMLHttpRequest";

    private final WebSSOProfileOptions defaultWebSSOProfileOptions;

    public AlertSamlEntryPoint() {

        defaultWebSSOProfileOptions = new WebSSOProfileOptions();
        defaultWebSSOProfileOptions.setIncludeScoping(false);
        defaultWebSSOProfileOptions.setProviderName(SAML_PROVIDER_NAME);
        defaultWebSSOProfileOptions.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
        defaultWebSSOProfileOptions.setForceAuthN(true);
        //TODO get property
        //        defaultWebSSOProfileOptions.setForceAuthN(samlContext.isLocalLogoutEnabled());
    }

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException e) {
        if (true) {
            //TODO get property
            //        if (samlContext.isSamlSSOEnabled()) {

            // SAML is enabled, attempt to authorize via IDP.
            logger.debug("Trying to authenticate with Single Sign On.");
            try {
                response.addHeader("X-SAML-LOGIN", "true");
                if (REQUEST_HEADER_FOR_UI_XHR.equals(request.getHeader(HttpHeaders.X_REQUESTED_WITH))) {
                    response.setStatus(RestConstants.UNAUTHORIZED_401);
                }
                refreshForceAuthnenticationFlag();
                super.commence(request, response, e);
                return;
            } catch (final ServletException | RuntimeException | IOException ex) {
                throw new RuntimeException("Saml configuration is invalid.", ex);

            }
        } else {
            logger.debug("Single Sign On is disabled by administrator.");
        }
    }

    private void refreshForceAuthnenticationFlag() {
        //TODO get property
        // defaultWebSSOProfileOptions.setForceAuthN(samlContext.isLocalLogoutEnabled());
        defaultWebSSOProfileOptions.setForceAuthN(true);
        super.setDefaultProfileOptions(defaultWebSSOProfileOptions);
    }
}

