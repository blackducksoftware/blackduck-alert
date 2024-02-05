package com.synopsys.integration.alert.component.certificates.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.util.DateUtils;

import io.micrometer.common.util.StringUtils;

public class CertificateTestUtil {
    public static final String CERTIFICATE_FILE_PATH = "certificates/selfsigned.cert.pem";
    // Hostnames used in test certificates for mTLS
    // rootCA.pem = "root-ca-cert-test.xxx.blackduck.alert.example.com"
    // server.pem = "server-cert-test.xxx.blackduck.alert.example.com"
    // client.pem = "client-cert-test.xxx.blackduck.alert.example.com"
    public static final String CERTIFICATE_MTLS_CLIENT_FILE_PATH = "certificates/mtls/client.pem";
    public static final String KEY_MTLS_CLIENT_FILE_PATH = "certificates/mtls/client.key";
    public static final String TEST_ALIAS = "test-alias";
    public static final String TRUSTSTORE_FILE_PATH = "./build/certs/blackduck-alert-test.truststore";
    public static final String TRUSTSTORE_PASSWORD = "changeit";
    public static final String MTLS_CERTIFICATE_PASSWORD = "changeit";

    protected File trustStoreFile;

    public void init(AlertProperties alertProperties) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        trustStoreFile = createKeyStore(TRUSTSTORE_FILE_PATH, KeyStore.getDefaultType());
        alertProperties.getTrustStoreFile().ifPresent(file -> System.out.printf("Alert Properties trust store file %s%n", file));
    }

    private File createKeyStore(String filePath, String keyStoreType) throws Exception {
        String storeType = StringUtils.isNotBlank(keyStoreType) ? keyStoreType : KeyStore.getDefaultType();
        KeyStore trustStore = KeyStore.getInstance(storeType);
        trustStore.load(null, null);
        File keyStoreFile = new File(filePath);
        keyStoreFile.getParentFile().mkdirs();
        keyStoreFile.createNewFile();
        System.out.printf("Keys store file path: %s%n", keyStoreFile.getAbsolutePath());
        FileOutputStream outputStream = new FileOutputStream(keyStoreFile);
        trustStore.store(outputStream, TRUSTSTORE_PASSWORD.toCharArray());
        outputStream.close();
        return keyStoreFile;
    }

    public void cleanup() {
        FileUtils.deleteQuietly(trustStoreFile);
    }

    protected Optional<CertificateModel> createCertificate(CertificateActions actions) throws AlertException, IOException {
        String certificateContent = readCertificateOrKeyContents(CertificateTestUtil.CERTIFICATE_FILE_PATH);
        CertificateModel certificateModel = new CertificateModel(TEST_ALIAS, certificateContent, DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        ActionResponse<CertificateModel> response = actions.create(certificateModel);

        return response.getContent();
    }

    public String readCertificateOrKeyContents(String certificateFilePath) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(certificateFilePath);
        File certificateFile = classPathResource.getFile();
        return FileUtils.readFileToString(certificateFile, Charset.defaultCharset());
    }

    public Certificate loadCertificate(String certificateContent) throws AlertException {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

            try (ByteArrayInputStream certInputStream = new ByteArrayInputStream(certificateContent.getBytes())) {
                return certFactory.generateCertificate(certInputStream);
            }
        } catch (CertificateException | IOException e) {
            throw new AlertException("The custom certificate could not be read.", e);
        }
    }

}
