/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.saml.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.blackduck.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.authentication.saml.SAMLTestHelper;
import com.blackduck.integration.alert.authentication.saml.validator.SAMLFileUploadValidator;

@ExtendWith(SpringExtension.class)
class SAMLFileUploadActionsTest {
    private SAMLFileUploadActions samlFileUploadActions;
    @Mock
    private SAMLFileUploadValidator samlFileUploadValidator;

    @TempDir
    private Path tempDir;
    private final Resource FILE_DATA = new ByteArrayResource("Dummy data".getBytes());

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
    void fileExistsReturnsNotFoundForNonExistingFile() {
        assertEquals(HttpStatus.NOT_FOUND, samlFileUploadActions.fileExists("Dummy_file.xml").getHttpStatus());
    }

    @Test
    void metadataFileExistsAfterUpload() {
        assertEquals(HttpStatus.NOT_FOUND, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_METADATA_FILE).getHttpStatus());

        samlFileUploadActions.metadataFileUpload(FILE_DATA);
        assertEquals(HttpStatus.NO_CONTENT, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_METADATA_FILE).getHttpStatus());
    }

    @Test
    void certFilesExistsAfterUpload() {
        assertEquals(HttpStatus.NOT_FOUND, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE).getHttpStatus());

        samlFileUploadActions.certFileUpload(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE, FILE_DATA);
        assertEquals(HttpStatus.NO_CONTENT, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_CERT_FILE).getHttpStatus());
    }

    @Test
    void privateKeyFileExistsAfterUpload() {
        assertEquals(HttpStatus.NOT_FOUND, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE).getHttpStatus());

        samlFileUploadActions.privateKeyFileUpload(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE, FILE_DATA);
        assertEquals(HttpStatus.NO_CONTENT, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE).getHttpStatus());
    }

    @Test
    void fileNotFoundAfterDelete() {
        // Upload the file
        assertEquals(HttpStatus.NOT_FOUND, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE).getHttpStatus());

        samlFileUploadActions.privateKeyFileUpload(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE, FILE_DATA);
        assertEquals(HttpStatus.NO_CONTENT, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE).getHttpStatus());

        // Delete the file and verify not found
        samlFileUploadActions.fileDelete(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE);
        assertEquals(HttpStatus.NOT_FOUND, samlFileUploadActions.fileExists(AuthenticationDescriptor.SAML_ENCRYPTION_PRIVATE_KEY_FILE).getHttpStatus());
    }
}
