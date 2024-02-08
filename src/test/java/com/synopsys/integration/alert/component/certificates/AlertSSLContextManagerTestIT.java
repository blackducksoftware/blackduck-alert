package com.synopsys.integration.alert.component.certificates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateKeyModel;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.common.persistence.model.CustomCertificateModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.certificates.web.CertificateTestUtil;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
@TestPropertySource(locations = "classpath:certificates/spring-certificate-test.properties")
class AlertSSLContextManagerTestIT {
    public static final String ROOT_CERTIFICATE_ALIAS = "root-certificate";
    public static final String SERVER_CERTIFICATE_ALIAS = "server-certificate";
    @Autowired
    private AlertProperties alertProperties;
    @Autowired
    private AlertTrustStoreManager trustStoreManager;
    @Autowired
    private AlertClientCertificateManager clientCertificateManager;

    private final CertificateTestUtil certTestUtil = new CertificateTestUtil();

    @BeforeEach
    void initTest() throws Exception {
        certTestUtil.init(alertProperties);
    }

    @AfterEach
    void cleanup() {
        try {
            trustStoreManager.removeCertificate(ROOT_CERTIFICATE_ALIAS);
            trustStoreManager.removeCertificate(SERVER_CERTIFICATE_ALIAS);
            clientCertificateManager.removeCertificate();
        } catch (AlertException e) {
            // ignore the exception and just continue to clean up.
        }
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

    private CustomCertificateModel createTrustStoreCertificate(String alias, String certificateFilePath) throws IOException {
        String content = certTestUtil.readCertificateOrKeyContents(certificateFilePath);
        return new CustomCertificateModel(alias, content, DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
    }

    @Test
    void createValidSSLContextTest() throws Exception {
        AlertSSLContextManager sslContextManager = new AlertSSLContextManager(trustStoreManager, clientCertificateManager);
        // load certificate data.
        ClientCertificateKeyModel keyModel = createClientKeyModel();
        ClientCertificateModel model = createClientModel(keyModel);
        CustomCertificateModel rootCertificate = createTrustStoreCertificate(ROOT_CERTIFICATE_ALIAS, CertificateTestUtil.CERTIFICATE_MTLS_ROOT_CA_FILE_PATH);
        CustomCertificateModel serverCertificate = createTrustStoreCertificate(SERVER_CERTIFICATE_ALIAS, CertificateTestUtil.CERTIFICATE_MTLS_SERVER_FILE_PATH);

        trustStoreManager.importCertificate(rootCertificate);
        trustStoreManager.importCertificate(serverCertificate);
        clientCertificateManager.importCertificate(model, keyModel);

        Optional<SSLContext> sslContext = sslContextManager.buildSslContext();
        assertTrue(sslContext.isPresent());
    }

    @Test
    void createDefaultSSLContextTest() throws Exception {
        AlertSSLContextManager sslContextManager = new AlertSSLContextManager(trustStoreManager, clientCertificateManager);

        Optional<SSLContext> sslContext = sslContextManager.buildSslContext();
        assertTrue(sslContext.isPresent());
        assertEquals(SSLContext.getDefault(), sslContext.get());
    }

    @Test
    void createDefaultSSLContextFromMissingTrustStoreTest() throws Exception {
        AlertTrustStoreManager missingFileTrustStoreManager = new AlertTrustStoreManager(new MockAlertProperties());
        AlertSSLContextManager sslContextManager = new AlertSSLContextManager(missingFileTrustStoreManager, clientCertificateManager);

        Optional<SSLContext> sslContext = sslContextManager.buildSslContext();
        assertTrue(sslContext.isPresent());
        assertEquals(SSLContext.getDefault(), sslContext.get());
    }


}
