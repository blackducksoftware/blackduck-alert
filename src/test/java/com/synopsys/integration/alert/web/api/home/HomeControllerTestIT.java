package com.synopsys.integration.alert.web.api.home;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.blackduck.integration.alert.web.api.home.VerifyAuthenticationResponseModel;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

@AlertIntegrationTest
public class HomeControllerTestIT {
    private static final String HOME_VERIFY_URL = AlertRestConstants.BASE_PATH + "/verify";
    private static final String HOME_URL = "/";
    private static final String HOME_SAML_VERIFY_URL = AlertRestConstants.BASE_PATH + "/verify/saml";

    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected HttpSessionCsrfTokenRepository csrfTokenRepository;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testVerify() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        MockHttpSession session = new MockHttpSession();
        ServletContext servletContext = webApplicationContext.getServletContext();

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(HOME_VERIFY_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN));
        request.session(session);
        HttpServletRequest httpServletRequest = request.buildRequest(servletContext);
        CsrfToken csrfToken = csrfTokenRepository.generateToken(httpServletRequest);
        csrfTokenRepository.saveToken(csrfToken, httpServletRequest, null);
        headers.add(csrfToken.getHeaderName(), csrfToken.getToken());
        VerifyAuthenticationResponseModel model = gson.fromJson(mockMvc.perform(request).andReturn().getResponse().getContentAsString(), VerifyAuthenticationResponseModel.class);
        assertTrue(model.isAuthenticated());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testVerifyNoToken() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-CSRF-TOKEN", UUID.randomUUID().toString());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(HOME_VERIFY_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        request.headers(headers);
        VerifyAuthenticationResponseModel model = gson.fromJson(mockMvc.perform(request).andReturn().getResponse().getContentAsString(), VerifyAuthenticationResponseModel.class);
        assertFalse(model.isAuthenticated());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testVerifyMissingCSRFToken() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(HOME_VERIFY_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        VerifyAuthenticationResponseModel model = gson.fromJson(mockMvc.perform(request).andReturn().getResponse().getContentAsString(), VerifyAuthenticationResponseModel.class);
        assertFalse(model.isAuthenticated());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testVerifyNullStringCSRFToken() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(HOME_VERIFY_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-CSRF-TOKEN", "null");
        request.headers(headers);
        VerifyAuthenticationResponseModel model = gson.fromJson(mockMvc.perform(request).andReturn().getResponse().getContentAsString(), VerifyAuthenticationResponseModel.class);
        assertFalse(model.isAuthenticated());
    }

    @Test
    public void testVerifySamlEnabled() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(HOME_SAML_VERIFY_URL);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testIndex() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(HOME_URL);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

}
