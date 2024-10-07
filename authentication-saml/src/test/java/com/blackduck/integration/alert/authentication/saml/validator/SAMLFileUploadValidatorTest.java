package com.blackduck.integration.alert.authentication.saml.validator;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.authentication.saml.SAMLTestHelper;
import com.blackduck.integration.alert.common.persistence.util.FilePersistenceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class SAMLFileUploadValidatorTest {
    private SAMLFileUploadValidator samlFileUploadValidator;

    @TempDir
    private Path tempDir;

    private final Resource XML_DATA = new ByteArrayResource("<note></note>".getBytes());

    @BeforeEach
    void init() {
        samlFileUploadValidator = new SAMLFileUploadValidator(
            SAMLTestHelper.createTempDirFilePersistenceUtil(tempDir)
        );
    }

    @Test
    void validateFileIOExceptionHasErrors() throws IOException {
        FilePersistenceUtil mockedFilePersistenceUtil = Mockito.mock(FilePersistenceUtil.class);
        Mockito.doThrow(new IOException())
            .when(mockedFilePersistenceUtil)
            .writeFileToUploadsDirectory(anyString(), any());

        SAMLFileUploadValidator throwingSAMLFileUploadValidator = new SAMLFileUploadValidator(mockedFilePersistenceUtil);
        ValidationResponseModel validationResponseModel = throwingSAMLFileUploadValidator.validateMetadataFile(XML_DATA);
        assertTrue(validationResponseModel.hasErrors());
        assertEquals("Error uploading file to server.", validationResponseModel.getMessage());
    }

    @Test
    void validateMetadataFileHasNoErrors() {
        ValidationResponseModel validationResponseModel = samlFileUploadValidator.validateMetadataFile(XML_DATA);
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    void validateMetadataFileHasErrors() {
        ValidationResponseModel validationResponseModel = samlFileUploadValidator.validateMetadataFile(new ByteArrayResource("malformed".getBytes()));
        assertTrue(validationResponseModel.hasErrors());
        assertTrue(validationResponseModel.getMessage().contains("XML file error"));
    }

    @Test
    void validateCertFileHasNoErrors() {
        ValidationResponseModel validationResponseModel = samlFileUploadValidator.
            validateCertFile(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE, new ByteArrayResource(SAMLTestHelper.TEST_X509_CERT.getBytes()));
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    void validateCertFileHasErrors() {
        ValidationResponseModel validationResponseModel = samlFileUploadValidator.
            validateCertFile(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE, new ByteArrayResource("malformed".getBytes()));
        assertTrue(validationResponseModel.hasErrors());
        assertTrue(validationResponseModel.getMessage().contains("Certificate file error"));
    }

    @Test
    void validatePrivateKeyFileHasNoErrors() {
        ValidationResponseModel validationResponseModel = samlFileUploadValidator.
            validatePrivateKeyFile(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE, new ByteArrayResource(SAMLTestHelper.TEST_PRIVATE_KEY.getBytes()));
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    void validatePrivateKeyFileHasErrors() {
        ValidationResponseModel validationResponseModel = samlFileUploadValidator.
            validatePrivateKeyFile(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE, new ByteArrayResource("malformed".getBytes()));
        assertTrue(validationResponseModel.hasErrors());
        assertTrue(validationResponseModel.getMessage().contains("Key file error"));
    }
}
