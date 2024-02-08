package com.synopsys.integration.alert.component.certificates;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateKeyModel;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.certificates.web.CertificateTestUtil;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
@TestPropertySource(locations = "classpath:certificates/spring-certificate-test.properties")
class AlertClientCertificateManagerTestIT {
    @Autowired
    private AlertProperties alertProperties;
    private AlertClientCertificateManager clientCertificateManager;
    private final CertificateTestUtil certTestUtil = new CertificateTestUtil();

    @BeforeEach
    void initTest() throws Exception {
        certTestUtil.init(alertProperties);
    }

    @AfterEach
    void cleanup() throws AlertException {
        certTestUtil.cleanup();
        clientCertificateManager.removeCertificate();
    }

    @Test
    void importCertificateNullTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        ClientCertificateKeyModel keyModel = certTestUtil.createClientKeyModel();
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(null, keyModel));
        assertFalse(clientCertificateManager.containsClientCertificate());
    }

    @Test
    void importCertificateNullContentTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        ClientCertificateKeyModel keyModel = certTestUtil.createClientKeyModel();
        UUID id = UUID.randomUUID();
        ClientCertificateModel model = new ClientCertificateModel(
            id,
            keyModel.getId(),
            null,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model, keyModel));
        assertFalse(clientCertificateManager.containsClientCertificate());
    }

    @Test
    void importCertificateEmptyContentTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        ClientCertificateKeyModel keyModel = certTestUtil.createClientKeyModel();
        UUID id = UUID.randomUUID();
        ClientCertificateModel model = new ClientCertificateModel(
            id,
            keyModel.getId(),
            CertificateTestUtil.EMPTY_STRING_CONTENT,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model, keyModel));
        assertFalse(clientCertificateManager.containsClientCertificate());
    }

    @Test
    void importCertificateKeyNullTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        ClientCertificateKeyModel keyModel = certTestUtil.createClientKeyModel();
        ClientCertificateModel model = certTestUtil.createClientModel(keyModel);
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model, null));
        assertFalse(clientCertificateManager.containsClientCertificate());
    }

    @Test
    void importCertificateKeyNullContentTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        UUID id = UUID.randomUUID();
        ClientCertificateKeyModel keyModel = new ClientCertificateKeyModel(
            id,
            CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD,
            false,
            null,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );
        ClientCertificateModel model = certTestUtil.createClientModel(keyModel);
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model, keyModel));
        assertFalse(clientCertificateManager.containsClientCertificate());
    }

    @Test
    void importCertificateKeyEmptyContentTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        UUID id = UUID.randomUUID();
        ClientCertificateKeyModel keyModel = new ClientCertificateKeyModel(
            id,
            CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD,
            false,
            CertificateTestUtil.EMPTY_STRING_CONTENT,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );
        ClientCertificateModel model = certTestUtil.createClientModel(keyModel);
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model, keyModel));
        assertFalse(clientCertificateManager.containsClientCertificate());
    }

    @Test
    void importCertificateKeyEmptyPasswordContentTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        UUID id = UUID.randomUUID();
        String keyContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.KEY_MTLS_CLIENT_FILE_PATH);
        ClientCertificateKeyModel keyModel = new ClientCertificateKeyModel(
            id,
            CertificateTestUtil.EMPTY_STRING_CONTENT,
            false,
            keyContent,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );
        ClientCertificateModel model = certTestUtil.createClientModel(keyModel);
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model, keyModel));
        assertFalse(clientCertificateManager.containsClientCertificate());
    }

    @Test
    void importCertificateTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        ClientCertificateKeyModel keyModel = certTestUtil.createClientKeyModel();
        ClientCertificateModel model = certTestUtil.createClientModel(keyModel);
        Certificate validationCertificate = certTestUtil.loadCertificate(model.getCertificateContent());
        clientCertificateManager.importCertificate(model, keyModel);
        KeyStore clientKeystore = clientCertificateManager.getClientKeyStore().orElseThrow(() -> new AssertionError("Keystore missing when it should exist"));

        assertTrue(clientCertificateManager.containsClientCertificate());
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
        ClientCertificateKeyModel keyModel = certTestUtil.createClientKeyModel();
        ClientCertificateModel model = certTestUtil.createClientModel(keyModel);
        clientCertificateManager.importCertificate(model, keyModel);
        clientCertificateManager.removeCertificate();
        assertFalse(clientCertificateManager.containsClientCertificate());
        assertTrue(clientCertificateManager.getClientKeyStore().isEmpty());
    }

    @Test
    void clientCertificateNotSetTest() {
        clientCertificateManager = new AlertClientCertificateManager();
        Optional<KeyStore> keystore = clientCertificateManager.getClientKeyStore();
        assertFalse(clientCertificateManager.containsClientCertificate());
        assertTrue(keystore.isEmpty());
    }
}
