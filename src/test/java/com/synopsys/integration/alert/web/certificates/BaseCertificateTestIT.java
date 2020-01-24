package com.synopsys.integration.alert.web.certificates;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyStore;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.certificates.CustomCertificateRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.model.CertificateModel;

@Transactional
@TestPropertySource(locations = "classpath:certificates/spring-certificate-test.properties")
public class BaseCertificateTestIT extends AlertIntegrationTest {
    public static final String CERTIFICATE_FILE_PATH = "certificates/selfsigned.cert.pem";
    public static final String TEST_ALIAS = "test-alias";
    public static final String TRUSTSTORE_FILE_PATH = "./build/certs/blackduck-alert-test.truststore";
    public static final String TRUSTSTORE_PASSWORD = "changeit";

    @Autowired
    protected CustomCertificateRepository customCertificateRepository;

    @Autowired
    protected CertificateActions certificateActions;

    @Autowired
    protected AlertProperties alertProperties;

    protected File trustStoreFile;

    @BeforeEach
    public void init() throws Exception {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        trustStoreFile = new File(TRUSTSTORE_FILE_PATH);
        trustStoreFile.getParentFile().mkdirs();
        trustStoreFile.createNewFile();
        System.out.println(String.format("Trust store file path: %s", trustStoreFile.getAbsolutePath()));
        FileOutputStream outputStream = new FileOutputStream(trustStoreFile);
        trustStore.store(outputStream, TRUSTSTORE_PASSWORD.toCharArray());
        outputStream.close();
        alertProperties.getTrustStoreFile().ifPresent(file -> System.out.println(String.format("Alert Properties trust store file %s", file)));
    }

    @AfterEach
    public void cleanup() {
        customCertificateRepository.deleteAll();
        FileUtils.deleteQuietly(trustStoreFile);
    }

    protected CertificateModel createCertificate() throws AlertException, IOException {
        String certificateContent = readCertificateContents();
        CertificateModel certificateModel = new CertificateModel(TEST_ALIAS, certificateContent);
        CertificateModel savedCertificate = certificateActions.createCertificate(certificateModel);

        return savedCertificate;
    }

    protected String readCertificateContents() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(CERTIFICATE_FILE_PATH);
        File jsonFile = classPathResource.getFile();
        return FileUtils.readFileToString(jsonFile, Charset.defaultCharset());
    }
}
