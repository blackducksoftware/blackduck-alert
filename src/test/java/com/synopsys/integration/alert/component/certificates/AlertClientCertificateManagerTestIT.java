package com.synopsys.integration.alert.component.certificates;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateKeyModel;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.certificates.web.CertificateTestUtil;
import com.synopsys.integration.alert.database.api.DefaultClientCertificateAccessor;
import com.synopsys.integration.alert.database.api.DefaultClientCertificateKeyAccessor;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
@TestPropertySource(locations = "classpath:certificates/spring-certificate-test.properties")
class AlertClientCertificateManagerTestIT {
    @Autowired
    private AlertProperties alertProperties;
    @Autowired
    private DefaultClientCertificateAccessor clientCertificateAccessor;
    @Autowired
    private DefaultClientCertificateKeyAccessor clientCertificateKeyAccessor;
    private AlertClientCertificateManager clientCertificateManager;
    private final CertificateTestUtil certTestUtil = new CertificateTestUtil();

    @BeforeEach
    void initTest() throws Exception {
        certTestUtil.init(alertProperties);
    }

    @AfterEach
    void cleanup() {
        clientCertificateKeyAccessor.deleteConfiguration();
        clientCertificateAccessor.deleteConfiguration();
        certTestUtil.cleanup();
    }

    private ClientCertificateKeyModel createClientKeyModel() throws AlertConfigurationException, IOException {
        UUID id = UUID.randomUUID();
        String content = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.KEY_MTLS_CLIENT_FILE_PATH);
        ClientCertificateKeyModel keyModel = new ClientCertificateKeyModel(
            id,
            CertificateTestUtil.MTLS_CERTIFICATE_PASSWORD,
            false,
            content,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );

        return clientCertificateKeyAccessor.createConfiguration(keyModel);
    }

    private ClientCertificateModel createClientModel(ClientCertificateKeyModel keyModel) throws AlertConfigurationException, IOException {
        UUID id = UUID.randomUUID();
        String content = certTestUtil.readCertificateOrKeyContents(CertificateTestUtil.CERTIFICATE_MTLS_CLIENT_FILE_PATH);
        ClientCertificateModel certificateModel = new ClientCertificateModel(
            id,
            keyModel.getId(),
            content,
            DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE)
        );

        return clientCertificateAccessor.createConfiguration(certificateModel);
    }

    @Test
    void importCertificateTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager(alertProperties);
        ClientCertificateKeyModel keyModel = createClientKeyModel();
        ClientCertificateModel model = createClientModel(keyModel);
        clientCertificateManager.importCertificate(model);
        File keystoreFile = clientCertificateManager.getAndValidateKeyStoreFile();
        assertTrue(keystoreFile.exists());
    }

    @Test
    void removeCertificateTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager(alertProperties);
        ClientCertificateKeyModel keyModel = createClientKeyModel();
        ClientCertificateModel model = createClientModel(keyModel);
        clientCertificateManager.importCertificate(model);
        clientCertificateManager.removeCertificate();
        File keystoreFile = clientCertificateManager.getAndValidateKeyStoreFile();
        assertTrue(keystoreFile.exists());
    }

    @Test
    void getCertificateFileTest() throws Exception {
        clientCertificateManager = new AlertClientCertificateManager(alertProperties);
        ClientCertificateKeyModel keyModel = createClientKeyModel();
        ClientCertificateModel model = createClientModel(keyModel);
        clientCertificateManager.importCertificate(model);
        File keystoreFile = clientCertificateManager.getAndValidateKeyStoreFile();
        assertTrue(keystoreFile.exists());
    }

}
