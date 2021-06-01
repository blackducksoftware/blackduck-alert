package com.synopsys.integration.alert.component.certificates.web;

import static com.synopsys.integration.alert.component.certificates.web.CertificateTestUtil.TEST_ALIAS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.CustomCertificateAccessor;
import com.synopsys.integration.alert.common.persistence.model.CustomCertificateModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.certificates.AlertTrustStoreManager;
import com.synopsys.integration.alert.component.certificates.CertificatesDescriptorKey;
import com.synopsys.integration.alert.database.certificates.CustomCertificateRepository;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

import junit.framework.AssertionFailedError;

@Transactional
@AlertIntegrationTest
@TestPropertySource(locations = "classpath:certificates/spring-certificate-test.properties")
public class CertificateActionsTestIT {

    @Autowired
    private CustomCertificateRepository customCertificateRepository;

    @Autowired
    private AlertProperties alertProperties;

    @Autowired
    private CustomCertificateAccessor certificateAccessor;

    private final CertificateTestUtil certTestUtil = new CertificateTestUtil();

    @Autowired
    private CertificatesDescriptorKey certificatesDescriptorKey;

    @Autowired
    private AlertTrustStoreManager trustStoreService;

    private CertificateActions certificateActions;

    @BeforeEach
    public void init() throws Exception {
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasCreatePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasDeletePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasWritePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasExecutePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);

        certificateActions = new CertificateActions(certificatesDescriptorKey, authorizationManager, certificateAccessor, trustStoreService);
        certTestUtil.init(alertProperties);
    }

    @AfterEach
    public void cleanup() {
        certTestUtil.cleanup(customCertificateRepository);
    }

    @Test
    public void readAllEmptyListTest() {
        ActionResponse<MultiCertificateModel> response = certificateActions.getAll();
        assertTrue(response.hasContent());
        assertTrue(response.getContent().isPresent());
        assertTrue(response.getContent().get().getCertificates().isEmpty());
    }

    @Test
    public void createCertificateTest() throws Exception {
        String certificateContent = certTestUtil.readCertificateContents();
        Optional<CertificateModel> certificate = certTestUtil.createCertificate(certificateActions);
        assertTrue(certificate.isPresent());
        CertificateModel savedCertificate = certificate.get();
        assertNotNull(savedCertificate.getId());
        assertEquals(TEST_ALIAS, savedCertificate.getAlias());
        assertEquals(certificateContent, savedCertificate.getCertificateContent());
    }

    @Test
    public void createCertificateIdTest() throws Exception {
        String certificateContent = certTestUtil.readCertificateContents();
        CertificateModel certificate = new CertificateModel("alias", certificateContent, DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        certificate.setId("badId");
        ActionResponse<CertificateModel> response = certificateActions.create(certificate);
        assertTrue(response.isError());
    }

    @Test
    public void readAllTest() throws Exception {
        certTestUtil.createCertificate(certificateActions);
        List<CertificateModel> certificates = certificateActions.getAll().getContent()
                                                  .map(MultiCertificateModel::getCertificates)
                                                  .orElse(List.of());
        assertEquals(1, certificates.size());
    }

    @Test
    public void readSingleCertificateTest() throws Exception {
        CertificateModel expectedCertificate = certTestUtil.createCertificate(certificateActions)
                                                   .orElseThrow(AssertionFailedError::new);
        ActionResponse<CertificateModel> response = certificateActions.getOne(Long.valueOf(expectedCertificate.getId()));
        Optional<CertificateModel> actualCertificate = response.getContent();
        assertTrue(actualCertificate.isPresent());
        assertEquals(expectedCertificate, actualCertificate.get());
    }

    @Test
    public void updateCertificateTest() throws Exception {
        String certificateContent = certTestUtil.readCertificateContents();
        CertificateModel savedCertificate = certTestUtil.createCertificate(certificateActions)
                                                .orElseThrow(AssertionFailedError::new);

        String updatedAlias = "updated-alias";
        CertificateModel newModel = new CertificateModel(savedCertificate.getId(), updatedAlias, certificateContent, savedCertificate.getLastUpdated());
        Optional<CertificateModel> updatedCertificate = certificateActions.update(Long.valueOf(savedCertificate.getId()), newModel).getContent();
        assertTrue(updatedCertificate.isPresent());

        CertificateModel updatedModel = updatedCertificate.get();
        assertEquals(savedCertificate.getId(), updatedModel.getId());
        assertEquals(updatedAlias, updatedModel.getAlias());
        assertEquals(certificateContent, updatedModel.getCertificateContent());
    }

    @Test
    public void updateCertificateMissingIdTest() throws Exception {
        String certificateContent = certTestUtil.readCertificateContents();
        CertificateModel certificate = new CertificateModel("-1", certificateContent, DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        Optional<CertificateModel> result = certificateActions.update(-1L, certificate).getContent();
        assertTrue(result.isEmpty());
    }

    @Test
    public void deleteCertificateTest() throws Exception {
        CertificateModel savedCertificate = certTestUtil.createCertificate(certificateActions)
                                                .orElseThrow(AssertionFailedError::new);
        certificateActions.delete(Long.valueOf(savedCertificate.getId()));
        assertTrue(customCertificateRepository.findAll().isEmpty());
    }

    @Test
    public void createExceptionTest() throws Exception {
        String certificateContent = certTestUtil.readCertificateContents();
        CertificateModel certificate = new CertificateModel(TEST_ALIAS, certificateContent, DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        AlertTrustStoreManager trustStoreService = Mockito.mock(AlertTrustStoreManager.class);
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasCreatePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.doThrow(new AlertException("Test exception")).when(trustStoreService).importCertificate(Mockito.any(CustomCertificateModel.class));
        CertificateActions certificateActions = new CertificateActions(new CertificatesDescriptorKey(), authorizationManager, certificateAccessor, trustStoreService);
        ActionResponse<CertificateModel> response = certificateActions.create(certificate);
        assertTrue(response.isError());
        assertTrue(certificateAccessor.getCertificates().isEmpty());
    }

}
