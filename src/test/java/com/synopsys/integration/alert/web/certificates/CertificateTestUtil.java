package com.synopsys.integration.alert.web.certificates;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyStore;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.certificates.CustomCertificateRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.model.CertificateModel;

public class CertificateTestUtil extends AlertIntegrationTest {
    public static final String CERTIFICATE_FILE_PATH = "certificates/selfsigned.cert.pem";
    public static final String TEST_ALIAS = "test-alias";
    public static final String TRUSTSTORE_FILE_PATH = "./build/certs/blackduck-alert-test.truststore";
    public static final String TRUSTSTORE_PASSWORD = "changeit";

    protected File trustStoreFile;

    public void init(AlertProperties alertProperties) throws Exception {
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

    public void cleanup(CustomCertificateRepository repository) {
        repository.deleteAll();
        FileUtils.deleteQuietly(trustStoreFile);
    }

    protected CertificateModel createCertificate(CertificateActions actions) throws AlertException, IOException {
        String certificateContent = readCertificateContents();
        CertificateModel certificateModel = new CertificateModel(TEST_ALIAS, certificateContent, DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        CertificateModel savedCertificate = actions.createCertificate(certificateModel);

        return savedCertificate;
    }

    protected String readCertificateContents() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(CERTIFICATE_FILE_PATH);
        File jsonFile = classPathResource.getFile();
        return FileUtils.readFileToString(jsonFile, Charset.defaultCharset());
    }
}
