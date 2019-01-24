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
import com.synopsys.integration.alert.mock.model.MockLoginRestModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.web.actions.LoginActions;
import com.synopsys.integration.alert.web.security.authentication.ldap.LdapManager;

public class LoginControllerTestIT extends AlertIntegrationTest {
    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    private final String loginUrl = BaseController.BASE_PATH + "/login";
    private final String logoutUrl = BaseController.BASE_PATH + "/logout";
    private final HttpSessionCsrfTokenRepository csrfTokenRepository = new HttpSessionCsrfTokenRepository();
    @Autowired
    protected WebApplicationContext webApplicationContext;
    @Autowired
    protected BlackDuckProperties blackDuckProperties;
    @Autowired
    protected AlertProperties alertProperties;
    @Autowired
    protected LdapManager ldapManager;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testLogout() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(logoutUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void testLogin() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(loginUrl);
        final TestProperties testProperties = new TestProperties();
        final MockLoginRestModel mockLoginRestModel = new MockLoginRestModel();
        mockLoginRestModel.setBlackDuckUsername(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_USERNAME));
        mockLoginRestModel.setBlackDuckPassword(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PASSWORD));

        ReflectionTestUtils.setField(alertProperties, "alertTrustCertificate", Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TRUST_HTTPS_CERT)));
        final String restModel = mockLoginRestModel.getRestModelJson();
        request.content(restModel);
        request.contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void userLogoutWithValidSessionTest() {
        ResponseFactory responseFactory = new ResponseFactory();
        final LoginController loginHandler = new LoginController(null, responseFactory, csrfTokenRepository);
        final HttpServletRequest request = new MockHttpServletRequest();
        final HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30);

        final ResponseEntity<String> response = loginHandler.logout(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void userLogoutWithInvalidSessionTest() {
        ResponseFactory responseFactory = new ResponseFactory();
        final LoginController loginHandler = new LoginController(null, responseFactory, csrfTokenRepository);
        final HttpServletRequest request = new MockHttpServletRequest();

        final ResponseEntity<String> response = loginHandler.logout(request);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void userLoginWithValidSessionTest() {
        final LoginActions loginActions = Mockito.mock(LoginActions.class);
        ResponseFactory responseFactory = new ResponseFactory();
        final LoginController loginHandler = new LoginController(loginActions, responseFactory, csrfTokenRepository);

        final HttpServletRequest request = new MockHttpServletRequest();
        final HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(30);
        final HttpServletResponse httpResponse = new MockHttpServletResponse();
        Mockito.when(loginActions.authenticateUser(Mockito.any())).thenReturn(true);

        final ResponseEntity<String> response = loginHandler.login(request, httpResponse, null);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void userLoginWithInvalidSessionTest() {
        final LoginActions loginActions = Mockito.mock(LoginActions.class);
        ResponseFactory responseFactory = new ResponseFactory();
        final LoginController loginHandler = new LoginController(loginActions, responseFactory, csrfTokenRepository);

        final HttpServletRequest request = new MockHttpServletRequest();
        Mockito.when(loginActions.authenticateUser(Mockito.any())).thenReturn(false);
        final HttpServletResponse httpResponse = new MockHttpServletResponse();

        final ResponseEntity<String> response = loginHandler.login(request, httpResponse, null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void userLoginWithBadCredentialsTest() {
        final LoginActions loginActions = Mockito.mock(LoginActions.class);
        ResponseFactory responseFactory = new ResponseFactory();
        final LoginController loginHandler = new LoginController(loginActions, responseFactory, csrfTokenRepository);

        final HttpServletRequest request = new MockHttpServletRequest();
        Mockito.when(loginActions.authenticateUser(Mockito.any())).thenThrow(new BadCredentialsException("Bad credentials test"));
        final HttpServletResponse httpResponse = new MockHttpServletResponse();

        final ResponseEntity<String> response = loginHandler.login(request, httpResponse, null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void userLoginWithExceptionTest() {
        final LoginActions loginActions = Mockito.mock(LoginActions.class);
        ResponseFactory responseFactory = new ResponseFactory();
        final LoginController loginHandler = new LoginController(loginActions, responseFactory, csrfTokenRepository);

        final HttpServletRequest request = new MockHttpServletRequest();
        Mockito.when(loginActions.authenticateUser(Mockito.any())).thenThrow(new IllegalArgumentException("Test exception for catch all"));
        final HttpServletResponse httpResponse = new MockHttpServletResponse();

        final ResponseEntity<String> response = loginHandler.login(request, httpResponse, null);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
