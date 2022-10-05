package com.synopsys.integration.alert.channel.azure.boards.action;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.azure.boards.database.accessor.AzureBoardsGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.azure.boards.model.AzureBoardsGlobalConfigModel;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class AzureBoardsGlobalCrudActionsTest {
    private final String CREATED_AT = String.valueOf(DateUtils.createCurrentDateTimestamp().minusMinutes(5L));
    private final String UPDATED_AT = String.valueOf(DateUtils.createCurrentDateTimestamp());
    private final String ORGANIZATION_NAME = "org";

    private final UUID id = UUID.randomUUID();
    private final UUID app_id = UUID.randomUUID();

    private final AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
    private final DescriptorKey descriptorKey = ChannelKeys.AZURE_BOARDS;
    private final PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
    private final Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
    private final AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet(
        "admin",
        "admin",
        () -> new PermissionMatrixModel(permissions)
    );

    AzureBoardsGlobalConfigModel testAzureBoardsGlobalConfigModel;
    AzureBoardsGlobalCrudActions testCrudActions;


    @Mock
    AzureBoardsGlobalConfigAccessor mockConfigAccessor;

    @BeforeEach
    void initEach() {
        testCrudActions = new AzureBoardsGlobalCrudActions(authorizationManager, mockConfigAccessor);
        testAzureBoardsGlobalConfigModel = new AzureBoardsGlobalConfigModel(
            String.valueOf(id),
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            CREATED_AT,
            UPDATED_AT,
            ORGANIZATION_NAME,
            String.valueOf(app_id),
            Boolean.TRUE,
            "W7071kQcvRRynq8-v4qmrZr1bjEfgFiKoVm7cOnOi!d",
            Boolean.TRUE
        );
    }

    @Test
    void getOneSucceeds() {
        Mockito.when(mockConfigAccessor.getConfiguration(id)).thenReturn(Optional.of(testAzureBoardsGlobalConfigModel));

        ActionResponse<AzureBoardsGlobalConfigModel> actionResponse = testCrudActions.getOne(id);

        assertActionResponseSucceeds(actionResponse);
        assertModelObfuscated(actionResponse);
    }

    @Test
    void getPagedSucceeds() {
        AlertPagedModel<AzureBoardsGlobalConfigModel> alertPagedModel = new AlertPagedModel<>(
            1,
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            List.of(testAzureBoardsGlobalConfigModel)
        );

        Mockito.when(mockConfigAccessor.getConfigurationPage(AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE, null, null, null)).thenReturn(alertPagedModel);

        ActionResponse<AlertPagedModel<AzureBoardsGlobalConfigModel>> actionResponse = testCrudActions.getPaged(
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            null,
            null,
            null
        );

        assertActionResponseSucceeds(actionResponse);

        assertTrue(actionResponse.getContent().isPresent());
        AlertPagedModel<AzureBoardsGlobalConfigModel> pagedModel = actionResponse.getContent().get();
        assertEquals(1, pagedModel.getModels().size());
        assertModelObfuscated(pagedModel.getModels().get(0));
    }

    @Test
    void getPageWithSearchTermSucceeds() {
        final String SEARCH_TERM = "term";
        AlertPagedModel<AzureBoardsGlobalConfigModel> alertPagedModel = new AlertPagedModel<>(
            1,
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            List.of(testAzureBoardsGlobalConfigModel)
        );

        Mockito.when(mockConfigAccessor.getConfigurationPage(AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE, SEARCH_TERM, null, null)).thenReturn(alertPagedModel);

        ActionResponse<AlertPagedModel<AzureBoardsGlobalConfigModel>> actionResponse = testCrudActions.getPaged(
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            SEARCH_TERM,
            null,
            null
        );

        assertActionResponseSucceeds(actionResponse);

        assertTrue(actionResponse.getContent().isPresent());
        AlertPagedModel<AzureBoardsGlobalConfigModel> pagedModel = actionResponse.getContent().get();
        assertEquals(1, pagedModel.getModels().size());
        assertModelObfuscated(pagedModel.getModels().get(0));
    }

    @Test
    void getPagedSortAscendingSucceeds() {
        AzureBoardsGlobalConfigModel firstModel = new AzureBoardsGlobalConfigModel(
            String.valueOf(UUID.randomUUID()),
            "Another Job",
            CREATED_AT,
            UPDATED_AT,
            "org2",
            String.valueOf(UUID.randomUUID()),
            Boolean.TRUE,
            "some other secret",
            Boolean.TRUE
        );
        AlertPagedModel<AzureBoardsGlobalConfigModel> alertPagedModel = new AlertPagedModel<>(
            1,
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            List.of(firstModel, testAzureBoardsGlobalConfigModel)
        );

        Mockito.when(mockConfigAccessor.getConfigurationPage(AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE, "", "name", "asc")).thenReturn(alertPagedModel);

        ActionResponse<AlertPagedModel<AzureBoardsGlobalConfigModel>> actionResponse = testCrudActions.getPaged(
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            "",
            "name",
            "asc"
        );

        assertActionResponseSucceeds(actionResponse);

        assertTrue(actionResponse.getContent().isPresent());
        AlertPagedModel<AzureBoardsGlobalConfigModel> pagedModel = actionResponse.getContent().get();
        assertEquals(2, pagedModel.getModels().size());
        assertEquals("Another Job", pagedModel.getModels().get(0).getName());
        assertEquals(AlertRestConstants.DEFAULT_CONFIGURATION_NAME, pagedModel.getModels().get(1).getName());
    }

    @Test
    void getPagedSortDescendingSucceeds() {
        AzureBoardsGlobalConfigModel secondModel = new AzureBoardsGlobalConfigModel(
            String.valueOf(UUID.randomUUID()),
            "Another Job",
            CREATED_AT,
            UPDATED_AT,
            "org2",
            String.valueOf(UUID.randomUUID()),
            Boolean.TRUE,
            "some other secret",
            Boolean.TRUE
        );
        AlertPagedModel<AzureBoardsGlobalConfigModel> alertPagedModel = new AlertPagedModel<>(
            1,
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            List.of(testAzureBoardsGlobalConfigModel, secondModel)
        );

        Mockito.when(mockConfigAccessor.getConfigurationPage(AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE, "", "name", "desc")).thenReturn(alertPagedModel);

        ActionResponse<AlertPagedModel<AzureBoardsGlobalConfigModel>> actionResponse = testCrudActions.getPaged(
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            "",
            "name",
            "desc"
        );

        assertActionResponseSucceeds(actionResponse);

        assertTrue(actionResponse.getContent().isPresent());
        AlertPagedModel<AzureBoardsGlobalConfigModel> pagedModel = actionResponse.getContent().get();
        assertEquals(2, pagedModel.getModels().size());
        assertEquals(AlertRestConstants.DEFAULT_CONFIGURATION_NAME, pagedModel.getModels().get(0).getName());
        assertEquals("Another Job", pagedModel.getModels().get(1).getName());
    }

    @Test
    void createSucceeds() throws AlertConfigurationException {
        Mockito.when(mockConfigAccessor.createConfiguration(Mockito.any())).thenReturn(testAzureBoardsGlobalConfigModel);

        ActionResponse<AzureBoardsGlobalConfigModel> actionResponse = testCrudActions.create(testAzureBoardsGlobalConfigModel);

        assertActionResponseSucceeds(actionResponse);
        assertModelObfuscated(actionResponse);
    }

    @Test
    void createDuplicateTest() throws AlertConfigurationException {
        Mockito.when(mockConfigAccessor.existsConfigurationByName(Mockito.any())).thenReturn(true);
        Mockito.when(mockConfigAccessor.createConfiguration(Mockito.any())).thenReturn(testAzureBoardsGlobalConfigModel);

        ActionResponse<AzureBoardsGlobalConfigModel> actionResponse = testCrudActions.create(testAzureBoardsGlobalConfigModel);

        assertTrue(actionResponse.isError());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponse.getHttpStatus());
    }

    @Test
    void updateTest() throws AlertConfigurationException {
        Mockito.when(mockConfigAccessor.existsConfigurationById(id)).thenReturn(true);
        Mockito.when(mockConfigAccessor.updateConfiguration(Mockito.eq(id), Mockito.any())).thenReturn(testAzureBoardsGlobalConfigModel);

        ActionResponse<AzureBoardsGlobalConfigModel> actionResponse = testCrudActions.update(id, testAzureBoardsGlobalConfigModel);

        assertActionResponseSucceeds(actionResponse);
        assertModelObfuscated(actionResponse);
    }

    @Test
    void deleteTest() {
        Mockito.when(mockConfigAccessor.existsConfigurationById(id)).thenReturn(true);

        ActionResponse<AzureBoardsGlobalConfigModel> actionResponse = testCrudActions.delete(id);

        Mockito.verify(mockConfigAccessor).deleteConfiguration(id);

        assertTrue(actionResponse.isSuccessful());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, actionResponse.getHttpStatus());
    }

    private void assertActionResponseSucceeds(ActionResponse<?> actionResponse) {
        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
    }

    private void assertModelObfuscated(ActionResponse<AzureBoardsGlobalConfigModel> actionResponse) {
        Optional<AzureBoardsGlobalConfigModel> optionalAzureBoardsGlobalConfigModel = actionResponse.getContent();
        assertTrue(optionalAzureBoardsGlobalConfigModel.isPresent());
        assertModelObfuscated(optionalAzureBoardsGlobalConfigModel.get());
    }

    private void assertModelObfuscated(AzureBoardsGlobalConfigModel azureBoardsGlobalConfigModel) {
        assertEquals(AlertRestConstants.DEFAULT_CONFIGURATION_NAME, azureBoardsGlobalConfigModel.getName());
        assertEquals(CREATED_AT, azureBoardsGlobalConfigModel.getCreatedAt());
        assertEquals(UPDATED_AT, azureBoardsGlobalConfigModel.getLastUpdated());
        assertEquals(ORGANIZATION_NAME, azureBoardsGlobalConfigModel.getOrganizationName());

        // See model obfuscate for fields to hide
        assertTrue(azureBoardsGlobalConfigModel.getAppId().isEmpty());
        assertTrue(azureBoardsGlobalConfigModel.getIsAppIdSet().isPresent());
        assertEquals(Boolean.TRUE, azureBoardsGlobalConfigModel.getIsAppIdSet().get());

        assertTrue(azureBoardsGlobalConfigModel.getClientSecret().isEmpty());
        assertTrue(azureBoardsGlobalConfigModel.getIsClientSecretSet().isPresent());
        assertEquals(Boolean.TRUE, azureBoardsGlobalConfigModel.getIsClientSecretSet().get());
    }
}
