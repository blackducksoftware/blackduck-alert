package com.synopsys.integration.alert.component.certificates;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyStore;
import java.security.cert.Certificate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.blackduck.integration.alert.api.certificates.AlertTrustStoreManager;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.model.CustomCertificateModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.certificates.web.CertificateTestUtil;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
@TestPropertySource(locations = "classpath:certificates/spring-certificate-test.properties")
class AlertTrustStoreManagerTestIT {
    public static final String CERTIFICATE_ALIAS = "certificate-alias";
    public static final String EMPTY_STRING_CONTENT = " \n\t\r  \n\t\r  \n";
    @Autowired
    private AlertProperties alertProperties;

    private final CertificateTestUtil certTestUtil = new CertificateTestUtil();

    @BeforeEach
    void init() throws Exception {
        certTestUtil.init(alertProperties);
    }

    @AfterEach
    void cleanUp() {
        certTestUtil.cleanup();
    }

    private CustomCertificateModel createCertificateModel() throws Exception {
        String certificateContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.CERTIFICATE_FILE_PATH);
        return new CustomCertificateModel(CERTIFICATE_ALIAS, certificateContent, DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
    }

    @Test
    void importCertificateTest() throws Exception {
        CustomCertificateModel customCertificateModel = createCertificateModel();
        AlertTrustStoreManager trustStoreManager = new AlertTrustStoreManager(alertProperties);
        trustStoreManager.importCertificate(customCertificateModel);
        KeyStore trustStore = trustStoreManager.getTrustStore().orElseThrow(() -> new AssertionError("Trust strore file should exist"));
        Certificate certificate = trustStore.getCertificate(CERTIFICATE_ALIAS);

        assertTrue(trustStoreManager.getTrustStore().isPresent());
        assertTrue(trustStoreManager.getAndValidateTrustStoreFile().exists());
        assertNotNull(certificate);
    }

    @Test
    void importNullCertificateTest() {
        AlertTrustStoreManager trustStoreManager = new AlertTrustStoreManager(alertProperties);
        assertThrows(AlertException.class, () -> trustStoreManager.importCertificate(null));

    }

    @Test
    void importNullCertificateAliasTest() throws Exception {
        String certificateContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.CERTIFICATE_FILE_PATH);
        CustomCertificateModel customCertificateModel = new CustomCertificateModel(
            null,
            certificateContent,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );
        AlertTrustStoreManager trustStoreManager = new AlertTrustStoreManager(alertProperties);
        assertThrows(AlertException.class, () -> trustStoreManager.importCertificate(customCertificateModel));
    }

    @Test
    void importNullCertificateContentTest() {
        CustomCertificateModel customCertificateModel = new CustomCertificateModel(CERTIFICATE_ALIAS, null, DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        AlertTrustStoreManager trustStoreManager = new AlertTrustStoreManager(alertProperties);
        assertThrows(AlertException.class, () -> trustStoreManager.importCertificate(customCertificateModel));
    }

    @Test
    void importEmptyCertificateAliasTest() throws Exception {
        String certificateContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.CERTIFICATE_FILE_PATH);
        CustomCertificateModel customCertificateModel = new CustomCertificateModel(
            EMPTY_STRING_CONTENT,
            certificateContent,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );
        AlertTrustStoreManager trustStoreManager = new AlertTrustStoreManager(alertProperties);
        assertThrows(AlertException.class, () -> trustStoreManager.importCertificate(customCertificateModel));
    }

    @Test
    void importEmptyCertificateContentTest() {
        CustomCertificateModel customCertificateModel = new CustomCertificateModel(
            CERTIFICATE_ALIAS,
            EMPTY_STRING_CONTENT,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );
        AlertTrustStoreManager trustStoreManager = new AlertTrustStoreManager(alertProperties);
        assertThrows(AlertException.class, () -> trustStoreManager.importCertificate(customCertificateModel));
    }

    @Test
    void removeCertificateByAliasTest() throws Exception {
        CustomCertificateModel customCertificateModel = createCertificateModel();
        AlertTrustStoreManager trustStoreManager = new AlertTrustStoreManager(alertProperties);
        trustStoreManager.importCertificate(customCertificateModel);
        trustStoreManager.removeCertificate(CERTIFICATE_ALIAS);
        KeyStore trustStore = trustStoreManager.getTrustStore().orElseThrow(() -> new AssertionError("Trust strore file should exist"));

        Certificate certificate = trustStore.getCertificate(CERTIFICATE_ALIAS);
        assertNull(certificate);
    }

    @Test
    void removeCertificateByModelContentTest() throws Exception {
        CustomCertificateModel customCertificateModel = createCertificateModel();
        AlertTrustStoreManager trustStoreManager = new AlertTrustStoreManager(alertProperties);
        trustStoreManager.importCertificate(customCertificateModel);
        trustStoreManager.removeCertificate(customCertificateModel);
        KeyStore trustStore = trustStoreManager.getTrustStore().orElseThrow(() -> new AssertionError("Trust strore file should exist"));

        Certificate certificate = trustStore.getCertificate(CERTIFICATE_ALIAS);
        assertNull(certificate);
    }

    @Test
    void removeNullCertificateModelTest() {
        CustomCertificateModel customCertificateModel = null;
        AlertTrustStoreManager trustStoreManager = new AlertTrustStoreManager(alertProperties);
        assertThrows(AlertException.class, () -> trustStoreManager.removeCertificate(customCertificateModel));
    }

    @Test
    void removeTrustStoreFileMissingTest() {
        AlertTrustStoreManager trustStoreManager = new AlertTrustStoreManager(new MockAlertProperties());
        assertThrows(AlertConfigurationException.class, () -> trustStoreManager.removeCertificate(CERTIFICATE_ALIAS));
    }

    @Test
    void getTrustStoreWithFileMissingTest() {
        AlertTrustStoreManager trustStoreManager = new AlertTrustStoreManager(new MockAlertProperties());
        assertTrue(trustStoreManager.getTrustStore().isEmpty());
    }

    @Test
    void getTrustStoreWithInvalidFilePathTest() {
        MockAlertProperties mockAlertProperties = new MockAlertProperties();
        mockAlertProperties.setTrustStoreFile("badprotocol:a_file-that-does-not-exist");
        AlertTrustStoreManager trustStoreManager = new AlertTrustStoreManager(mockAlertProperties);
        assertTrue(trustStoreManager.getTrustStore().isEmpty());
    }

    @Test
    void validateCertificateContentTest() throws Exception {
        CustomCertificateModel customCertificateModel = createCertificateModel();
        AlertTrustStoreManager trustStoreManager = new AlertTrustStoreManager(alertProperties);

        assertDoesNotThrow(() -> trustStoreManager.validateCertificateContent(customCertificateModel));
    }

    @Test
    void getAndValidateFileThrowsExceptionTest() {
        MockAlertProperties mockAlertProperties = new MockAlertProperties();
        mockAlertProperties.setTrustStoreFile("badprotocol:a_file-that-does-not-exist");
        AlertTrustStoreManager trustStoreManager = new AlertTrustStoreManager(mockAlertProperties);
        assertThrows(AlertConfigurationException.class, trustStoreManager::getAndValidateTrustStoreFile);
    }

    @Test
    void getAndValidateFileMissingTest() {
        AlertTrustStoreManager trustStoreManager = new AlertTrustStoreManager(new MockAlertProperties());
        assertThrows(AlertConfigurationException.class, trustStoreManager::getAndValidateTrustStoreFile);
    }
}

