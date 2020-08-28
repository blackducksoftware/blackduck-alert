package com.synopsys.integration.alert.common.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.UploadValidationFunction;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.ValidationResult;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

public class UploadEndpointManagerTest {
    public static final String TEST_TARGET_KEY = "a.key";
    private static final String TEST_FILE_NAME = "TEST_FILE_NAME";
    private final AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
    private final FilePersistenceUtil filePersistenceUtil = Mockito.mock(FilePersistenceUtil.class);
    private Resource testResource;
    private final DescriptorKey descriptorKey = new DescriptorKey() {
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
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));
    }

    @Test
    public void registerExistingTarget() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));

        assertThrows(AlertException.class, () -> manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME));
    }

    @Test
    public void unRegisterMissingTarget() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);

        assertFalse(manager.containsTarget(TEST_TARGET_KEY));
        assertThrows(AlertException.class, () -> manager.unRegisterTarget(TEST_TARGET_KEY));
    }

    @Test
    public void unRegisterTarget() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));
        manager.unRegisterTarget(TEST_TARGET_KEY);
        assertFalse(manager.containsTarget(TEST_TARGET_KEY));
    }

    @Test
    public void performUpload() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));

        manager.performUpload(TEST_TARGET_KEY, testResource);
    }

    @Test
    public void performUploadWithValidationSuccess() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

        UploadValidationFunction validationFunction = (file) -> ValidationResult.success();
        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME, validationFunction);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));

        manager.performUpload(TEST_TARGET_KEY, testResource);
    }

    @Test
    public void performUploadWithValidationFail() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

        UploadValidationFunction validationFunction = (file) -> ValidationResult.errors("validation error");
        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME, validationFunction);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));

        callEndpointAndAssertException(HttpStatus.BAD_REQUEST, () -> manager.performUpload(TEST_TARGET_KEY, testResource));
    }

    @Test
    public void performUploadIOException() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.doThrow(IOException.class).when(filePersistenceUtil).writeFileToUploadsDirectory(Mockito.anyString(), Mockito.any(InputStream.class));

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));

        callEndpointAndAssertException(HttpStatus.INTERNAL_SERVER_ERROR, () -> manager.performUpload(TEST_TARGET_KEY, testResource));
    }

    @Test
    public void performUploadMissingTarget() {
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);

        assertFalse(manager.containsTarget(TEST_TARGET_KEY));
        callEndpointAndAssertException(HttpStatus.NOT_IMPLEMENTED, () -> manager.performUpload(TEST_TARGET_KEY, testResource));
    }

    @Test
    public void performUploadMissingPermissions() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);
        Mockito.when(authorizationManager.hasUploadWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.FALSE);

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));

        callEndpointAndAssertException(HttpStatus.FORBIDDEN, () -> manager.performUpload(TEST_TARGET_KEY, testResource));
    }

    @Test
    public void deleteUpload() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);
        Mockito.when(authorizationManager.hasUploadDeletePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(Mockito.mock(File.class));

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));

        manager.deleteUploadedFile(TEST_TARGET_KEY);

    }

    @Test
    public void deleteUploadIOException() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);
        Mockito.when(authorizationManager.hasUploadDeletePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(Mockito.mock(File.class));
        Mockito.doThrow(IOException.class).when(filePersistenceUtil).delete(Mockito.any(File.class));

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));

        callEndpointAndAssertException(HttpStatus.INTERNAL_SERVER_ERROR, () -> manager.deleteUploadedFile(TEST_TARGET_KEY));
    }

    @Test
    public void deleteUploadMissingTarget() {
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(Mockito.mock(File.class));

        assertFalse(manager.containsTarget(TEST_TARGET_KEY));
        callEndpointAndAssertException(HttpStatus.NOT_IMPLEMENTED, () -> manager.deleteUploadedFile(TEST_TARGET_KEY));
    }

    @Test
    public void deleteUploadMissingPermissions() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);
        Mockito.when(authorizationManager.hasUploadDeletePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.FALSE);
        Mockito.when(filePersistenceUtil.createUploadsFile(Mockito.anyString())).thenReturn(Mockito.mock(File.class));

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));

        callEndpointAndAssertException(HttpStatus.FORBIDDEN, () -> manager.deleteUploadedFile(TEST_TARGET_KEY));
    }

    @Test
    public void fileExists() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);
        Mockito.when(authorizationManager.hasUploadReadPermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));

        manager.checkExists(TEST_TARGET_KEY);
    }

    @Test
    public void fileExistsMissingTarget() {
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);

        assertFalse(manager.containsTarget(TEST_TARGET_KEY));
        callEndpointAndAssertException(HttpStatus.NOT_IMPLEMENTED, () -> manager.checkExists(TEST_TARGET_KEY));
    }

    @Test
    public void fileExistsMissingPermissions() throws Exception {
        UploadEndpointManager manager = new UploadEndpointManager(filePersistenceUtil, authorizationManager);
        Mockito.when(authorizationManager.hasUploadReadPermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.FALSE);

        manager.registerTarget(TEST_TARGET_KEY, ConfigContextEnum.GLOBAL, descriptorKey, TEST_FILE_NAME);
        assertTrue(manager.containsTarget(TEST_TARGET_KEY));

        callEndpointAndAssertException(HttpStatus.FORBIDDEN, () -> manager.checkExists(TEST_TARGET_KEY));
    }

    private void callEndpointAndAssertException(HttpStatus expectedStatus, Runnable endpointFunction) {
        try {
            endpointFunction.run();
            fail("Expected a ResponseStatusException to be thrown");
        } catch (ResponseStatusException responseStatusException) {
            assertEquals(expectedStatus, responseStatusException.getStatus());
        }
    }

}
