package com.synopsys.integration.alert.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import com.synopsys.integration.alert.common.action.UploadEndpointManager;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.api.upload.UploadEndpointController;

public class UploadEndpointControllerTestIT extends AlertIntegrationTest {
    private static final SettingsDescriptorKey SETTINGS_DESCRIPTOR_KEY = new SettingsDescriptorKey();

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private UploadEndpointManager endpointManager;
    private MockMvc mockMvc;

    @BeforeEach
    public void initialize() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    public void testUploadForEmptyKey() {
        UploadEndpointController controller = new UploadEndpointController(endpointManager);
        callEndpointAndAssertBadRequest(() -> controller.postFileUpload("", null));
    }

    @Test
    public void testExistsForEmptyKey() {
        UploadEndpointController controller = new UploadEndpointController(endpointManager);
        callEndpointAndAssertBadRequest(() -> controller.checkUploadedFileExists(""));
    }

    @Test
    public void testDeleteForEmptyKey() {
        UploadEndpointController controller = new UploadEndpointController(endpointManager);
        callEndpointAndAssertBadRequest(() -> controller.deleteUploadedFile(""));
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
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart(new URI(url))
                                                    .file(file)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    public void exists(String targetKey) throws Exception {
        String url = UploadEndpointManager.UPLOAD_ENDPOINT_URL + "/" + targetKey + "/exists";
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    public void deleteFile(String targetKey) throws Exception {
        String url = UploadEndpointManager.UPLOAD_ENDPOINT_URL + "/" + targetKey;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    private void callEndpointAndAssertBadRequest(Runnable endpointFunction) {
        try {
            endpointFunction.run();
            fail("Expected a ResponseStatusException to be thrown");
        } catch (ResponseStatusException responseStatusException) {
            assertEquals(HttpStatus.BAD_REQUEST, responseStatusException.getStatus());
        }
    }

}
