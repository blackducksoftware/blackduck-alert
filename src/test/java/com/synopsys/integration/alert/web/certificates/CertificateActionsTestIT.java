package com.synopsys.integration.alert.web.certificates;

import static com.synopsys.integration.alert.web.certificates.CertificateTestUtil.TEST_ALIAS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.CustomCertificateAccessor;
import com.synopsys.integration.alert.common.persistence.model.CustomCertificateModel;
import com.synopsys.integration.alert.common.security.CertificateUtility;
import com.synopsys.integration.alert.database.certificates.CustomCertificateRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.model.CertificateModel;

@Transactional
@TestPropertySource(locations = "classpath:certificates/spring-certificate-test.properties")
public class CertificateActionsTestIT extends AlertIntegrationTest {

    @Autowired
    private CustomCertificateRepository customCertificateRepository;

    @Autowired
    private CertificateActions certificateActions;

    @Autowired
    private AlertProperties alertProperties;

    @Autowired
    private CustomCertificateAccessor certificateAccessor;

    private CertificateTestUtil certTestUtil = new CertificateTestUtil();

    @BeforeEach
    public void init() throws Exception {
        certTestUtil.init(alertProperties);
    }

    @AfterEach
    public void cleanup() {
        certTestUtil.cleanup(customCertificateRepository);
    }

    @Test
    public void readAllEmptyListTest() {
        assertTrue(certificateActions.readCertificates().isEmpty());
    }

    @Test
    public void createCertificateTest() throws Exception {
        String certificateContent = certTestUtil.readCertificateContents();
        CertificateModel savedCertificate = certTestUtil.createCertificate(certificateActions);

        assertNotNull(savedCertificate.getId());
        assertEquals(TEST_ALIAS, savedCertificate.getAlias());
        assertEquals(certificateContent, savedCertificate.getCertificateContent());
    }

    @Test
    public void createCertificateIdTest() throws Exception {
        String certificateContent = certTestUtil.readCertificateContents();
        try {
            CertificateModel certificate = new CertificateModel("alias", certificateContent);
            certificate.setId("badId");
            certificateActions.createCertificate(certificate);
            fail();
        } catch (AlertDatabaseConstraintException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void readAllTest() throws Exception {
        certTestUtil.createCertificate(certificateActions);
        assertEquals(1, certificateActions.readCertificates().size());
    }

    @Test
    public void readSingleCertificateTest() throws Exception {
        CertificateModel expectedCertificate = certTestUtil.createCertificate(certificateActions);
        Optional<CertificateModel> actualCertificate = certificateActions.readCertificate(Long.valueOf(expectedCertificate.getId()));
        assertTrue(actualCertificate.isPresent());
        assertEquals(expectedCertificate, actualCertificate.get());
    }

    @Test
    public void updateCertificateTest() throws Exception {
        String certificateContent = certTestUtil.readCertificateContents();
        CertificateModel savedCertificate = certTestUtil.createCertificate(certificateActions);

        String updatedAlias = "updated-alias";
        CertificateModel newModel = new CertificateModel(savedCertificate.getId(), updatedAlias, certificateContent);
        Optional<CertificateModel> updatedCertificate = certificateActions.updateCertificate(Long.valueOf(savedCertificate.getId()), newModel);
        assertTrue(updatedCertificate.isPresent());

        CertificateModel updatedModel = updatedCertificate.get();
        assertEquals(savedCertificate.getId(), updatedModel.getId());
        assertEquals(updatedAlias, updatedModel.getAlias());
        assertEquals(certificateContent, updatedModel.getCertificateContent());
    }

    @Test
    public void updateCertificateMissingIdTest() throws Exception {
        String certificateContent = certTestUtil.readCertificateContents();
        CertificateModel certificate = new CertificateModel("-1", certificateContent);
        Optional<CertificateModel> result = certificateActions.updateCertificate(-1L, certificate);
        assertTrue(result.isEmpty());
    }

    @Test
    public void deleteCertificateTest() throws Exception {
        CertificateModel savedCertificate = certTestUtil.createCertificate(certificateActions);
        certificateActions.deleteCertificate(Long.valueOf(savedCertificate.getId()));
        assertTrue(customCertificateRepository.findAll().isEmpty());
    }

    @Test
    public void createExceptionTest() throws Exception {
        String certificateContent = certTestUtil.readCertificateContents();
        CertificateModel certificate = new CertificateModel(TEST_ALIAS, certificateContent);
        CertificateUtility certificateUtility = Mockito.mock(CertificateUtility.class);
        Mockito.doThrow(new AlertException("Test exception")).when(certificateUtility).importCertificate(Mockito.any(CustomCertificateModel.class));
        CertificateActions certificateActions = new CertificateActions(certificateAccessor, certificateUtility);
        try {
            certificateActions.createCertificate(certificate);
            fail();
        } catch (AlertException ex) {
            assertNotNull(ex.getMessage());
        }
        assertTrue(certificateAccessor.getCertificates().isEmpty());
    }

}
