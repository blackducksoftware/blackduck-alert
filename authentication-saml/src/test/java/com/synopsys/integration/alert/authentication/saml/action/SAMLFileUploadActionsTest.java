package com.synopsys.integration.alert.authentication.saml.action;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.saml.SAMLTestHelper;
import com.synopsys.integration.alert.authentication.saml.validator.SAMLFileUploadValidator;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class SAMLFileUploadActionsTest {
    private SAMLFileUploadActions samlFileUploadActions;

    @Mock
    private SAMLFileUploadValidator samlFileUploadValidator;
    @TempDir
    private Path tempDir;

    @BeforeEach
    void init() {
        samlFileUploadActions = new SAMLFileUploadActions(
            SAMLTestHelper.createAuthorizationManager(),
            samlFileUploadValidator,
            new AuthenticationDescriptorKey(),
            SAMLTestHelper.createTempDirFilePersistenceUtil(tempDir)
        );

        when(samlFileUploadValidator.validateMetadataFile(any(Resource.class))).thenReturn(ValidationResponseModel.success());
        when(samlFileUploadValidator.validateCertFile(anyString(), any(Resource.class))).thenReturn(ValidationResponseModel.success());
        when(samlFileUploadValidator.validatePrivateKeyFile(anyString(), any(Resource.class))).thenReturn(ValidationResponseModel.success());
    }

    @Test
    void fileExistsReturnsNotFoundOnNonExistingFile() {
        assertEquals(HttpStatus.NOT_FOUND, samlFileUploadActions.fileExists("Dummy_file.xml").getHttpStatus());
    }

    @Test
    void metadataFileExistsAfterUpload() {
        assertEquals(HttpStatus.NOT_FOUND, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_METADATA_FILE).getHttpStatus());

        samlFileUploadActions.metadataFileUpload(new ByteArrayResource("Dummy data".getBytes()));
        assertEquals(HttpStatus.NO_CONTENT, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_METADATA_FILE).getHttpStatus());
    }

    @Test
    void certFilesExistsAfterUpload() {
        assertEquals(HttpStatus.NOT_FOUND, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE).getHttpStatus());

        samlFileUploadActions.certFileUpload(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE, new ByteArrayResource("Dummy data".getBytes()));
        assertEquals(HttpStatus.NO_CONTENT, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE).getHttpStatus());
    }

    @Test
    void privateKeyFileExistsAfterUpload() {
        assertEquals(HttpStatus.NOT_FOUND, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE).getHttpStatus());

        samlFileUploadActions.privateKeyFileUpload(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE, new ByteArrayResource("Dummy data".getBytes()));
        assertEquals(HttpStatus.NO_CONTENT, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE).getHttpStatus());
    }

    @Test
    void fileNotFoundAfterDelete() {
        // Upload the file
        assertEquals(HttpStatus.NOT_FOUND, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE).getHttpStatus());

        samlFileUploadActions.privateKeyFileUpload(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE, new ByteArrayResource("Dummy data".getBytes()));
        assertEquals(HttpStatus.NO_CONTENT, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE).getHttpStatus());

        // Delete the file and verify not found
        samlFileUploadActions.fileDelete(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE);
        assertEquals(HttpStatus.NOT_FOUND, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE).getHttpStatus());
    }
}
