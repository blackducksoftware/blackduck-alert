package com.synopsys.integration.alert.component.certificates.web;

import java.net.URI;
import java.nio.charset.Charset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.CustomCertificateAccessor;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.certificates.AlertTrustStoreManager;
import com.synopsys.integration.alert.component.certificates.CertificatesDescriptorKey;
import com.synopsys.integration.alert.database.certificates.CustomCertificateRepository;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;

import junit.framework.AssertionFailedError;

@Transactional
@AlertIntegrationTest
@TestPropertySource(locations = "classpath:certificates/spring-certificate-test.properties")
public class CertificateControllerTestIT {
    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    @Autowired
    private CustomCertificateRepository customCertificateRepository;

    @Autowired
    private AlertProperties alertProperties;

    private final CertificateTestUtil certTestUtil = new CertificateTestUtil();

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private Gson gson;

    @Autowired
    private CertificatesDescriptorKey certificatesDescriptorKey;

    @Autowired
    private AlertTrustStoreManager trustStoreService;
    @Autowired
    private CustomCertificateAccessor certificateAccessor;

    private CertificateActions certificateActions;
    private MockMvc mockMvc;

    @BeforeEach
    public void init() throws Exception {
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasCreatePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasDeletePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasWritePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasExecutePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        certificateActions = new CertificateActions(certificatesDescriptorKey, authorizationManager, certificateAccessor, trustStoreService);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
        certTestUtil.init(alertProperties);
    }

    @AfterEach
    public void cleanup() {
        certTestUtil.cleanup(customCertificateRepository);
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void readAllTest() throws Exception {
        String url = CertificatesController.API_BASE_URL;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void readSingleTest() throws Exception {
        CertificateModel expectedCertificate = certTestUtil.createCertificate(certificateActions)
                                                   .orElseThrow(AssertionFailedError::new);

        String url = CertificatesController.API_BASE_URL + String.format("/%s", expectedCertificate.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void createTest() throws Exception {
        String certificateContent = certTestUtil.readCertificateContents();
        CertificateModel certificateModel = new CertificateModel(CertificateTestUtil.TEST_ALIAS, certificateContent, DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        String url = CertificatesController.API_BASE_URL;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                    .content(gson.toJson(certificateModel))
                                                    .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void updateTest() throws Exception {
        CertificateModel expectedCertificate = certTestUtil.createCertificate(certificateActions)
                                                   .orElseThrow(AssertionFailedError::new);
        CertificateModel updatedCertificate = new CertificateModel(expectedCertificate.getId(), "new-alias", expectedCertificate.getCertificateContent(), expectedCertificate.getLastUpdated());
        String url = CertificatesController.API_BASE_URL + String.format("/%s", expectedCertificate.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                    .content(gson.toJson(updatedCertificate))
                                                    .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void deleteTest() throws Exception {
        CertificateModel expectedCertificate = certTestUtil.createCertificate(certificateActions)
                                                   .orElseThrow(AssertionFailedError::new);
        String url = CertificatesController.API_BASE_URL + String.format("/%s", expectedCertificate.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    public void readAllForbiddenTest() throws Exception {
        String url = CertificatesController.API_BASE_URL;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("badUser"))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void readSingleForbiddenTest() throws Exception {
        CertificateModel expectedCertificate = certTestUtil.createCertificate(certificateActions)
                                                   .orElseThrow(AssertionFailedError::new);

        String url = CertificatesController.API_BASE_URL + String.format("/%s", expectedCertificate.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("badUser"))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void createForbiddenTest() throws Exception {
        String certificateContent = certTestUtil.readCertificateContents();
        CertificateModel certificateModel = new CertificateModel(CertificateTestUtil.TEST_ALIAS, certificateContent, DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        String url = CertificatesController.API_BASE_URL;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("badUser"))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                    .content(gson.toJson(certificateModel))
                                                    .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void updateForbiddenTest() throws Exception {
        CertificateModel expectedCertificate = certTestUtil.createCertificate(certificateActions)
                                                   .orElseThrow(AssertionFailedError::new);
        CertificateModel updatedCertificate = new CertificateModel(expectedCertificate.getId(), "new-alias", expectedCertificate.getCertificateContent());
        String url = CertificatesController.API_BASE_URL + String.format("/%s", expectedCertificate.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("badUser"))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                    .content(gson.toJson(updatedCertificate))
                                                    .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void deleteForbiddenTest() throws Exception {
        CertificateModel expectedCertificate = certTestUtil.createCertificate(certificateActions)
                                                   .orElseThrow(AssertionFailedError::new);
        String url = CertificatesController.API_BASE_URL + String.format("/%s", expectedCertificate.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("badUser"))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

}
