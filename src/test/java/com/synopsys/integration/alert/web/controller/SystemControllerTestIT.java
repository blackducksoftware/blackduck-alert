package com.synopsys.integration.alert.web.controller;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.component.settings.SettingsDescriptor;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.web.actions.SystemActions;
import com.synopsys.integration.alert.web.model.SystemSetupModel;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;

public class SystemControllerTestIT extends AlertIntegrationTest {
    protected final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    private final String systemMessageBaseUrl = BaseController.BASE_PATH + "/system/messages";
    private final String systemInitialSetupBaseUrl = BaseController.BASE_PATH + "/system/setup/initial";
    private final Gson gson = new Gson();
    private final ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private SystemStatusUtility systemStatusUtility;
    private MockMvc mockMvc;
    private SystemActions systemActions;

    @BeforeEach
    public void initialize() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
        systemActions = Mockito.mock(SystemActions.class);
    }

    @Test
    public void testGetLatestMessages() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(systemMessageBaseUrl + "/latest")
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetMessages() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(systemMessageBaseUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetInitialSystemSetup() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(systemInitialSetupBaseUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        if (systemStatusUtility.isSystemInitialized()) {
            // the spring-populateFieldModel.properties file sets the encryption and in order to run a hub URL is needed therefore the environment is setup.
            mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isFound());
        } else {
            mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
        }
    }

    @Test
    public void testPostInitialSystemSetup() throws Exception {
        final TestProperties testProperties = new TestProperties();
        final String defaultAdminPassword = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PASSWORD);
        final boolean defaultAdminPasswordSet = false;
        final String blackDuckProviderUrl = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL);
        final Integer blackDuckConnectionTimeout = 300;
        final String blackDuckApiToken = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY);
        final boolean blackDuckApiTokenSet = false;
        final String globalEncryptionPassword = "password";
        final boolean isGlobalEncryptionPasswordSet = false;
        final String globalEncryptionSalt = "salt";
        final boolean isGlobalEncryptionSaltSet = false;
        final String proxyHost = "";
        final String proxyPort = "";
        final String proxyUsername = "";
        final String proxyPassword = "";
        final boolean proxyPasswordSet = false;

        final SystemSetupModel configuration = SystemSetupModel.of(defaultAdminPassword, defaultAdminPasswordSet, blackDuckProviderUrl, blackDuckConnectionTimeout, blackDuckApiToken, blackDuckApiTokenSet,
            globalEncryptionPassword, isGlobalEncryptionPasswordSet, globalEncryptionSalt, isGlobalEncryptionSaltSet,
            proxyHost, proxyPort, proxyUsername, proxyPassword, proxyPasswordSet);

        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(systemInitialSetupBaseUrl)
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        request.content(gson.toJson(configuration));
        request.contentType(contentType);
        if (systemStatusUtility.isSystemInitialized()) {
            // the spring-populateFieldModel.properties file sets the encryption and in order to run a hub URL is needed therefore the environment is setup.
            mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isConflict());
        } else {
            mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
        }
    }

    @Test
    public void testGetLatestMessagesHandling() {
        ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);
        final ResponseEntity<String> responseEntity = handler.getLatestSystemMessages();
        Mockito.verify(systemActions).getSystemMessagesSinceStartup();
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesgetAll() {
        ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);
        final ResponseEntity<String> responseEntity = handler.getSystemMessages("", "");
        Mockito.verify(systemActions).getSystemMessages();
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesGetAfter() throws Exception {
        ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);
        final ResponseEntity<String> responseEntity = handler.getSystemMessages("2018-11-13T00:00:00.000Z", null);
        Mockito.verify(systemActions).getSystemMessagesAfter(Mockito.anyString());
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesGetBefore() throws Exception {
        ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);
        final ResponseEntity<String> responseEntity = handler.getSystemMessages(null, "2018-11-13T00:00:00.000Z");
        Mockito.verify(systemActions).getSystemMessagesBefore(Mockito.anyString());
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesGetBetween() throws Exception {
        ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);
        final ResponseEntity<String> responseEntity = handler.getSystemMessages("2018-11-13T00:00:00.000Z", "2018-11-13T01:00:00.000Z");
        Mockito.verify(systemActions).getSystemMessagesBetween(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesBadDateRange() throws Exception {
        ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);
        Mockito.when(systemActions.getSystemMessagesBetween(Mockito.anyString(), Mockito.anyString())).thenThrow(new ParseException("errorparsing date ", 0));
        final ResponseEntity<String> responseEntity = handler.getSystemMessages("bad-start-time", "bad-end-time");
        Mockito.verify(systemActions).getSystemMessagesBetween(Mockito.anyString(), Mockito.anyString());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testGetCurrentSetup() {
        ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);

        final String contextPath = "context-path/";
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        Mockito.when(request.getServletContext()).thenReturn(servletContext);
        Mockito.when(servletContext.getContextPath()).thenReturn(contextPath);
        final ResponseEntity<String> response = handler.getInitialSystemSetup(request);
        Mockito.verify(systemActions).isSystemInitialized();
        Mockito.verify(systemActions).getCurrentSystemSetup();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        final String body = response.getBody();
        assertNull(body);
    }

    @Test
    public void testGetCurrentSetupInitialized() {
        ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);
        Mockito.when(systemActions.isSystemInitialized()).thenReturn(Boolean.TRUE);
        final String contextPath = "context-path/";
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        Mockito.when(request.getServletContext()).thenReturn(servletContext);
        Mockito.when(servletContext.getContextPath()).thenReturn(contextPath);
        final ResponseEntity<String> response = handler.getInitialSystemSetup(request);
        Mockito.verify(systemActions).isSystemInitialized();

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertNull(response.getBody());
        assertEquals(contextPath, response.getHeaders().getFirst("Location"));
    }

    @Test
    public void testSaveNotAllowed() {
        final FieldModel model = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, "GLOBAL", Map.of());
        Mockito.when(systemActions.isSystemInitialized()).thenReturn(Boolean.TRUE);
        ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);
        final ResponseEntity<String> response = handler.initialSystemSetup(model);
        Mockito.verify(systemActions).isSystemInitialized();

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testSaveWithErrors() {
        final FieldModel model = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, "GLOBAL", Map.of());
        Mockito.when(systemActions.isSystemInitialized()).thenReturn(Boolean.FALSE);
        Mockito.doAnswer(invocation -> {
            final Map<String, String> fieldErrors = invocation.getArgument(1);
            fieldErrors.put("propertyKey", "error");
            return invocation.getArgument(0);
        }).when(systemActions).saveRequiredInformation(Mockito.any(FieldModel.class), Mockito.anyMap());
        ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);
        final ResponseEntity<String> response = handler.initialSystemSetup(model);
        Mockito.verify(systemActions).isSystemInitialized();
        Mockito.verify(systemActions).saveRequiredInformation(Mockito.any(FieldModel.class), Mockito.anyMap());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testSave() {
        final FieldModel model = new FieldModel(SettingsDescriptor.SETTINGS_COMPONENT, "GLOBAL", Map.of());
        Mockito.when(systemActions.isSystemInitialized()).thenReturn(Boolean.FALSE);
        ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);
        final ResponseEntity<String> response = handler.initialSystemSetup(model);
        Mockito.verify(systemActions).isSystemInitialized();
        Mockito.verify(systemActions).saveRequiredInformation(Mockito.any(FieldModel.class), Mockito.anyMap());

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
