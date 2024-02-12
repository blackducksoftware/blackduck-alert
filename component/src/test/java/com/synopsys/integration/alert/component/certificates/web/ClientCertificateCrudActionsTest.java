package com.synopsys.integration.alert.component.certificates.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.certificates.AlertClientCertificateManager;
import com.synopsys.integration.alert.component.certificates.CertificatesDescriptorKey;
import com.synopsys.integration.alert.database.api.ClientCertificateAccessor;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

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
            () -> new PermissionMatrixModel(permissions));
        AlertProperties alertProperties = new MockAlertProperties();
        FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, BlackDuckServicesFactory.createDefaultGson());
        EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);

        crudActions = new ClientCertificateCrudActions(
            certificateManager,
            authorizationManager,
            certificatesDescriptorKey,
            new ClientCertificateAccessor(encryptionUtility, new MockClientCertificateKeyRepository(), new MockClientCertificateRepository())
        );
        model = new ClientCertificateModel("key_password", "key_content", "certificate_content");
    }

    @Test
    void createConfig() {
        ActionResponse<ClientCertificateModel> actionResponseGetOne = crudActions.getOne();
        assertFalse(actionResponseGetOne.isSuccessful());
        assertEquals(HttpStatus.NOT_FOUND, actionResponseGetOne.getHttpStatus());
        assertFalse(actionResponseGetOne.hasContent());

        ActionResponse<ClientCertificateModel> actionResponseCreate = crudActions.create(model);
        assertTrue(actionResponseCreate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseCreate.getHttpStatus());
        assertTrue(actionResponseCreate.hasContent());
    }

    @Test
    void deleteNonExistingConfigReturnsNotFound() {
        ActionResponse<ClientCertificateModel> actionResponseDelete = crudActions.delete();
        assertFalse(actionResponseDelete.isSuccessful());
        assertEquals(HttpStatus.NOT_FOUND, actionResponseDelete.getHttpStatus());
        assertFalse(actionResponseDelete.hasContent());
    }

    @Test
    void deleteExistingConfigSucceeds() {
        ActionResponse<ClientCertificateModel> actionResponseCreate = crudActions.create(model);
        assertTrue(actionResponseCreate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseCreate.getHttpStatus());
        assertTrue(actionResponseCreate.hasContent());

        ActionResponse<ClientCertificateModel> actionResponseDelete = crudActions.delete();
        assertTrue(actionResponseDelete.isSuccessful());
        assertEquals(HttpStatus.NO_CONTENT, actionResponseDelete.getHttpStatus());
        assertFalse(actionResponseDelete.hasContent());
    }
}
