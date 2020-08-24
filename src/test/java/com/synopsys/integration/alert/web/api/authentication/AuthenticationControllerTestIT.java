package com.synopsys.integration.alert.web.api.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.server.ResponseStatusException;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.mock.model.MockLoginRestModel;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
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
        mockLoginRestModel.setAlertUsername(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_USERNAME));
        mockLoginRestModel.setAlertPassword(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PASSWORD));

        ReflectionTestUtils.setField(alertProperties, "alertTrustCertificate", Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TRUST_HTTPS_CERT)));
        String restModel = mockLoginRestModel.getRestModelJson();
        request.content(restModel);
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void userLogoutWithValidSessionTest() {
        AuthenticationController loginHandler = new AuthenticationController(null, null, csrfTokenRepository);
        HttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30);

        ResponseEntity<Void> response = loginHandler.logout(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void userLogoutWithInvalidSessionTest() {
        AuthenticationController loginHandler = new AuthenticationController(null, null, csrfTokenRepository);
        HttpServletRequest request = new MockHttpServletRequest();

        ResponseEntity<Void> response = loginHandler.logout(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void userLoginWithValidSessionTest() {
        LoginActions loginActions = Mockito.mock(LoginActions.class);
        AuthenticationController loginHandler = new AuthenticationController(loginActions, null, csrfTokenRepository);

        HttpServletRequest request = new MockHttpServletRequest();
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30);
        HttpServletResponse httpResponse = new MockHttpServletResponse();
        Mockito.when(loginActions.authenticateUser(Mockito.any())).thenReturn(true);

        try {
            loginHandler.login(request, httpResponse, null);
        } catch (ResponseStatusException e) {
            fail("Expect an OK response, but a ResponseStatusException was thrown: " + e.getMessage());
        }
    }

    @Test
    public void userLoginWithInvalidSessionTest() {
        LoginActions loginActions = Mockito.mock(LoginActions.class);
        AuthenticationController loginHandler = new AuthenticationController(loginActions, null, csrfTokenRepository);

        HttpServletRequest request = new MockHttpServletRequest();
        Mockito.when(loginActions.authenticateUser(Mockito.any())).thenReturn(false);
        HttpServletResponse httpResponse = new MockHttpServletResponse();

        assertErrorStatus(HttpStatus.UNAUTHORIZED, () -> loginHandler.login(request, httpResponse, null));
    }

    @Test
    public void userLoginWithBadCredentialsTest() {
        LoginActions loginActions = Mockito.mock(LoginActions.class);
        AuthenticationController loginHandler = new AuthenticationController(loginActions, null, csrfTokenRepository);

        HttpServletRequest request = new MockHttpServletRequest();
        Mockito.when(loginActions.authenticateUser(Mockito.any())).thenThrow(new BadCredentialsException("Bad credentials test"));
        HttpServletResponse httpResponse = new MockHttpServletResponse();

        assertErrorStatus(HttpStatus.UNAUTHORIZED, () -> loginHandler.login(request, httpResponse, null));
    }

    @Test
    public void resetPasswordNullTest() {
        AuthenticationController loginHandler = new AuthenticationController(null, null, csrfTokenRepository);
        assertErrorStatus(HttpStatus.BAD_REQUEST, () -> loginHandler.resetPassword(null));
    }

    @Test
    public void resetPasswordBlankTest() {
        AuthenticationController loginHandler = new AuthenticationController(null, null, csrfTokenRepository);
        assertErrorStatus(HttpStatus.BAD_REQUEST, () -> loginHandler.resetPassword(StringUtils.EMPTY));
    }

    @Test
    public void resetPasswordValidTest() throws AlertException {
        PasswordResetService passwordResetService = Mockito.mock(PasswordResetService.class);
        Mockito.doNothing().when(passwordResetService).resetPassword(Mockito.anyString());

        AuthenticationController loginHandler = new AuthenticationController(null, passwordResetService, csrfTokenRepository);

        try {
            loginHandler.resetPassword("exampleUsername");
        } catch (ResponseStatusException e) {
            fail("Expect an OK response, but a ResponseStatusException was thrown: " + e.getMessage());
        }
    }

    @Test
    public void resetPasswordAlertExceptionTest() throws AlertException {
        PasswordResetService passwordResetService = Mockito.mock(PasswordResetService.class);
        Mockito.doThrow(new AlertException("Test Exception")).when(passwordResetService).resetPassword(Mockito.anyString());

        AuthenticationController loginHandler = new AuthenticationController(null, passwordResetService, csrfTokenRepository);

        assertErrorStatus(HttpStatus.BAD_REQUEST, () -> loginHandler.resetPassword("exampleUsername"));
    }

    private void assertErrorStatus(HttpStatus errorStatus, Runnable requestMethod) {
        try {
            requestMethod.run();
        } catch (ResponseStatusException e) {
            assertEquals(errorStatus, e.getStatus());
        }
    }

}
