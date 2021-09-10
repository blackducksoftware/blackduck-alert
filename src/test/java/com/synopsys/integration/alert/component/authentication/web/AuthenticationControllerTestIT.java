package com.synopsys.integration.alert.component.authentication.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.authentication.security.ldap.LdapManager;
import com.synopsys.integration.alert.mock.model.MockLoginRestModel;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;

@Transactional
@AlertIntegrationTest
public class AuthenticationControllerTestIT {
    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    private final String loginUrl = AlertRestConstants.BASE_PATH + "/login";
    private final String logoutUrl = AlertRestConstants.BASE_PATH + "/logout";
    @Autowired
    protected WebApplicationContext webApplicationContext;
    @Autowired
    protected AlertProperties alertProperties;
    @Autowired
    protected LdapManager ldapManager;
    @Autowired
    protected AuthorizationManager authorizationManager;
    @Autowired
    protected CsrfTokenRepository csrfTokenRepository;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testLogout() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(logoutUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN));
        mockMvc.perform(request).andExpect(ResultMatcher.matchAll(
            MockMvcResultMatchers.redirectedUrl("/"),
            MockMvcResultMatchers.status().isNoContent()
        ));
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
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void userLoginWithValidSessionTest() {
        AuthenticationActions authenticationActions = Mockito.mock(AuthenticationActions.class);
        Mockito.when(authenticationActions.authenticateUser(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class), Mockito.any()))
            .thenReturn(new ActionResponse<>(HttpStatus.NO_CONTENT));
        AuthenticationController loginController = new AuthenticationController(authenticationActions);

        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpSession session = servletRequest.getSession(true);
        session.setMaxInactiveInterval(30);
        HttpServletResponse servletResponse = new MockHttpServletResponse();

        try {
            loginController.login(servletRequest, servletResponse, null);
        } catch (ResponseStatusException e) {
            fail("Expect an OK response, but a ResponseStatusException was thrown: " + e.getMessage());
        }
    }

    @Test
    public void userLoginWithInvalidSessionTest() {
        AuthenticationActions authenticationActions = Mockito.mock(AuthenticationActions.class);
        Mockito.when(authenticationActions.authenticateUser(Mockito.any(HttpServletRequest.class), Mockito.any(HttpServletResponse.class), Mockito.any()))
            .thenReturn(new ActionResponse<>(HttpStatus.UNAUTHORIZED));
        AuthenticationController loginController = new AuthenticationController(authenticationActions);

        HttpServletRequest servletRequest = new MockHttpServletRequest();
        HttpServletResponse servletResponse = new MockHttpServletResponse();

        try {
            loginController.login(servletRequest, servletResponse, null);
        } catch (ResponseStatusException ex) {
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        }
    }

}
