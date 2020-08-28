package com.synopsys.integration.alert.web.controller;

import java.nio.charset.Charset;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.api.system.SystemActions;
import com.synopsys.integration.alert.web.api.system.SystemController;
import com.synopsys.integration.alert.web.common.BaseController;

public class SystemControllerTestIT extends AlertIntegrationTest {
    private static final String SYSTEM_MESSAGE_BASE_URL = BaseController.BASE_PATH + "/system/messages";
    protected final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    private final String SYSTEM_INITIAL_SETUP_BASE_URL = BaseController.BASE_PATH + "/system/setup/initial";
    private final String SYSTEM_INITIAL_DESCRIPTOR = BaseController.BASE_PATH + "/system/setup/descriptor";
    private final Gson gson = new Gson();
    //private final ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
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
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(SYSTEM_MESSAGE_BASE_URL + "/latest")
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testGetMessages() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(SYSTEM_MESSAGE_BASE_URL)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testGetInitialSystemSetup() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(SYSTEM_INITIAL_SETUP_BASE_URL)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testPostInitialSystemSetup() throws Exception {
        HashMap<String, FieldValueModel> valueModelMap = new HashMap<>();
        FieldModel configuration = new FieldModel("a_key", ConfigContextEnum.GLOBAL.name(), valueModelMap);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(SYSTEM_INITIAL_SETUP_BASE_URL)
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        request.content(gson.toJson(configuration));
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testGetLatestMessagesHandling() {
        SystemController handler = new SystemController(systemActions);
        handler.getLatestSystemMessages();
        Mockito.verify(systemActions).getSystemMessagesSinceStartup();
    }

    @Test
    public void testGetSystemMessagesGetAll() {
        SystemController handler = new SystemController(systemActions);
        handler.getSystemMessages("", "");
        Mockito.verify(systemActions).getSystemMessages();
    }

    /*
    @Test
    public void testGetSystemMessagesGetAfter() throws Exception {
        ResponseFactory responseFactory = new ResponseFactory();
        SystemController handler = new SystemController(systemActions);
        ResponseEntity<String> responseEntity = handler.getSystemMessages("2018-11-13T00:00:00.000Z", null);
        Mockito.verify(systemActions).getSystemMessagesAfter(Mockito.anyString());
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesGetBefore() throws Exception {
        ResponseFactory responseFactory = new ResponseFactory();
        SystemController handler = new SystemController(systemActions);
        ResponseEntity<String> responseEntity = handler.getSystemMessages(null, "2018-11-13T00:00:00.000Z");
        Mockito.verify(systemActions).getSystemMessagesBefore(Mockito.anyString());
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetSystemMessagesGetBetween() throws Exception {
        ResponseFactory responseFactory = new ResponseFactory();
        SystemController handler = new SystemController(systemActions);
        ResponseEntity<String> responseEntity = handler.getSystemMessages("2018-11-13T00:00:00.000Z", "2018-11-13T01:00:00.000Z");
        Mockito.verify(systemActions).getSystemMessagesBetween(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(contentConverter).getJsonString(Mockito.any());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }



    @Test
    public void testGetSystemMessagesBadDateRange() throws Exception {
        ResponseFactory responseFactory = new ResponseFactory();
        SystemController handler = new SystemController(systemActions);
        Mockito.when(systemActions.getSystemMessagesBetween(Mockito.anyString(), Mockito.anyString())).thenThrow(new ParseException("error parsing date ", 0));
        ResponseEntity<String> responseEntity = handler.getSystemMessages("bad-start-time", "bad-end-time");
        Mockito.verify(systemActions).getSystemMessagesBetween(Mockito.anyString(), Mockito.anyString());
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }
    */
}
