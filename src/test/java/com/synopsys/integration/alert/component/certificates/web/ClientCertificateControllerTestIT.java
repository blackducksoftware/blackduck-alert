package com.synopsys.integration.alert.component.certificates.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.persistence.model.ClientCertificateModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.database.job.api.ClientCertificateAccessor;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;

@AlertIntegrationTest
class ClientCertificateControllerTestIT {
    @Autowired
    private AlertProperties alertProperties;
    @Autowired
    private ClientCertificateAccessor accessor;
    @Autowired
    private Gson gson;
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private final CertificateTestUtil certTestUtil = new CertificateTestUtil();
    private final MediaType MEDIA_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8);
    private final String REQUEST_URL = AlertRestConstants.CLIENT_CERTIFICATE_PATH;

    @BeforeEach
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
        certTestUtil.init(alertProperties);
    }

    @AfterEach
    public void cleanup() throws IOException {
        accessor.deleteConfiguration();
        certTestUtil.cleanup();
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void getOne() throws Exception {
        accessor.createConfiguration(createClientCertificateModel());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void createConfig() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        ClientCertificateModel model = createClientCertificateModel();

        request.content(gson.toJson(model));
        request.contentType(MEDIA_TYPE);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void createExistingConfig() throws Exception {
        accessor.createConfiguration(createClientCertificateModel());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        ClientCertificateModel model = createClientCertificateModel();

        request.content(gson.toJson(model));
        request.contentType(MEDIA_TYPE);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void createConfigEmptyKeyPassword() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        String keyContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.KEY_MTLS_CLIENT_FILE_PATH);
        String certificateContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.CERTIFICATE_MTLS_CLIENT_FILE_PATH);

        request.content(gson.toJson(new ClientCertificateModel(null, keyContent, certificateContent)));
        request.contentType(MEDIA_TYPE);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());

        request.content(gson.toJson(new ClientCertificateModel("", keyContent, certificateContent)));
        request.contentType(MEDIA_TYPE);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void createConfigEmptyKeyContent() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        String certificateContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.CERTIFICATE_MTLS_CLIENT_FILE_PATH);

        request.content(gson.toJson(new ClientCertificateModel(CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD, null, certificateContent)));
        request.contentType(MEDIA_TYPE);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());

        request.content(gson.toJson(new ClientCertificateModel(CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD, "", certificateContent)));
        request.contentType(MEDIA_TYPE);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void createConfigEmptyCertificateContent() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        String keyContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.KEY_MTLS_CLIENT_FILE_PATH);

        request.content(gson.toJson(new ClientCertificateModel(CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD, keyContent, null)));
        request.contentType(MEDIA_TYPE);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());

        request.content(gson.toJson(new ClientCertificateModel(CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD, keyContent, "")));
        request.contentType(MEDIA_TYPE);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void createConfigEmptyAllFields() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        request.content(gson.toJson(new ClientCertificateModel(null, null, null)));
        request.contentType(MEDIA_TYPE);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());

        request.content(gson.toJson(new ClientCertificateModel("", "", "")));
        request.contentType(MEDIA_TYPE);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void createConfigThrows() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        ClientCertificateModel model = new ClientCertificateModel("bad_key_password", "bad_key_content", "bad_certificate_content");

        request.content(gson.toJson(model));
        request.contentType(MEDIA_TYPE);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void updateNotAllowed() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        ClientCertificateModel model = createClientCertificateModel();

        request.content(gson.toJson(model));
        request.contentType(MEDIA_TYPE);

        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void deleteConfig() throws Exception {
        accessor.createConfiguration(createClientCertificateModel());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void deleteEmptyConfig() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    private ClientCertificateModel createClientCertificateModel() throws IOException {
        String keyContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.KEY_MTLS_CLIENT_FILE_PATH);
        String certificateContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.CERTIFICATE_MTLS_CLIENT_FILE_PATH);

        return new ClientCertificateModel(
            CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD,
            keyContent,
            certificateContent
        );
    }
}
