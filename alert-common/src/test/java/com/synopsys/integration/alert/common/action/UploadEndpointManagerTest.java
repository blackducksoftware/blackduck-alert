package com.synopsys.integration.alert.common.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.config.field.UploadValidationFunction;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

public class UploadEndpointManagerTest {

    private static final String TEST_FILE_NAME = "TEST_FILE_NAME";

    private Gson gson = new Gson();
    private ResponseFactory responseFactory = new ResponseFactory();
    private AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
    private FilePersistenceUtil filePersistenceUtil = Mockito.mock(FilePersistenceUtil.class);
    private Resource testResource;
    private DescriptorKey descriptorKey = new DescriptorKey() {
        @Override
        public String getUniversalKey() {
            return "descriptor_universal_key";
        }
    };

    @BeforeEach
    public void initResource() throws Exception {
        testResource = Mockito.mock(Resource.class);
        Mockito.when(testResource.getInputStream()).thenReturn(Mockito.mock(InputStream.class));
    }

    @Test
    public void registerTarget() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        String targetKey = "a.key";
        manager.registerTarget(targetKey, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(targetKey));
    }

    @Test
    public void registerExistingTarget() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        String targetKey = "a.key";
        manager.registerTarget(targetKey, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(targetKey));

        assertThrows(AlertException.class, () -> manager.registerTarget(targetKey, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME));
    }

    @Test
    public void unRegisterMissingTarget() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        String targetKey = "a.key";
        assertFalse(manager.containsTarget(targetKey));
        assertThrows(AlertException.class, () -> manager.unRegisterTarget(targetKey));
    }

    @Test
    public void unRegisterTarget() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        String targetKey = "a.key";
        manager.registerTarget(targetKey, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(targetKey));
        manager.unRegisterTarget(targetKey);
        assertFalse(manager.containsTarget(targetKey));
    }

    @Test
    public void performUpload() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        String targetKey = "a.key";
        manager.registerTarget(targetKey, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(targetKey));
        ResponseEntity<String> response = manager.performUpload(targetKey, testResource);
        assertEquals(response.getStatusCode(), HttpStatus.CREATED);
    }

    @Test
    public void performUploadWithValidationSuccess() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        String targetKey = "a.key";
        UploadValidationFunction validationFunction = (file) -> List.of();
        manager.registerTarget(targetKey, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME, validationFunction);
        assertTrue(manager.containsTarget(targetKey));
        ResponseEntity<String> response = manager.performUpload(targetKey, testResource);
        assertEquals(response.getStatusCode(), HttpStatus.CREATED);
    }

    @Test
    public void performUploadWithValidationFail() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        String targetKey = "a.key";
        UploadValidationFunction validationFunction = (file) -> List.of("validation error");
        manager.registerTarget(targetKey, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME, validationFunction);
        assertTrue(manager.containsTarget(targetKey));
        ResponseEntity<String> response = manager.performUpload(targetKey, testResource);
        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void performUploadIOException() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.doThrow(IOException.class).when(filePersistenceUtil).writeFileToUploadsDirectory(Mockito.anyString(), Mockito.any(InputStream.class));
        String targetKey = "a.key";
        manager.registerTarget(targetKey, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(targetKey));
        ResponseEntity<String> response = manager.performUpload(targetKey, testResource);
        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void performUploadMissingTarget() {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        String targetKey = "a.key";
        assertFalse(manager.containsTarget(targetKey));
        ResponseEntity<String> response = manager.performUpload(targetKey, testResource);
        assertEquals(response.getStatusCode(), HttpStatus.NOT_IMPLEMENTED);
    }

    @Test
    public void performUploadMissingPermissions() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.FALSE);
        String targetKey = "a.key";
        manager.registerTarget(targetKey, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        ResponseEntity<String> response = manager.performUpload(targetKey, testResource);
        assertTrue(manager.containsTarget(targetKey));
        assertEquals(response.getStatusCode(), HttpStatus.FORBIDDEN);
    }

    @Test
    public void deleteUpload() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadDeletePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(Mockito.mock(File.class));
        String targetKey = "a.key";
        manager.registerTarget(targetKey, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(targetKey));
        ResponseEntity<String> response = manager.deleteUploadedFile(targetKey);
        assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
    }

    @Test
    public void deleteUploadIOException() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadDeletePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(Mockito.mock(File.class));
        Mockito.doThrow(IOException.class).when(filePersistenceUtil).delete(Mockito.any(File.class));
        String targetKey = "a.key";
        manager.registerTarget(targetKey, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(targetKey));
        ResponseEntity<String> response = manager.deleteUploadedFile(targetKey);
        assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void deleteUploadMissingTarget() {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(Mockito.mock(File.class));
        String targetKey = "a.key";
        assertFalse(manager.containsTarget(targetKey));
        ResponseEntity<String> response = manager.deleteUploadedFile(targetKey);
        assertEquals(response.getStatusCode(), HttpStatus.NOT_IMPLEMENTED);
    }

    @Test
    public void deleteUploadMissingPermissions() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadDeletePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.FALSE);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(Mockito.mock(File.class));
        String targetKey = "a.key";
        manager.registerTarget(targetKey, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        ResponseEntity<String> response = manager.deleteUploadedFile(targetKey);
        assertTrue(manager.containsTarget(targetKey));
        assertEquals(response.getStatusCode(), HttpStatus.FORBIDDEN);
    }

    @Test
    public void fileExists() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadReadPermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        String targetKey = "a.key";
        manager.registerTarget(targetKey, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(targetKey));
        ResponseEntity<String> response = manager.checkExists(targetKey);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void fileExistsMissingTarget() {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        String targetKey = "a.key";
        assertFalse(manager.containsTarget(targetKey));
        ResponseEntity<String> response = manager.checkExists(targetKey);
        assertEquals(response.getStatusCode(), HttpStatus.NOT_IMPLEMENTED);
    }

    @Test
    public void fileExistsMissingPermissions() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadReadPermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.FALSE);
        String targetKey = "a.key";
        manager.registerTarget(targetKey, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        ResponseEntity<String> response = manager.checkExists(targetKey);
        assertTrue(manager.containsTarget(targetKey));
        assertEquals(response.getStatusCode(), HttpStatus.FORBIDDEN);
    }
}
