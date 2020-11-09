package com.synopsys.integration.alert.component.authentication.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.model.ExistenceModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public class SamlMetadataFileUploadTest {
    private final AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
    private final FilePersistenceUtil filePersistenceUtil = Mockito.mock(FilePersistenceUtil.class);
    private Resource testResource;
    private final AuthenticationDescriptorKey descriptorKey = new AuthenticationDescriptorKey();
    private File tempFile;

    @BeforeEach
    public void initResource() throws Exception {
        testResource = Mockito.mock(Resource.class);
        Mockito.when(testResource.getInputStream()).thenReturn(Mockito.mock(InputStream.class));
        tempFile = File.createTempFile("TEST_FILE_NAME", ".xml");
        ClassPathResource classPathResource = new ClassPathResource("saml/testMetadata.xml");
        File jsonFile = classPathResource.getFile();
        String xmlContent = FileUtils.readFileToString(jsonFile, Charset.defaultCharset());
        Files.write(tempFile.toPath(), xmlContent.getBytes());
    }

    @AfterEach
    public void cleanResource() throws Exception {
        FileUtils.forceDelete(tempFile);
    }

    @Test
    public void performUpload() {
        SamlMetaDataFileUpload action = new SamlMetaDataFileUpload(descriptorKey, authorizationManager, filePersistenceUtil);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(tempFile);
        ActionResponse<Void> response = action.uploadFile(testResource);
        assertTrue(response.isSuccessful());
        assertFalse(response.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
    }

    @Test
    public void performUploadWithValidationFail() throws IOException {
        SamlMetaDataFileUpload action = new SamlMetaDataFileUpload(descriptorKey, authorizationManager, filePersistenceUtil);
        tempFile = File.createTempFile("TEST_FILE_NAME_INVALID", ".xml");
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(tempFile);
        ActionResponse<Void> response = action.uploadFile(testResource);
        assertTrue(response.isError());
        assertFalse(response.hasContent());
        assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus());
    }

    @Test
    public void performUploadIOException() throws Exception {
        SamlMetaDataFileUpload action = new SamlMetaDataFileUpload(descriptorKey, authorizationManager, filePersistenceUtil);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.doThrow(IOException.class).when(filePersistenceUtil).writeFileToUploadsDirectory(Mockito.anyString(), Mockito.any(InputStream.class));

        ActionResponse<Void> response = action.uploadFile(testResource);
        assertTrue(response.isError());
        assertFalse(response.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
    }

    @Test
    public void performUploadMissingTarget() {
        SamlMetaDataFileUpload action = new SamlMetaDataFileUpload(descriptorKey, authorizationManager, filePersistenceUtil);
        action.setTarget(null);
        ActionResponse<Void> response = action.uploadFile(testResource);
        assertTrue(response.isError());
        assertFalse(response.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
    }

    @Test
    public void performUploadMissingPermissions() throws Exception {
        SamlMetaDataFileUpload action = new SamlMetaDataFileUpload(descriptorKey, authorizationManager, filePersistenceUtil);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.FALSE);

        ActionResponse<Void> response = action.uploadFile(testResource);
        assertTrue(response.isError());
        assertFalse(response.hasContent());
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }

    @Test
    public void deleteUpload() {
        SamlMetaDataFileUpload action = new SamlMetaDataFileUpload(descriptorKey, authorizationManager, filePersistenceUtil);
        Mockito.when(authorizationManager.hasUploadDeletePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(Mockito.mock(File.class));

        ActionResponse<Void> response = action.deleteFile();
        assertTrue(response.isSuccessful());
        assertFalse(response.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());

    }

    @Test
    public void deleteUploadIOException() throws Exception {
        SamlMetaDataFileUpload action = new SamlMetaDataFileUpload(descriptorKey, authorizationManager, filePersistenceUtil);
        Mockito.when(authorizationManager.hasUploadDeletePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(Mockito.mock(File.class));
        Mockito.doThrow(IOException.class).when(filePersistenceUtil).delete(Mockito.any(File.class));

        ActionResponse<Void> response = action.deleteFile();
        assertTrue(response.isError());
        assertFalse(response.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
    }

    @Test
    public void deleteUploadMissingTarget() {
        SamlMetaDataFileUpload action = new SamlMetaDataFileUpload(descriptorKey, authorizationManager, filePersistenceUtil);
        action.setTarget(null);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(Mockito.mock(File.class));

        ActionResponse<Void> response = action.deleteFile();
        assertTrue(response.isError());
        assertFalse(response.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
    }

    @Test
    public void deleteUploadMissingPermissions() {
        SamlMetaDataFileUpload action = new SamlMetaDataFileUpload(descriptorKey, authorizationManager, filePersistenceUtil);
        Mockito.when(authorizationManager.hasUploadDeletePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.FALSE);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(Mockito.mock(File.class));

        ActionResponse<Void> response = action.deleteFile();
        assertTrue(response.isError());
        assertFalse(response.hasContent());
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }

    @Test
    public void fileExists() {
        SamlMetaDataFileUpload action = new SamlMetaDataFileUpload(descriptorKey, authorizationManager, filePersistenceUtil);
        Mockito.when(authorizationManager.hasUploadReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);

        ActionResponse<ExistenceModel> response = action.uploadFileExists();
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        assertEquals(HttpStatus.OK, response.getHttpStatus());
    }

    @Test
    public void fileExistsMissingTarget() {
        SamlMetaDataFileUpload action = new SamlMetaDataFileUpload(descriptorKey, authorizationManager, filePersistenceUtil);
        action.setTarget(null);
        ActionResponse<ExistenceModel> response = action.uploadFileExists();
        assertTrue(response.isError());
        assertFalse(response.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
    }

    @Test
    public void fileExistsMissingPermissions() {
        SamlMetaDataFileUpload action = new SamlMetaDataFileUpload(descriptorKey, authorizationManager, filePersistenceUtil);
        Mockito.when(authorizationManager.hasUploadReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.FALSE);

        ActionResponse<ExistenceModel> response = action.uploadFileExists();
        assertTrue(response.isError());
        assertFalse(response.hasContent());
        assertEquals(HttpStatus.FORBIDDEN, response.getHttpStatus());
    }
}
