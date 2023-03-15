package com.synopsys.integration.alert.authentication.saml.web;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

@AlertIntegrationTest
class SAMLConfigControllerTestIT {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private SAMLConfigAccessor samlConfigAccessor;
    @Autowired
    private FilePersistenceUtil filePersistenceUtil;

    @Autowired
    private Gson gson;

    private MockMvc mockMvc;

    private final MediaType MEDIA_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8);
    private final String REQUEST_URL = AlertRestConstants.SAML_PATH;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @BeforeEach
    @AfterEach
    public void cleanup() throws IOException {
        samlConfigAccessor.deleteConfiguration();

        deleteSAMLFiles();
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void createEndpointReturnsCreated() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        SAMLConfigModel configModel = createSAMLUrlConfigModel(null);
        request.content(gson.toJson(configModel));
        request.contentType(MEDIA_TYPE);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void getOneEndpointReturnsOk() throws Exception {
        saveConfigModel(createSAMLUrlConfigModel(UUID.randomUUID()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void updateEndpointReturnsNoContent() throws Exception {
        SAMLConfigModel savedConfigModel = saveConfigModel(createSAMLUrlConfigModel(UUID.randomUUID()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        request.content(gson.toJson(savedConfigModel));
        request.contentType(MEDIA_TYPE);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void deleteEndpointReturnsNoContent() throws Exception {
        saveConfigModel(createSAMLUrlConfigModel(UUID.randomUUID()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(REQUEST_URL)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void validateEndpointReturnsOk() throws Exception {
        String urlPath = REQUEST_URL + "/validate";
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());

        UUID uuid = UUID.randomUUID();
        SAMLConfigModel configModel = createSAMLUrlConfigModel(uuid);
        SAMLConfigModel savedConfigModel = saveConfigModel(configModel);
        request.content(gson.toJson(savedConfigModel));
        request.contentType(MEDIA_TYPE);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void checkFileExists() throws Exception {
        filePersistenceUtil.writeFileToUploadsDirectory(AuthenticationDescriptor.SAML_METADATA_FILE, new ByteArrayInputStream("data".getBytes()));

        String urlPath = REQUEST_URL + SAMLConfigController.METADATA_FILE_UPLOAD_PATH;
        MockHttpServletRequestBuilder presentRequest = MockMvcRequestBuilders.get(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(presentRequest).andExpect(MockMvcResultMatchers.status().isNoContent());

        filePersistenceUtil.deleteUploadsFile(AuthenticationDescriptor.SAML_METADATA_FILE);

        MockHttpServletRequestBuilder missingRequest = MockMvcRequestBuilders.get(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(missingRequest).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void deleteFileReturnsNoContent() throws Exception {
        filePersistenceUtil.writeFileToUploadsDirectory(AuthenticationDescriptor.SAML_METADATA_FILE, new ByteArrayInputStream("data".getBytes()));

        String urlPath = REQUEST_URL + SAMLConfigController.METADATA_FILE_UPLOAD_PATH;
        MockHttpServletRequestBuilder presentRequest = MockMvcRequestBuilders.delete(urlPath)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(presentRequest).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void uploadFileReturnsNoContent() throws Exception {
        filePersistenceUtil.writeFileToUploadsDirectory(AuthenticationDescriptor.SAML_METADATA_FILE, new ByteArrayInputStream("data".getBytes()));

        String urlPath = REQUEST_URL + SAMLConfigController.METADATA_FILE_UPLOAD_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart(urlPath)
            .file(new MockMultipartFile("file", "filename.txt", "text/plain", "<note></note>".getBytes()))
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    private SAMLConfigModel createSAMLUrlConfigModel(UUID uuid) {
        SAMLConfigModel samlConfigModel = new SAMLConfigModel((null != uuid) ? uuid.toString() : null);
        samlConfigModel.setMetadataUrl("https://www.metadata.com");
        return samlConfigModel;
    }

    private SAMLConfigModel saveConfigModel(SAMLConfigModel samlConfigModel) throws AlertConfigurationException {
        return samlConfigAccessor.createConfiguration(samlConfigModel);
    }

    private void deleteSAMLFiles () throws IOException {
        Set<String> samlFiles = Set.of(
            AuthenticationDescriptor.SAML_METADATA_FILE,
            AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE,
            AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE,
            AuthenticationDescriptor.SAML_SIGNING_CERT_FILE,
            AuthenticationDescriptor.SAML_SIGNING_PRIVATE_KEY_FILE,
            AuthenticationDescriptor.SAML_VERIFICATION_CERT_FILE
        );

        for (String fileName: samlFiles) {
            if (filePersistenceUtil.uploadFileExists(fileName)) {
                File fileToValidate = filePersistenceUtil.createUploadsFile(fileName);
                filePersistenceUtil.delete(fileToValidate);
            }
        }
    }
}
