package com.synopsys.integration.alert.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.synopsys.integration.alert.common.action.UploadEndpointManager;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class UploadEndpointControllerTestIT extends AlertIntegrationTest {
    private static final SettingsDescriptorKey SETTINGS_DESCRIPTOR_KEY = new SettingsDescriptorKey();

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private UploadEndpointManager endpointManager;
    private ResponseFactory responseFactory = new ResponseFactory();
    private MockMvc mockMvc;

    @BeforeEach
    public void initialize() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    public void testUploadForEmptyKey() throws Exception {
        UploadEndpointController controller = new UploadEndpointController(endpointManager, responseFactory);
        ResponseEntity<String> response = controller.postFileUpload("", null);
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testExistsForEmptyKey() throws Exception {
        UploadEndpointController controller = new UploadEndpointController(endpointManager, responseFactory);
        ResponseEntity<String> response = controller.checkUploadedFileExists("");
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testDeleteForEmptyKey() throws Exception {
        UploadEndpointController controller = new UploadEndpointController(endpointManager, responseFactory);
        ResponseEntity<String> response = controller.deleteUploadedFile("");
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testControllerMethods() throws Exception {
        String targetKey = "target.key";
        endpointManager.registerTarget(targetKey, ConfigContextEnum.GLOBAL, SETTINGS_DESCRIPTOR_KEY, "TEST_UPLOAD_FILE");
        uploadFile(targetKey);
        exists(targetKey);
        deleteFile(targetKey);
        endpointManager.unRegisterTarget(targetKey);
    }

    public void uploadFile(String targetKey) throws Exception {
        String url = UploadEndpointManager.UPLOAD_ENDPOINT_URL + "/" + targetKey;
        MockMultipartFile file = new MockMultipartFile("file", "testfile.txt", "text/plain", "test text".getBytes());
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart(new URI(url))
                                                          .file(file)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    public void exists(String targetKey) throws Exception {
        String url = UploadEndpointManager.UPLOAD_ENDPOINT_URL + "/" + targetKey + "/exists";
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    public void deleteFile(String targetKey) throws Exception {
        String url = UploadEndpointManager.UPLOAD_ENDPOINT_URL + "/" + targetKey;
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(new URI(url))
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }
}
