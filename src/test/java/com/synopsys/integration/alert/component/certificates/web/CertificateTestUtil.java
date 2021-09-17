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
import com.synopsys.integration.alert.database.certificates.CustomCertificateRepository;

public class CertificateTestUtil {
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

    protected Optional<CertificateModel> createCertificate(CertificateActions actions) throws AlertException, IOException {
        String certificateContent = readCertificateContents();
        CertificateModel certificateModel = new CertificateModel(TEST_ALIAS, certificateContent, DateUtils.createCurrentDateString(DateUtils.UTC_DATE_FORMAT_TO_MINUTE));
        ActionResponse<CertificateModel> response = actions.create(certificateModel);

        return response.getContent();
    }

    protected String readCertificateContents() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(CERTIFICATE_FILE_PATH);
        File jsonFile = classPathResource.getFile();
        return FileUtils.readFileToString(jsonFile, Charset.defaultCharset());
    }

}
