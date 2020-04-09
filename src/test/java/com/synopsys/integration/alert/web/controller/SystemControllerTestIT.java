package com.synopsys.integration.alert.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.HashMap;

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
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.actions.SystemActions;

public class SystemControllerTestIT extends AlertIntegrationTest {
    private static final String SYSTEM_MESSAGE_BASE_URL = BaseController.BASE_PATH + "/system/messages";
    protected final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    private final String SYSTEM_INITIAL_SETUP_BASE_URL = BaseController.BASE_PATH + "/system/setup/initial";
    private final String SYSTEM_INITIAL_DESCRIPTOR = BaseController.BASE_PATH + "/system/setup/descriptor";
    private final Gson gson = new Gson();
    private final ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private SystemActions systemActions;

    @BeforeEach
    public void initialize() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
        systemActions = Mockito.mock(SystemActions.class);
    }

    @Test
    public void testGetLatestMessages() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(SYSTEM_MESSAGE_BASE_URL + "/latest")
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testGetMessages() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(SYSTEM_MESSAGE_BASE_URL)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testGetInitialSystemSetup() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(SYSTEM_INITIAL_SETUP_BASE_URL)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testPostInitialSystemSetup() throws Exception {
        final HashMap<String, FieldValueModel> valueModelMap = new HashMap<>();
        final FieldModel configuration = new FieldModel("a_key", ConfigContextEnum.GLOBAL.name(), valueModelMap);

        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(SYSTEM_INITIAL_SETUP_BASE_URL)
                                                          .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                          .with(SecurityMockMvcRequestPostProcessors.csrf());
        request.content(gson.toJson(configuration));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testGetLatestMessagesHandling() {
        final ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);
        final ResponseEntity<String> responseEntity = handler.getLatestSystemMessages();
        Mockito.verify(systemActions).getSystemMessagesSinceStartup();
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesgetAll() {
        final ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);
        final ResponseEntity<String> responseEntity = handler.getSystemMessages("", "");
        Mockito.verify(systemActions).getSystemMessages();
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesGetAfter() throws Exception {
        final ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);
        final ResponseEntity<String> responseEntity = handler.getSystemMessages("2018-11-13T00:00:00.000Z", null);
        Mockito.verify(systemActions).getSystemMessagesAfter(Mockito.anyString());
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesGetBefore() throws Exception {
        final ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);
        final ResponseEntity<String> responseEntity = handler.getSystemMessages(null, "2018-11-13T00:00:00.000Z");
        Mockito.verify(systemActions).getSystemMessagesBefore(Mockito.anyString());
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesGetBetween() throws Exception {
        final ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);
        final ResponseEntity<String> responseEntity = handler.getSystemMessages("2018-11-13T00:00:00.000Z", "2018-11-13T01:00:00.000Z");
        Mockito.verify(systemActions).getSystemMessagesBetween(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesBadDateRange() throws Exception {
        final ResponseFactory responseFactory = new ResponseFactory();
        final SystemController handler = new SystemController(systemActions, contentConverter, responseFactory);
        Mockito.when(systemActions.getSystemMessagesBetween(Mockito.anyString(), Mockito.anyString())).thenThrow(new ParseException("error parsing date ", 0));
        final ResponseEntity<String> responseEntity = handler.getSystemMessages("bad-start-time", "bad-end-time");
        Mockito.verify(systemActions).getSystemMessagesBetween(Mockito.anyString(), Mockito.anyString());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }
}
