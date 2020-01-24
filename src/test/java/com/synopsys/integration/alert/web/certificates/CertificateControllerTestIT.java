package com.synopsys.integration.alert.web.certificates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.synopsys.integration.alert.util.AlertIntegrationTest;

@Transactional
public class CertificateControllerTestIT extends BaseCertificateTestIT {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private CertificateActions actions;
    private MockMvc mockMvc;

    @Override
    @BeforeEach
    public void init() {
        actions = Mockito.mock(CertificateActions.class);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void readAllTest() {
        //Mockito.when(actions.readCertificates()).thenReturn(models);

    }
}
