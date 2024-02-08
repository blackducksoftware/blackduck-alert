package com.synopsys.integration.alert.component.certificates;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.Certificate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.component.certificates.web.CertificateTestUtil;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
@TestPropertySource(locations = "classpath:certificates/spring-certificate-test.properties")
class AlertClientCertificateManagerTestIT {
    public static final String EMPTY_STRING_CONTENT = " \n\t\r  \n\t\r  \n";
    @Autowired
    private AlertProperties alertProperties;
    private AlertClientCertificateManager clientCertificateManager;
    private final CertificateTestUtil certTestUtil = new CertificateTestUtil();

    @BeforeEach
    void initTest() throws Exception {
        certTestUtil.init(alertProperties);
    }

    @AfterEach
    void cleanup() {
        certTestUtil.cleanup();
    }

    private ClientCertificateModel createClientCertificateModel() throws IOException {
        String keyContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.KEY_MTLS_CLIENT_FILE_PATH);
        String certificateContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.CERTIFICATE_MTLS_CLIENT_FILE_PATH);

        return new ClientCertificateModel(
            CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD,
            keyContent,
            certificateContent
        );
    }

    @Test
    void importCertificateNullTest() {
        clientCertificateManager = new AlertClientCertificateManager();
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(null));
    }

    @Test
    void importCertificateNullContentTest() {
        clientCertificateManager = new AlertClientCertificateManager();
        ClientCertificateModel model = new ClientCertificateModel(
            CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD,
            null,
            EMPTY_STRING_CONTENT
        );
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model));
    }

    @Test
    void importCertificateEmptyContentTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        String keyContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.KEY_MTLS_CLIENT_FILE_PATH);
        ClientCertificateModel model = new ClientCertificateModel(
            CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD,
            keyContent,
            EMPTY_STRING_CONTENT
        );

        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model));
    }

    @Test
    void importCertificateNullKeyContentTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        String certificateContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.CERTIFICATE_MTLS_CLIENT_FILE_PATH);
        ClientCertificateModel model = new ClientCertificateModel(
            CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD,
            null,
            certificateContent
        );
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model));
    }

    @Test
    void importCertificateEmptyKeyContentTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        String certificateContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.CERTIFICATE_MTLS_CLIENT_FILE_PATH);
        ClientCertificateModel model = new ClientCertificateModel(
            CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD,
            EMPTY_STRING_CONTENT,
            certificateContent
        );
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model));
    }

    @Test
    void importCertificateNullKeyPasswordTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        String keyContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.KEY_MTLS_CLIENT_FILE_PATH);
        String certificateContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.CERTIFICATE_MTLS_CLIENT_FILE_PATH);
        ClientCertificateModel model = new ClientCertificateModel(
            null,
            keyContent,
            certificateContent
        );
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model));
    }

    @Test
    void importCertificateEmptyKeyPasswordTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        String keyContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.KEY_MTLS_CLIENT_FILE_PATH);
        String certificateContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.CERTIFICATE_MTLS_CLIENT_FILE_PATH);
        ClientCertificateModel model = new ClientCertificateModel(
            EMPTY_STRING_CONTENT,
            keyContent,
            certificateContent
        );
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model));
    }

    @Test
    void importCertificateTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        ClientCertificateModel model = createClientCertificateModel();
        Certificate validationCertificate = certTestUtil.loadCertificate(model.getCertificateContent());
        clientCertificateManager.importCertificate(model);
        KeyStore clientKeystore = clientCertificateManager.getClientKeyStore().orElseThrow(() -> new AssertionError("Keystore missing when it should exist"));
        assertTrue(clientKeystore.containsAlias(AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS));
        Certificate clientCertificate = clientKeystore.getCertificate(AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS);

        assertEquals(validationCertificate.getType(), clientCertificate.getType());
        assertEquals(validationCertificate.getPublicKey().getAlgorithm(), clientCertificate.getPublicKey().getAlgorithm());
        assertEquals(validationCertificate.getPublicKey().getFormat(), clientCertificate.getPublicKey().getFormat());
        assertArrayEquals(validationCertificate.getPublicKey().getEncoded(), clientCertificate.getPublicKey().getEncoded());
    }

    @Test
    void removeCertificateTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        ClientCertificateModel model = createClientCertificateModel();
        clientCertificateManager.importCertificate(model);
        clientCertificateManager.removeCertificate();
        assertTrue(clientCertificateManager.getClientKeyStore().isEmpty());
    }
}
