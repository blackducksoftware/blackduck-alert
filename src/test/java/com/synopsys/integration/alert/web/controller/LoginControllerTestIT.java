package com.synopsys.integration.alert.web.controller;

import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.TestProperties;
import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.LdapProperties;
import com.synopsys.integration.alert.mock.model.MockLoginRestModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.web.security.authentication.ldap.LdapManager;

public class LoginControllerTestIT extends AlertIntegrationTest {

    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    private final String loginUrl = BaseController.BASE_PATH + "/login";
    private final String logoutUrl = BaseController.BASE_PATH + "/logout";
    @Autowired
    protected WebApplicationContext webApplicationContext;
    @Autowired
    protected BlackDuckProperties blackDuckProperties;
    @Autowired
    protected AlertProperties alertProperties;
    @Autowired
    protected LdapManager ldapManager;
    private MockMvc mockMvc;

    @Before
    public void setup() throws Exception {
        final LdapProperties ldapProperties = new LdapProperties();
        ldapProperties.setEnabled("false");
        ldapManager.updateConfiguration(ldapProperties);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testLogout() throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(logoutUrl).with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"));
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    //FIXME fix test
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
}
