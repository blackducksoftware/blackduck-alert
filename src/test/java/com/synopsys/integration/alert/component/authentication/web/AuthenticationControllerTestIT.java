package com.synopsys.integration.alert.component.authentication.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import com.blackduck.integration.alert.component.authentication.web.AuthenticationActions;
import com.blackduck.integration.alert.component.authentication.web.AuthenticationController;
import com.blackduck.integration.alert.component.authentication.web.AuthenticationResponseModel;
import com.google.gson.Gson;
import com.synopsys.integration.alert.authentication.ldap.action.LDAPManager;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.mock.model.MockLoginRestModel;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Transactional
@AlertIntegrationTest
class AuthenticationControllerTestIT {
    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    private final String loginUrl = AlertRestConstants.BASE_PATH + "/login";
    private final String logoutUrl = AlertRestConstants.BASE_PATH + "/logout";
    @Autowired
    protected WebApplicationContext webApplicationContext;
    @Autowired
    protected AlertProperties alertProperties;
    @Autowired
    protected LDAPManager ldapManager;
    @Autowired
    protected AuthorizationManager authorizationManager;
    @Autowired
    protected CsrfTokenRepository csrfTokenRepository;
    private MockMvc mockMvc;
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testLogout() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(logoutUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN));
        mockMvc.perform(request).andExpect(ResultMatcher.matchAll(
            MockMvcResultMatchers.redirectedUrl("/"),
            MockMvcResultMatchers.status().isNoContent()
        ));
    }

    @Test
    void testLogin() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(loginUrl);
        TestProperties testProperties = new TestProperties();
        MockLoginRestModel mockLoginRestModel = new MockLoginRestModel();
        mockLoginRestModel.setAlertUsername(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_USERNAME));
        mockLoginRestModel.setAlertPassword(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PASSWORD));

        ReflectionTestUtils.setField(alertProperties, "alertTrustCertificate", Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TRUST_HTTPS_CERT)));
        String restModel = mockLoginRestModel.getRestModelJson();
        request.content(restModel);
        request.contentType(contentType);
        ResultActions resultActions = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
        AuthenticationResponseModel authenticationResponseModel = gson.fromJson(resultActions.andReturn().getResponse().getContentAsString(), AuthenticationResponseModel.class);
        assertEquals(HttpStatus.OK.value(), authenticationResponseModel.getStatusCode());
        assertTrue(StringUtils.isBlank(authenticationResponseModel.getMessage()));
    }

    @Test
    void testLoginFailure() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(loginUrl);
        TestProperties testProperties = new TestProperties();
        MockLoginRestModel mockLoginRestModel = new MockLoginRestModel();
        mockLoginRestModel.setAlertUsername(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_USERNAME));
        mockLoginRestModel.setAlertPassword("badPassword");

        ReflectionTestUtils.setField(
            alertProperties,
            "alertTrustCertificate",
            Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TRUST_HTTPS_CERT))
        );
        String restModel = mockLoginRestModel.getRestModelJson();
        request.content(restModel);
        request.contentType(contentType);
        ResultActions resultActions = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
        AuthenticationResponseModel authenticationResponseModel = gson.fromJson(resultActions.andReturn().getResponse().getContentAsString(), AuthenticationResponseModel.class);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), authenticationResponseModel.getStatusCode());
        assertEquals(AuthenticationActions.ERROR_LOGIN_ATTEMPT_FAILED, authenticationResponseModel.getMessage());
    }

    @Test
    void testLoginLockoutFailure() throws Exception {
        TestProperties testProperties = new TestProperties();
        MockLoginRestModel invalidLoginRestModel = new MockLoginRestModel();
        invalidLoginRestModel.setAlertUsername(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_USERNAME));
        invalidLoginRestModel.setAlertPassword("badPassword");
        ReflectionTestUtils.setField(
            alertProperties,
            "alertTrustCertificate",
            Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TRUST_HTTPS_CERT))
        );
        for (int attempt = 0; attempt < 10; attempt++) {
            MockHttpServletRequestBuilder invalidLoginRequest = MockMvcRequestBuilders.post(loginUrl);
            String restModel = invalidLoginRestModel.getRestModelJson();
            invalidLoginRequest.content(restModel);
            invalidLoginRequest.contentType(contentType);
            mockMvc.perform(invalidLoginRequest).andExpect(MockMvcResultMatchers.status().isOk());
        }
        MockHttpServletRequestBuilder validLoginRequest = MockMvcRequestBuilders.post(loginUrl);
        MockLoginRestModel mockLoginRestModel = new MockLoginRestModel();
        mockLoginRestModel.setAlertUsername(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_USERNAME));
        mockLoginRestModel.setAlertPassword(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PASSWORD));
        String validRestModel = invalidLoginRestModel.getRestModelJson();
        validLoginRequest.content(validRestModel);
        validLoginRequest.contentType(contentType);
        ResultActions resultActions = mockMvc.perform(validLoginRequest).andExpect(MockMvcResultMatchers.status().isOk());
        AuthenticationResponseModel authenticationResponseModel = gson.fromJson(resultActions.andReturn().getResponse().getContentAsString(), AuthenticationResponseModel.class);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), authenticationResponseModel.getStatusCode());
        assertEquals(AuthenticationActions.ERROR_ACCOUNT_TEMPORARILY_LOCKED, authenticationResponseModel.getMessage());
    }

    @Test
    void userLoginWithValidSessionTest() {
        AuthenticationActions authenticationActions = Mockito.mock(AuthenticationActions.class);
        Mockito.when(authenticationActions.authenticateUser(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class), Mockito.any()))
            .thenReturn(new ActionResponse<>(HttpStatus.OK, new AuthenticationResponseModel(HttpStatus.OK.value(), "")));
        AuthenticationController loginController = new AuthenticationController(authenticationActions);

        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpSession session = servletRequest.getSession(true);
        session.setMaxInactiveInterval(30);
        HttpServletResponse servletResponse = new MockHttpServletResponse();

        try {
            AuthenticationResponseModel responseModel = loginController.login(servletRequest, servletResponse, null);
            assertEquals(HttpStatus.OK.value(), responseModel.getStatusCode());
            assertTrue(StringUtils.isBlank(responseModel.getMessage()));
        } catch (ResponseStatusException e) {
            fail("Expect an OK response, but a ResponseStatusException was thrown: " + e.getMessage());
        }
    }

    @Test
    void userLoginWithInvalidSessionTest() {
        AuthenticationActions authenticationActions = Mockito.mock(AuthenticationActions.class);
        Mockito.when(authenticationActions.authenticateUser(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class), Mockito.any()))
            .thenReturn(new ActionResponse<>(HttpStatus.UNAUTHORIZED));
        AuthenticationController loginController = new AuthenticationController(authenticationActions);

        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();

        try {
            loginController.login(servletRequest, servletResponse, null);
        } catch (ResponseStatusException ex) {
            assertEquals(HttpStatus.UNAUTHORIZED, HttpStatus.resolve(ex.getStatusCode().value()));
        }
    }

}
