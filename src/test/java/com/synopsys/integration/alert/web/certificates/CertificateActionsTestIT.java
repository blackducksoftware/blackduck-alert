package com.synopsys.integration.alert.web.certificates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.certificates.CustomCertificateRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.model.CertificateModel;

public class CertificateActionsTestIT extends AlertIntegrationTest {
    private static final String CERTIFICATE_FILE_PATH = "certificates/selfsigned.cert.pem";
    private static final String TEST_ALIAS = "test-alias";
    @Autowired
    private CustomCertificateRepository customCertificateRepository;

    @Autowired
    private CertificateActions certificateActions;

    @BeforeEach
    public void cleanUpDatabase() {
        customCertificateRepository.deleteAll();
    }

    @Test
    public void readAllEmptyListTest() {
        assertTrue(certificateActions.readCertificates().isEmpty());
    }

    @Test
    public void createCertificateTest() throws Exception {
        String certificateContent = readCertificateContents();
        CertificateModel savedCertificate = createCertificate();

        assertNotNull(savedCertificate.getId());
        assertEquals(TEST_ALIAS, savedCertificate.getAlias());
        assertEquals(certificateContent, savedCertificate.getCertificateContent());
    }

    @Test
    public void readAllTest() throws Exception {
        createCertificate();
        assertEquals(1, certificateActions.readCertificates().size());
    }

    @Test
    public void readSingleCertificateTest() throws Exception {
        CertificateModel expectedCertificate = createCertificate();
        Optional<CertificateModel> actualCertificate = certificateActions.readCertificate(Long.valueOf(expectedCertificate.getId()));
        assertTrue(actualCertificate.isPresent());
        assertEquals(expectedCertificate, actualCertificate);
    }

    @Test
    public void updateCertificateTest() throws Exception {
        String certificateContent = readCertificateContents();
        CertificateModel savedCertificate = createCertificate();

        String updatedAlias = "updated-alias";
        String newCertificateContent = StringUtils.replaceChars(certificateContent, 'A', 'B');
        CertificateModel newModel = new CertificateModel(savedCertificate.getId(), updatedAlias, newCertificateContent);
        Optional<CertificateModel> updatedCertificate = certificateActions.updateCertificate(Long.valueOf(savedCertificate.getId()), newModel);
        assertTrue(updatedCertificate.isPresent());

        CertificateModel updatedModel = updatedCertificate.get();
        assertEquals(savedCertificate.getId(), updatedModel.getId());
        assertEquals(updatedAlias, updatedModel.getAlias());
        assertEquals(newCertificateContent, updatedModel.getCertificateContent());
    }

    @Test
    public void deleteCertificateTest() throws Exception {
        CertificateModel savedCertificate = createCertificate();
        certificateActions.deleteCertificate(Long.valueOf(savedCertificate.getId()));
        assertTrue(customCertificateRepository.findAll().isEmpty());
    }

    private CertificateModel createCertificate() throws AlertException, IOException {
        String certificateContent = readCertificateContents();
        CertificateModel certificateModel = new CertificateModel(TEST_ALIAS, certificateContent);
        CertificateModel savedCertificate = certificateActions.createCertificate(certificateModel);

        return savedCertificate;
    }

    private String readCertificateContents() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(CERTIFICATE_FILE_PATH);
        File jsonFile = classPathResource.getFile();
        return FileUtils.readFileToString(jsonFile, Charset.defaultCharset());
    }
}
