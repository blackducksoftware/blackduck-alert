package com.synopsys.integration.alert.web.certificates;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.model.CertificateModel;

@Transactional
public class CertificateControllerTestIT extends BaseCertificateTestIT {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private Gson gson;
    private MockMvc mockMvc;

    @Override
    @BeforeEach
    public void init() throws Exception {
        super.init();
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void readAllTest() throws Exception {
        String url = CertificatesController.API_BASE_URL;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void readSingleTest() throws Exception {
        CertificateModel expectedCertificate = createCertificate();

        String url = CertificatesController.API_BASE_URL + String.format("/%s", expectedCertificate.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void updateTest() throws Exception {
        CertificateModel expectedCertificate = createCertificate();
        CertificateModel updatedCertificate = new CertificateModel(expectedCertificate.getId(), "new-alias", expectedCertificate.getCertificateContent());
        String url = CertificatesController.API_BASE_URL + String.format("/%s", expectedCertificate.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                    .content(gson.toJson(updatedCertificate));
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void deleteTest() throws Exception {
        CertificateModel expectedCertificate = createCertificate();
        String url = CertificatesController.API_BASE_URL + String.format("/%s", expectedCertificate.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void readAllForbiddenTest() throws Exception {
        String url = CertificatesController.API_BASE_URL;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("badUser"))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void readSingleForbiddenTest() throws Exception {
        CertificateModel expectedCertificate = createCertificate();

        String url = CertificatesController.API_BASE_URL + String.format("/%s", expectedCertificate.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("badUser"))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void updateForbiddenTest() throws Exception {
        CertificateModel expectedCertificate = createCertificate();
        CertificateModel updatedCertificate = new CertificateModel(expectedCertificate.getId(), "new-alias", expectedCertificate.getCertificateContent());
        String url = CertificatesController.API_BASE_URL + String.format("/%s", expectedCertificate.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("badUser"))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                    .content(gson.toJson(updatedCertificate));
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void deleteForbiddenTest() throws Exception {
        CertificateModel expectedCertificate = createCertificate();
        String url = CertificatesController.API_BASE_URL + String.format("/%s", expectedCertificate.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("badUser"))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}
