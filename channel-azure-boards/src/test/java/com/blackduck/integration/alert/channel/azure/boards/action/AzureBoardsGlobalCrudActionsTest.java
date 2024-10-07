package com.blackduck.integration.alert.channel.azure.boards.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.gson.Gson;
import com.blackduck.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.azure.boards.database.configuration.AzureBoardsConfigurationEntity;
import com.blackduck.integration.alert.channel.azure.boards.database.mock.MockAzureBoardsConfigurationRepository;
import com.blackduck.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.blackduck.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationValidator;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.model.PermissionKey;
import com.blackduck.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.blackduck.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.test.common.AuthenticationTestUtils;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.alert.test.common.database.MockRepositorySorter;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

@ExtendWith(SpringExtension.class)
class AzureBoardsGlobalCrudActionsTest {
    private final AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
    private final DescriptorKey descriptorKey = ChannelKeys.AZURE_BOARDS;
    private final PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
    private final Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
    private final AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet(
        "admin",
        "admin",
        () -> new PermissionMatrixModel(permissions)
    );

    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();
    private final AlertProperties alertProperties = new MockAlertProperties();
    private final FilePersistenceUtil filePersistenceUtil = new FilePersistenceUtil(alertProperties, gson);
    private final EncryptionUtility encryptionUtility = new EncryptionUtility(alertProperties, filePersistenceUtil);

    AzureBoardsGlobalConfigModel firstConfigModel;
    AzureBoardsGlobalCrudActions azureBoardsGlobalCrudActions;
    AzureBoardsGlobalConfigAccessor azureBoardsGlobalConfigAccessor;

    @BeforeEach
    void initEach() {
        MockRepositorySorter<AzureBoardsConfigurationEntity> sorter = new MockRepositorySorter<>();
        MockAzureBoardsConfigurationRepository mockAzureBoardsConfigurationRepository = new MockAzureBoardsConfigurationRepository(sorter);
        azureBoardsGlobalConfigAccessor = new AzureBoardsGlobalConfigAccessor(encryptionUtility, mockAzureBoardsConfigurationRepository);

        azureBoardsGlobalCrudActions = new AzureBoardsGlobalCrudActions(
            authorizationManager,
            azureBoardsGlobalConfigAccessor,
            new AzureBoardsGlobalConfigurationValidator(azureBoardsGlobalConfigAccessor)
        );
        firstConfigModel = new AzureBoardsGlobalConfigModel(
            "",
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "",
            "",
            "First org",
            String.valueOf(UUID.randomUUID()),
            Boolean.TRUE,
            "a secret",
            Boolean.TRUE
        );
    }

    @Test
    void getOneSucceeds() throws ParseException {
        ActionResponse<AzureBoardsGlobalConfigModel> createActionResponse = createConfigModelAndAssertSuccess(firstConfigModel);
        ActionResponse<AzureBoardsGlobalConfigModel> getOneActionResponse = azureBoardsGlobalCrudActions.getOne(getUUIDFromActionResponse(createActionResponse));

        assertActionResponseSucceeds(getOneActionResponse);
        assertModelObfuscated(getOneActionResponse, firstConfigModel);
    }

    @Test
    void getOneReturnsNotFoundForNonExistentId() {
        createConfigModelAndAssertSuccess(firstConfigModel);
        ActionResponse<AzureBoardsGlobalConfigModel> getOneActionResponse = azureBoardsGlobalCrudActions.getOne(UUID.randomUUID());

        assertFalse(getOneActionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, getOneActionResponse.getHttpStatus());
    }

    @Test
    void getPagedSucceeds() throws ParseException {
        createConfigModelAndAssertSuccess(firstConfigModel);
        ActionResponse<AlertPagedModel<AzureBoardsGlobalConfigModel>> getPagedActionResponse = azureBoardsGlobalCrudActions.getPaged(
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            null,
            null,
            null
        );

        assertActionResponseSucceeds(getPagedActionResponse);

        assertTrue(getPagedActionResponse.getContent().isPresent());
        AlertPagedModel<AzureBoardsGlobalConfigModel> pagedModel = getPagedActionResponse.getContent().get();
        assertEquals(1, pagedModel.getModels().size());
        assertModelObfuscated(pagedModel.getModels().get(0), firstConfigModel);
    }

    @Test
    void getPagedReturnsNoModel() {
        ActionResponse<AlertPagedModel<AzureBoardsGlobalConfigModel>> getPagedActionResponse = azureBoardsGlobalCrudActions.getPaged(
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            null,
            null,
            null
        );

        assertTrue(getPagedActionResponse.getContent().isPresent());
        AlertPagedModel<AzureBoardsGlobalConfigModel> pagedModel = getPagedActionResponse.getContent().get();
        assertEquals(0, pagedModel.getModels().size());
    }

    @Test
    void createSucceeds() throws ParseException {
        ActionResponse<AzureBoardsGlobalConfigModel> actionResponse = createConfigModelAndAssertSuccess(firstConfigModel);

        assertModelObfuscated(actionResponse, firstConfigModel);
    }

    @Test
    void createMultipleNonDuplicateSucceeds() {
        createConfigModelAndAssertSuccess(firstConfigModel);

        AzureBoardsGlobalConfigModel secondConfigModel = new AzureBoardsGlobalConfigModel(
            "",
            "Second config",
            "",
            "",
            "Second org",
            String.valueOf(UUID.randomUUID()),
            Boolean.TRUE,
            "Second secret",
            Boolean.TRUE
        );
        createConfigModelAndAssertSuccess(secondConfigModel);
        assertEquals(2, azureBoardsGlobalConfigAccessor.getConfigurationCount());

        AzureBoardsGlobalConfigModel thirdConfigModel = new AzureBoardsGlobalConfigModel(
            "",
            "Third config",
            "",
            "",
            "Third org",
            String.valueOf(UUID.randomUUID()),
            Boolean.TRUE,
            "Third secret",
            Boolean.TRUE
        );
        createConfigModelAndAssertSuccess(thirdConfigModel);
        assertEquals(3, azureBoardsGlobalConfigAccessor.getConfigurationCount());
    }

    @Test
    void createDuplicateReturnsError() {
        createConfigModelAndAssertSuccess(firstConfigModel);

        AzureBoardsGlobalConfigModel duplicateAzureBoardsGlobalConfigModel = new AzureBoardsGlobalConfigModel(
            "",
            firstConfigModel.getName(),  // Reusing name returns error as it is unique
            "",
            "",
            "New org name",
            String.valueOf(UUID.randomUUID()),
            Boolean.TRUE,
            "New secret",
            Boolean.TRUE
        );

        ActionResponse<AzureBoardsGlobalConfigModel> updateActionResponse = azureBoardsGlobalCrudActions.create(duplicateAzureBoardsGlobalConfigModel);
        assertEquals(1, azureBoardsGlobalConfigAccessor.getConfigurationCount());

        assertTrue(updateActionResponse.isError());
        assertFalse(updateActionResponse.hasContent());
        assertEquals(HttpStatus.BAD_REQUEST, updateActionResponse.getHttpStatus());
    }

    @Test
    void updateSucceeds() throws ParseException {
        ActionResponse<AzureBoardsGlobalConfigModel> createActionResponse = createConfigModelAndAssertSuccess(firstConfigModel);
        AzureBoardsGlobalConfigModel createResponseModel = createActionResponse.getContent().orElseThrow();

        ActionResponse<AzureBoardsGlobalConfigModel> updateActionResponse = azureBoardsGlobalCrudActions.update(
            getUUIDFromActionResponse(createActionResponse),
            createResponseModel
        );
        assertEquals(1, azureBoardsGlobalConfigAccessor.getConfigurationCount());

        assertActionResponseSucceeds(updateActionResponse);
        assertModelObfuscated(updateActionResponse, createResponseModel);
    }

    @Test
    void deleteSucceeds() {
        ActionResponse<AzureBoardsGlobalConfigModel> createActionResponse = createConfigModelAndAssertSuccess(firstConfigModel);

        ActionResponse<AzureBoardsGlobalConfigModel> deleteActionResponse = azureBoardsGlobalCrudActions.delete(getUUIDFromActionResponse(createActionResponse));

        assertTrue(deleteActionResponse.isSuccessful());
        assertFalse(deleteActionResponse.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, deleteActionResponse.getHttpStatus());
    }

    private void assertActionResponseSucceeds(ActionResponse<?> actionResponse) {
        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
    }

    private void assertModelObfuscated(ActionResponse<AzureBoardsGlobalConfigModel> actionResponse, AzureBoardsGlobalConfigModel expectedValuesModel) throws ParseException {
        assertTrue(actionResponse.getContent().isPresent());
        AzureBoardsGlobalConfigModel responseAzureBoardsGlobalConfigModel = actionResponse.getContent().get();
        assertModelObfuscated(responseAzureBoardsGlobalConfigModel, expectedValuesModel);
    }

    private void assertModelObfuscated(AzureBoardsGlobalConfigModel responseAzureBoardsGlobalConfigModel, AzureBoardsGlobalConfigModel expectedValuesModel) throws ParseException {
        // These values from response should be visible and match the ones from expectedValuesModel
        assertEquals(expectedValuesModel.getName(), responseAzureBoardsGlobalConfigModel.getName());
        assertEquals(expectedValuesModel.getOrganizationName(), responseAzureBoardsGlobalConfigModel.getOrganizationName());
        // For timestamps fields (set by accessor); parsing them should not throw an error (bad format or empty)
        DateUtils.parseDate(responseAzureBoardsGlobalConfigModel.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        DateUtils.parseDate(responseAzureBoardsGlobalConfigModel.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);

        // See model.obfuscate for fields to hide
        assertTrue(responseAzureBoardsGlobalConfigModel.getAppId().isEmpty());
        assertTrue(responseAzureBoardsGlobalConfigModel.getIsAppIdSet().isPresent());
        assertEquals(Boolean.TRUE, responseAzureBoardsGlobalConfigModel.getIsAppIdSet().get());

        assertTrue(responseAzureBoardsGlobalConfigModel.getClientSecret().isEmpty());
        assertTrue(responseAzureBoardsGlobalConfigModel.getIsClientSecretSet().isPresent());
        assertEquals(Boolean.TRUE, responseAzureBoardsGlobalConfigModel.getIsClientSecretSet().get());
    }

    private ActionResponse<AzureBoardsGlobalConfigModel> createConfigModelAndAssertSuccess(AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel) {
        ActionResponse<AzureBoardsGlobalConfigModel> actionResponse = azureBoardsGlobalCrudActions.create(azureBoardsGlobalConfigModel);
        assertActionResponseSucceeds(actionResponse);

        return actionResponse;
    }

    private UUID getUUIDFromActionResponse(ActionResponse<AzureBoardsGlobalConfigModel> actionResponse) {
        AzureBoardsGlobalConfigModel createResponseModel = actionResponse.getContent().orElseThrow();
        return UUID.fromString(createResponseModel.getId());
    }
}
