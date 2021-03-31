package com.synopsys.integration.alert.web.api.functions.upload;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.synopsys.integration.alert.component.authentication.web.SAMLMetadataUploadFunctionController;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;

@AlertIntegrationTest
public class SAMLMetadataUploadFunctionControllerTestIT {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @BeforeEach
    public void initialize() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    public void testControllerMethods() throws Exception {
        uploadFile();
        exists();
        deleteFile();
    }

    public void uploadFile() throws Exception {
        String url = SAMLMetadataUploadFunctionController.SAML_UPLOAD_URL;
        ClassPathResource classPathResource = new ClassPathResource("saml/testMetadata.xml");
        File jsonFile = classPathResource.getFile();
        String xmlContent = FileUtils.readFileToString(jsonFile, Charset.defaultCharset());
        MockMultipartFile file = new MockMultipartFile("file", "testMetadata.xml", "text/xml", xmlContent.getBytes());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart(new URI(url))
                                                    .file(file)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    public void exists() throws Exception {
        String url = SAMLMetadataUploadFunctionController.SAML_UPLOAD_URL + "/exists";
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    public void deleteFile() throws Exception {
        String url = SAMLMetadataUploadFunctionController.SAML_UPLOAD_URL;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

}
