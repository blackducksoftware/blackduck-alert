package com.synopsys.integration.alert.common.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.UploadValidationFunction;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.ValidationResult;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

public class UploadEndpointManagerTest {

    public static final String TEST_TARGET_KEY = "a.key";
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

        @Override
        public String getDisplayName() {
            return "descriptorName";
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

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));
    }

    @Test
    public void registerExistingTarget() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));

        assertThrows(AlertException.class, () -> manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME));
    }

    @Test
    public void unRegisterMissingTarget() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);

        assertFalse(manager.containsTarget(TEST_TARGET_KEY));
        assertThrows(AlertException.class, () -> manager.unRegisterTarget(TEST_TARGET_KEY));
    }

    @Test
    public void unRegisterTarget() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));
        manager.unRegisterTarget(TEST_TARGET_KEY);
        assertFalse(manager.containsTarget(TEST_TARGET_KEY));
    }

    @Test
    public void performUpload() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));
        ResponseEntity<String> response = manager.performUpload(TEST_TARGET_KEY, testResource);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void performUploadWithValidationSuccess() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

        UploadValidationFunction validationFunction = (file) -> ValidationResult.of();
        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME, validationFunction);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));
        ResponseEntity<String> response = manager.performUpload(TEST_TARGET_KEY, testResource);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void performUploadWithValidationFail() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

        UploadValidationFunction validationFunction = (file) -> ValidationResult.of("validation error");
        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME, validationFunction);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));
        ResponseEntity<String> response = manager.performUpload(TEST_TARGET_KEY, testResource);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void performUploadIOException() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.doThrow(IOException.class).when(filePersistenceUtil).writeFileToUploadsDirectory(Mockito.anyString(), Mockito.any(InputStream.class));

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));
        ResponseEntity<String> response = manager.performUpload(TEST_TARGET_KEY, testResource);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void performUploadMissingTarget() {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);

        assertFalse(manager.containsTarget(TEST_TARGET_KEY));
        ResponseEntity<String> response = manager.performUpload(TEST_TARGET_KEY, testResource);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    public void performUploadMissingPermissions() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.FALSE);

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        ResponseEntity<String> response = manager.performUpload(TEST_TARGET_KEY, testResource);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void deleteUpload() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadDeletePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(Mockito.mock(File.class));

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));
        ResponseEntity<String> response = manager.deleteUploadedFile(TEST_TARGET_KEY);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void deleteUploadIOException() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadDeletePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(Mockito.mock(File.class));
        Mockito.doThrow(IOException.class).when(filePersistenceUtil).delete(Mockito.any(File.class));

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));
        ResponseEntity<String> response = manager.deleteUploadedFile(TEST_TARGET_KEY);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void deleteUploadMissingTarget() {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(Mockito.mock(File.class));

        assertFalse(manager.containsTarget(TEST_TARGET_KEY));
        ResponseEntity<String> response = manager.deleteUploadedFile(TEST_TARGET_KEY);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    public void deleteUploadMissingPermissions() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadDeletePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.FALSE);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(Mockito.mock(File.class));

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        ResponseEntity<String> response = manager.deleteUploadedFile(TEST_TARGET_KEY);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void fileExists() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadReadPermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));
        ResponseEntity<String> response = manager.checkExists(TEST_TARGET_KEY);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void fileExistsMissingTarget() {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);

        assertFalse(manager.containsTarget(TEST_TARGET_KEY));
        ResponseEntity<String> response = manager.checkExists(TEST_TARGET_KEY);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, response.getStatusCode());
    }

    @Test
    public void fileExistsMissingPermissions() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(gson, filePersistenceUtil, authorizationManager, responseFactory);
        Mockito.when(authorizationManager.hasUploadReadPermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.FALSE);
        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        ResponseEntity<String> response = manager.checkExists(TEST_TARGET_KEY);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
}
