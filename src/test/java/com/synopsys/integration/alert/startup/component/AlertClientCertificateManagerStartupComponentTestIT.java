package com.synopsys.integration.alert.startup.component;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.synopsys.integration.alert.api.certificates.AlertClientCertificateManager;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateKeyModel;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.certificates.web.CertificateTestUtil;
import com.synopsys.integration.alert.database.api.DefaultClientCertificateAccessor;
import com.synopsys.integration.alert.database.api.DefaultClientCertificateKeyAccessor;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
@TestPropertySource(locations = "classpath:certificates/spring-certificate-test.properties")
class AlertClientCertificateManagerStartupComponentTestIT {
    @Autowired
    private AlertProperties alertProperties;
    @Autowired
    private AlertClientCertificateManagerStartupComponent alertClientCertificateManagerStartupComponent;
    @Autowired
    private AlertClientCertificateManager alertClientCertificateManager;
    @Autowired
    private DefaultClientCertificateKeyAccessor clientCertificateKeyAccessor;
    @Autowired
    private DefaultClientCertificateAccessor clientCertificateAccessor;
    private final CertificateTestUtil certTestUtil = new CertificateTestUtil();

    @BeforeEach
    void initTest() throws Exception {
        certTestUtil.init(alertProperties);
    }

    @AfterEach
    void cleanup() throws AlertException {
        certTestUtil.cleanup();
        clientCertificateKeyAccessor.deleteConfiguration();
        clientCertificateAccessor.deleteConfiguration();
        alertClientCertificateManager.removeCertificate();
    }

    @Test
    void initializeWithClientCertificatePersistedTest() throws Exception {
        ClientCertificateKeyModel clientCertificateKeyModel = clientCertificateKeyAccessor.createConfiguration(certTestUtil.createClientKeyModel());
        ClientCertificateModel clientCertificateModel = clientCertificateAccessor.createConfiguration(certTestUtil.createClientModel(clientCertificateKeyModel));
        assertTrue(alertClientCertificateManager.getClientKeyStore().isEmpty());

        alertClientCertificateManagerStartupComponent.initialize();
        KeyStore clientKeystore = alertClientCertificateManager.getClientKeyStore().orElseThrow(() -> new AssertionError("Keystore missing when it should exist"));
        assertTrue(clientKeystore.containsAlias(AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS));
        Certificate clientCertificate = clientKeystore.getCertificate(AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS);
        Certificate validationCertificate = certTestUtil.loadCertificate(clientCertificateModel.getCertificateContent());

        assertEquals(validationCertificate.getType(), clientCertificate.getType());
        assertEquals(validationCertificate.getPublicKey().getAlgorithm(), clientCertificate.getPublicKey().getAlgorithm());
        assertEquals(validationCertificate.getPublicKey().getFormat(), clientCertificate.getPublicKey().getFormat());
        assertArrayEquals(validationCertificate.getPublicKey().getEncoded(), clientCertificate.getPublicKey().getEncoded());
    }

    @Test
    void initializeWithoutCertificateOrKeyPersistedTest() {
        assertTrue(alertClientCertificateManager.getClientKeyStore().isEmpty());
        alertClientCertificateManagerStartupComponent.initialize();
        assertTrue(alertClientCertificateManager.getClientKeyStore().isEmpty());
    }

    @Test
    void initializeWithoutCertificatePersistedTest() throws Exception {
        clientCertificateKeyAccessor.createConfiguration(certTestUtil.createClientKeyModel());
        assertTrue(alertClientCertificateManager.getClientKeyStore().isEmpty());
        alertClientCertificateManagerStartupComponent.initialize();
        assertTrue(alertClientCertificateManager.getClientKeyStore().isEmpty());
    }

    @Test
    void certificateManagerThrowsExceptionTest() throws Exception {
        UUID id = UUID.randomUUID();
        ClientCertificateKeyModel keyModelWithoutPassword = new ClientCertificateKeyModel(
            id,
            CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD,
            false,
            CertificateTestUtil.EMPTY_STRING_CONTENT,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );

        ClientCertificateKeyModel clientCertificateKeyModel = clientCertificateKeyAccessor.createConfiguration(keyModelWithoutPassword);
        clientCertificateAccessor.createConfiguration(certTestUtil.createClientModel(clientCertificateKeyModel));
        assertTrue(alertClientCertificateManager.getClientKeyStore().isEmpty());

        alertClientCertificateManagerStartupComponent.initialize();
        assertTrue(alertClientCertificateManager.getClientKeyStore().isEmpty());
    }
}
