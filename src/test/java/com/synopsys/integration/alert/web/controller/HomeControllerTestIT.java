package com.synopsys.integration.alert.web.controller;

import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

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

import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class HomeControllerTestIT extends AlertIntegrationTest {

    private final String homeVerifyUrl = BaseController.BASE_PATH + "/verify";
    private final String homeUrl = "/";

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
    @WithMockUser(roles = "ADMIN")
    public void testVerify() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        final MockHttpSession session = new MockHttpSession();
        final ServletContext servletContext = webApplicationContext.getServletContext();

        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(homeVerifyUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        request.session(session);
        final HttpServletRequest httpServletRequest = request.buildRequest(servletContext);
        final CsrfToken csrfToken = csrfTokenRepository.generateToken(httpServletRequest);
        csrfTokenRepository.saveToken(csrfToken, httpServletRequest, null);
        headers.add(csrfToken.getHeaderName(), csrfToken.getToken());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testVerifyNoToken() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("X-CSRF-TOKEN", UUID.randomUUID().toString());
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(homeVerifyUrl)
                                                              .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                              .with(SecurityMockMvcRequestPostProcessors.csrf());
        request.headers(headers);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testVerifyMissingCSRFToken() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(homeVerifyUrl)
                                                              .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                              .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testVerifyNullStringCSRFToken() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(homeVerifyUrl)
                                                              .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                              .with(SecurityMockMvcRequestPostProcessors.csrf());
        final HttpHeaders headers = new HttpHeaders();
        headers.add("X-CSRF-TOKEN", "null");
        request.headers(headers);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testIndex() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(homeUrl);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }
}
