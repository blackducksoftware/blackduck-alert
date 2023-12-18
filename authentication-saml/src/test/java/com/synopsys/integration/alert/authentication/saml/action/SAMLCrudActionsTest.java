package com.synopsys.integration.alert.authentication.saml.action;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.authentication.saml.SAMLTestHelper;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.authentication.saml.security.SAMLManager;
import com.synopsys.integration.alert.authentication.saml.validator.SAMLConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.opentest4j.AssertionFailedError;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
class SAMLCrudActionsTest {
    private SAMLCrudActions samlCrudActions;
    private SAMLConfigModel samlConfigModel;

    @Mock
    private SAMLManager samlManager;

    @BeforeEach
    void init() {
        FilePersistenceUtil filePersistenceUtil = SAMLTestHelper.createFilePersistenceUtil();

        samlCrudActions = new SAMLCrudActions(
            SAMLTestHelper.createAuthorizationManager(),
            SAMLTestHelper.createTestSAMLConfigAccessor(),
            new SAMLConfigurationValidator(filePersistenceUtil),
            new AuthenticationDescriptorKey(),
            samlManager,
            filePersistenceUtil
        );

        samlConfigModel = new SAMLTestHelper.SAMLConfigModelBuilder()
            .setMetadataUrl("https://www.metdataurl.com/metadata_url")
            .build();
    }

    @Test
    void createNewConfigSucceeds() {
        ActionResponse<SAMLConfigModel> actionResponseGetOne = samlCrudActions.getOne();
        assertFalse(actionResponseGetOne.isSuccessful());
        assertEquals(HttpStatus.NOT_FOUND, actionResponseGetOne.getHttpStatus());
        assertFalse(actionResponseGetOne.hasContent());

        ActionResponse<SAMLConfigModel> actionResponseCreate = samlCrudActions.create(samlConfigModel);
        assertTrue(actionResponseCreate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseCreate.getHttpStatus());
        assertTrue(actionResponseCreate.hasContent());
    }

    @Test
    void updateExistingConfigSucceeds() {
        ActionResponse<SAMLConfigModel> actionResponseCreate = samlCrudActions.create(samlConfigModel);
        SAMLConfigModel createdSAMLConfigModel = actionResponseCreate.getContent().orElseThrow(() -> new AssertionFailedError("Updated SAMLConfigModel did not exist"));
        assertTrue(actionResponseCreate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseCreate.getHttpStatus());
        assertTrue(actionResponseCreate.hasContent());
        assertFalse(createdSAMLConfigModel.getEnabled());

        samlConfigModel.setEnabled(true);
        ActionResponse<SAMLConfigModel> actionResponseUpdate = samlCrudActions.update(samlConfigModel);
        SAMLConfigModel updatedSAMLConfigModel = actionResponseUpdate.getContent().orElseThrow(() -> new AssertionFailedError("Updated SAMLConfigModel did not exist"));
        assertTrue(actionResponseUpdate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseUpdate.getHttpStatus());
        assertTrue(actionResponseUpdate.hasContent());
        assertTrue(updatedSAMLConfigModel.getEnabled());
    }

    @Test
    void deleteNonExistingConfigReturnsNotFound() {
        ActionResponse<SAMLConfigModel> actionResponseDelete = samlCrudActions.delete();
        assertFalse(actionResponseDelete.isSuccessful());
        assertEquals(HttpStatus.NOT_FOUND, actionResponseDelete.getHttpStatus());
        assertFalse(actionResponseDelete.hasContent());
    }

    @Test
    void deleteExistingConfigSucceeds() {
        ActionResponse<SAMLConfigModel> actionResponseCreate = samlCrudActions.create(samlConfigModel);
        assertTrue(actionResponseCreate.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponseCreate.getHttpStatus());
        assertTrue(actionResponseCreate.hasContent());

        ActionResponse<SAMLConfigModel> actionResponseDelete = samlCrudActions.delete();
        assertTrue(actionResponseDelete.isSuccessful());
        assertEquals(HttpStatus.NO_CONTENT, actionResponseDelete.getHttpStatus());
        assertFalse(actionResponseDelete.hasContent());
    }
}
