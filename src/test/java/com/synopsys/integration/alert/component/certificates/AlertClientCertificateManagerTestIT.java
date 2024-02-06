package com.synopsys.integration.alert.component.certificates;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
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

    private ClientCertificateKeyModel createClientKeyModel() throws IOException {
        UUID id = UUID.randomUUID();
        String content = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.KEY_MTLS_CLIENT_FILE_PATH);
        ClientCertificateKeyModel keyModel = new ClientCertificateKeyModel(
            id,
            CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD,
            false,
            content,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );

        return keyModel;
    }

    private ClientCertificateModel createClientModel(ClientCertificateKeyModel keyModel) throws IOException {
        UUID id = UUID.randomUUID();
        String content = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.CERTIFICATE_MTLS_CLIENT_FILE_PATH);
        ClientCertificateModel certificateModel = new ClientCertificateModel(
            id,
            keyModel.getId(),
            content,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );
        return certificateModel;
    }

    @Test
    void importCertificateNullTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        ClientCertificateKeyModel keyModel = createClientKeyModel();
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(null, keyModel));
    }

    @Test
    void importCertificateNullContentTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        ClientCertificateKeyModel keyModel = createClientKeyModel();
        UUID id = UUID.randomUUID();
        ClientCertificateModel model = new ClientCertificateModel(
            id,
            keyModel.getId(),
            null,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model, keyModel));
    }

    @Test
    void importCertificateEmptyContentTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        ClientCertificateKeyModel keyModel = createClientKeyModel();
        UUID id = UUID.randomUUID();
        ClientCertificateModel model = new ClientCertificateModel(
            id,
            keyModel.getId(),
            EMPTY_STRING_CONTENT,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model, keyModel));
    }

    @Test
    void importCertificateKeyNullTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        ClientCertificateKeyModel keyModel = createClientKeyModel();
        ClientCertificateModel model = createClientModel(keyModel);
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model, null));
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
        ClientCertificateModel model = createClientModel(keyModel);
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model, keyModel));
    }

    @Test
    void importCertificateKeyEmptyContentTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        UUID id = UUID.randomUUID();
        ClientCertificateKeyModel keyModel = new ClientCertificateKeyModel(
            id,
            CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD,
            false,
            EMPTY_STRING_CONTENT,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );
        ClientCertificateModel model = createClientModel(keyModel);
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model, keyModel));
    }

    @Test
    void importCertificateKeyEmptyPasswordContentTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        UUID id = UUID.randomUUID();
        String keyContent = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.KEY_MTLS_CLIENT_FILE_PATH);
        ClientCertificateKeyModel keyModel = new ClientCertificateKeyModel(
            id,
            EMPTY_STRING_CONTENT,
            false,
            keyContent,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );
        ClientCertificateModel model = createClientModel(keyModel);
        assertThrows(AlertException.class, () -> clientCertificateManager.importCertificate(model, keyModel));
    }

    @Test
    void importCertificateTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager();
        ClientCertificateKeyModel keyModel = createClientKeyModel();
        ClientCertificateModel model = createClientModel(keyModel);
        Certificate validationCertificate = certTestUtil.loadCertificate(model.getCertificateContent());
        clientCertificateManager.importCertificate(model, keyModel);
        KeyStore clientKeystore = clientCertificateManager.getClientKeyStore().orElseThrow(() -> new AssertionError("Keystore missing when it should exist"));
        assertEquals(keyModel.getPassword(), clientCertificateManager.getClientKeyPassword());
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
        ClientCertificateKeyModel keyModel = createClientKeyModel();
        ClientCertificateModel model = createClientModel(keyModel);
        clientCertificateManager.importCertificate(model, keyModel);
        clientCertificateManager.removeCertificate();
        assertTrue(clientCertificateManager.getClientKeyStore().isEmpty());
        assertTrue(clientCertificateManager.getClientKeyPassword().isEmpty());
    }

    @Test
    void clientCertificateNotSetTest() {
        clientCertificateManager = new AlertClientCertificateManager();
        Optional<KeyStore> keystore = clientCertificateManager.getClientKeyStore();
        Optional<String> password = clientCertificateManager.getClientKeyPassword();
        assertTrue(keystore.isEmpty());
        assertTrue(password.isEmpty());
    }
}
