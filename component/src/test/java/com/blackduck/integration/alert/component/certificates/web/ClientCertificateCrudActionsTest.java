/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.certificates.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.blackduck.integration.alert.api.certificates.AlertClientCertificateManager;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.model.ClientCertificateModel;
import com.blackduck.integration.alert.common.persistence.model.PermissionKey;
import com.blackduck.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.blackduck.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.blackduck.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.component.certificates.CertificatesDescriptorKey;
import com.blackduck.integration.alert.database.job.api.ClientCertificateAccessor;
import com.blackduck.integration.alert.test.common.AuthenticationTestUtils;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

@ExtendWith(SpringExtension.class)
class ClientCertificateCrudActionsTest {
    private ClientCertificateCrudActions crudActions;
    private ClientCertificateModel model;

    @Mock
    private AlertClientCertificateManager certificateManager;

    @BeforeEach
    void init() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        CertificatesDescriptorKey certificatesDescriptorKey = new CertificatesDescriptorKey();
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), certificatesDescriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
        AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet(
            "admin",
            "admin",
            () -> new PermissionMatrixModel(permissions)
        );
        AlertProperties alertProperties = new MockAlertProperties();
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, BlackDuckServicesFactory.createDefaultGson());
        EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);

        crudActions = new ClientCertificateCrudActions(
            certificateManager,
            authorizationManager,
            certificatesDescriptorKey,
            new ClientCertificateAccessor(encryptionUtility, new MockClientCertificateKeyRepository(), new MockClientCertificateRepository()),
            new ClientCertificateConfigurationValidator(certificateManager)
        );
        model = new ClientCertificateModel("key_password", "key_content", "certificate_content");
    }

    @Test
    void createConfig() throws AlertException {
        ActionResponse<ClientCertificateModel> actionResponseGetOne = crudActions.getOne();
        assertFalse(actionResponseGetOne.isSuccessful());
        assertEquals(HttpStatus.NOT_FOUND, actionResponseGetOne.getHttpStatus());
        assertFalse(actionResponseGetOne.hasContent());

        Mockito.when(certificateManager.validateCertificate(model)).thenReturn(true);
        ActionResponse<ClientCertificateModel> actionResponseCreate = crudActions.create(model);
        assertTrue(actionResponseCreate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseCreate.getHttpStatus());
        assertTrue(actionResponseCreate.hasContent());

        Mockito.verify(certificateManager).importCertificate(model);
    }

    @Test
    void createConfigWithValidationFailures() {
        ActionResponse<ClientCertificateModel> actionResponseGetOne = crudActions.getOne();
        assertFalse(actionResponseGetOne.isSuccessful());
        assertEquals(HttpStatus.NOT_FOUND, actionResponseGetOne.getHttpStatus());
        assertFalse(actionResponseGetOne.hasContent());

        Mockito.when(certificateManager.validateCertificate(model)).thenReturn(false);
        ActionResponse<ClientCertificateModel> actionResponseCreate = crudActions.create(model);
        assertFalse(actionResponseCreate.isSuccessful());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponseCreate.getHttpStatus());
        assertFalse(actionResponseCreate.hasContent());
    }

    @Test
    void deleteNonExistingConfigReturnsNotFound() {
        ActionResponse<ClientCertificateModel> actionResponseDelete = crudActions.delete();
        assertFalse(actionResponseDelete.isSuccessful());
        assertEquals(HttpStatus.NOT_FOUND, actionResponseDelete.getHttpStatus());
        assertFalse(actionResponseDelete.hasContent());
    }

    @Test
    void deleteExistingConfig() throws AlertException {
        Mockito.when(certificateManager.validateCertificate(model)).thenReturn(true);
        ActionResponse<ClientCertificateModel> actionResponseCreate = crudActions.create(model);
        assertTrue(actionResponseCreate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseCreate.getHttpStatus());
        assertTrue(actionResponseCreate.hasContent());

        ActionResponse<ClientCertificateModel> actionResponseDelete = crudActions.delete();
        assertTrue(actionResponseDelete.isSuccessful());
        assertEquals(HttpStatus.NO_CONTENT, actionResponseDelete.getHttpStatus());
        assertFalse(actionResponseDelete.hasContent());

        Mockito.verify(certificateManager).removeCertificate();
    }
}
