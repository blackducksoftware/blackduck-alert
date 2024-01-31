package com.synopsys.integration.alert.component.certificates.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyStore;
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
    public static final String CERTIFICATE_MTLS_CLIENT_FILE_PATH = "certificates/mtls/client.pem";
    public static final String KEY_MTLS_CLIENT_FILE_PATH = "certificates/mtls/client.key";
    public static final String TEST_ALIAS = "test-alias";
    public static final String TRUSTSTORE_FILE_PATH = "./build/certs/blackduck-alert-test.truststore";
    public static final String TRUSTSTORE_PASSWORD = "changeit";

    public static final String MTLS_KEYSTORE_FILE_PATH = "./build/certs/mtls-client-test.keystore";
    public static final String MTLS_CERTIFICATE_PASSWORD = "changeit";
    public static final String MTLS_HOST_NAME_ROOT_CA = "root-ca-cert-test.xxx.blackduck.alert.example.com";
    public static final String MTLS_HOST_NAME_GATEWAY = "gateway-cert-test.xxx.blackduck.alert.example.com";
    public static final String MTLS_HOST_NAME_CLIENT = "client-cert-test.xxx.blackduck.alert.example.com";

    protected File trustStoreFile;
    protected File mtlsKeyStoreFile;

    public void init(AlertProperties alertProperties) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        trustStoreFile = createKeyStore(TRUSTSTORE_FILE_PATH, KeyStore.getDefaultType());
        mtlsKeyStoreFile = createKeyStore(MTLS_KEYSTORE_FILE_PATH, "PKCS12");
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
        FileUtils.deleteQuietly(mtlsKeyStoreFile);
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

}
