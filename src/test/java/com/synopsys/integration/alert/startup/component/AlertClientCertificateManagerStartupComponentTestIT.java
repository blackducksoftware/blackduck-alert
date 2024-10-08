package com.synopsys.integration.alert.startup.component;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyStore;
import java.security.cert.Certificate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.blackduck.integration.alert.api.certificates.AlertClientCertificateManager;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.component.certificates.web.CertificateTestUtil;
import com.synopsys.integration.alert.database.job.api.ClientCertificateAccessor;
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
    private ClientCertificateAccessor clientCertificateAccessor;
    private final CertificateTestUtil certTestUtil = new CertificateTestUtil();

    @BeforeEach
    void initTest() throws Exception {
        certTestUtil.init(alertProperties);
    }

    @AfterEach
    void cleanup() throws AlertException {
        certTestUtil.cleanup();
        clientCertificateAccessor.deleteConfiguration();
        alertClientCertificateManager.removeCertificate();
    }

    @Test
    void initializeWithClientCertificatePersistedTest() throws Exception {
        ClientCertificateModel clientCertificateModel = clientCertificateAccessor.createConfiguration(certTestUtil.createClientModel());
        assertTrue(alertClientCertificateManager.getClientKeyStore().isEmpty());

        alertClientCertificateManagerStartupComponent.initialize();
        KeyStore clientKeystore = alertClientCertificateManager.getClientKeyStore().orElseThrow(() -> new AssertionError("Keystore missing when it should exist"));
        assertTrue(clientKeystore.containsAlias(AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS));
        Certificate clientCertificate = clientKeystore.getCertificate(AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS);
        Certificate validationCertificate = certTestUtil.loadCertificate(clientCertificateModel.getClientCertificateContent());

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
    void initializeWithoutCertificatePersistedTest() {
        assertTrue(alertClientCertificateManager.getClientKeyStore().isEmpty());
        alertClientCertificateManagerStartupComponent.initialize();
        assertTrue(alertClientCertificateManager.getClientKeyStore().isEmpty());
    }

    @Test
    void certificateManagerThrowsExceptionTest() throws Exception {
        clientCertificateAccessor.createConfiguration(certTestUtil.createClientModel(CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD, CertificateTestUtil.EMPTY_STRING_CONTENT));
        assertTrue(alertClientCertificateManager.getClientKeyStore().isEmpty());

        alertClientCertificateManagerStartupComponent.initialize();
        assertTrue(alertClientCertificateManager.getClientKeyStore().isEmpty());
    }
}
