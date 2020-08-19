package com.synopsys.integration.alert.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.mock.model.MockLoginRestModel;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.web.api.authentication.AuthenticationController;
import com.synopsys.integration.alert.web.api.authentication.LoginActions;
import com.synopsys.integration.alert.web.api.authentication.PasswordResetService;
import com.synopsys.integration.alert.web.common.BaseController;
import com.synopsys.integration.alert.web.security.authentication.ldap.LdapManager;

public class AuthenticationControllerTestIT extends AlertIntegrationTest {
    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    private final String loginUrl = BaseController.BASE_PATH + "/login";
    private final String logoutUrl = BaseController.BASE_PATH + "/logout";
    private final HttpSessionCsrfTokenRepository csrfTokenRepository = new HttpSessionCsrfTokenRepository();
    @Autowired
    protected WebApplicationContext webApplicationContext;
    @Autowired
    protected AlertProperties alertProperties;
    @Autowired
    protected LdapManager ldapManager;
    @Autowired
    protected AuthorizationManager authorizationManager;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testLogout() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(logoutUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN));
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testLogin() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(loginUrl);
        TestProperties testProperties = new TestProperties();
        MockLoginRestModel mockLoginRestModel = new MockLoginRestModel();
        mockLoginRestModel.setBlackDuckUsername(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_USERNAME));
        mockLoginRestModel.setBlackDuckPassword(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PASSWORD));

        ReflectionTestUtils.setField(alertProperties, "alertTrustCertificate", Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TRUST_HTTPS_CERT)));
        String restModel = mockLoginRestModel.getRestModelJson();
        request.content(restModel);
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void userLogoutWithValidSessionTest() {
        ResponseFactory responseFactory = new ResponseFactory();
        AuthenticationController loginHandler = new AuthenticationController(null, null, responseFactory, csrfTokenRepository);
        HttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30);

        ResponseEntity<String> response = loginHandler.logout(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void userLogoutWithInvalidSessionTest() {
        ResponseFactory responseFactory = new ResponseFactory();
        AuthenticationController loginHandler = new AuthenticationController(null, null, responseFactory, csrfTokenRepository);
        HttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<String> response = loginHandler.logout(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void userLoginWithValidSessionTest() {
        LoginActions loginActions = Mockito.mock(LoginActions.class);
        ResponseFactory responseFactory = new ResponseFactory();
        AuthenticationController loginHandler = new AuthenticationController(loginActions, null, responseFactory, csrfTokenRepository);

        HttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30);
        HttpServletResponse httpResponse = new MockHttpServletResponse();
        Mockito.when(loginActions.authenticateUser(Mockito.any())).thenReturn(true);

        ResponseEntity<String> response = loginHandler.login(request, httpResponse, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void userLoginWithInvalidSessionTest() {
        LoginActions loginActions = Mockito.mock(LoginActions.class);
        ResponseFactory responseFactory = new ResponseFactory();
        AuthenticationController loginHandler = new AuthenticationController(loginActions, null, responseFactory, csrfTokenRepository);

        HttpServletRequest request = new MockHttpServletRequest();
        Mockito.when(loginActions.authenticateUser(Mockito.any())).thenReturn(false);
        HttpServletResponse httpResponse = new MockHttpServletResponse();

        ResponseEntity<String> response = loginHandler.login(request, httpResponse, null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void userLoginWithBadCredentialsTest() {
        LoginActions loginActions = Mockito.mock(LoginActions.class);
        ResponseFactory responseFactory = new ResponseFactory();
        AuthenticationController loginHandler = new AuthenticationController(loginActions, null, responseFactory, csrfTokenRepository);

        HttpServletRequest request = new MockHttpServletRequest();
        Mockito.when(loginActions.authenticateUser(Mockito.any())).thenThrow(new BadCredentialsException("Bad credentials test"));
        HttpServletResponse httpResponse = new MockHttpServletResponse();

        ResponseEntity<String> response = loginHandler.login(request, httpResponse, null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void userLoginWithExceptionTest() {
        LoginActions loginActions = Mockito.mock(LoginActions.class);
        ResponseFactory responseFactory = new ResponseFactory();
        AuthenticationController loginHandler = new AuthenticationController(loginActions, null, responseFactory, csrfTokenRepository);

        HttpServletRequest request = new MockHttpServletRequest();
        Mockito.when(loginActions.authenticateUser(Mockito.any())).thenThrow(new IllegalArgumentException("Test exception for catch all"));
        HttpServletResponse httpResponse = new MockHttpServletResponse();

        ResponseEntity<String> response = loginHandler.login(request, httpResponse, null);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void resetPasswordBlankTest() {
        ResponseFactory responseFactory = new ResponseFactory();
        AuthenticationController loginHandler = new AuthenticationController(null, null, responseFactory, csrfTokenRepository);

        ResponseEntity<String> response = loginHandler.resetPassword();
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void resetPasswordValidTest() throws AlertException {
        PasswordResetService passwordResetService = Mockito.mock(PasswordResetService.class);
        Mockito.doNothing().when(passwordResetService).resetPassword(Mockito.anyString());

        ResponseFactory responseFactory = new ResponseFactory();
        AuthenticationController loginHandler = new AuthenticationController(null, passwordResetService, responseFactory, csrfTokenRepository);

        ResponseEntity<String> response = loginHandler.resetPassword("exampleUsername");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void resetPasswordDatabaseExceptionTest() throws AlertException {
        PasswordResetService passwordResetService = Mockito.mock(PasswordResetService.class);
        Mockito.doThrow(new AlertDatabaseConstraintException("Test Exception")).when(passwordResetService).resetPassword(Mockito.anyString());

        ResponseFactory responseFactory = new ResponseFactory();
        AuthenticationController loginHandler = new AuthenticationController(null, passwordResetService, responseFactory, csrfTokenRepository);

        ResponseEntity<String> response = loginHandler.resetPassword("exampleUsername");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void resetPasswordAlertExceptionTest() throws AlertException {
        PasswordResetService passwordResetService = Mockito.mock(PasswordResetService.class);
        Mockito.doThrow(new AlertException("Test Exception")).when(passwordResetService).resetPassword(Mockito.anyString());

        ResponseFactory responseFactory = new ResponseFactory();
        AuthenticationController loginHandler = new AuthenticationController(null, passwordResetService, responseFactory, csrfTokenRepository);

        ResponseEntity<String> response = loginHandler.resetPassword("exampleUsername");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
