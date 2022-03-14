package com.synopsys.integration.alert.channel.jira.server.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;

public class JiraServerGlobalCrudActionsTest {
    private final String CREATED_AT = String.valueOf(DateUtils.createCurrentDateTimestamp().minusMinutes(5L));
    private final String UPDATED_AT = String.valueOf(DateUtils.createCurrentDateTimestamp());
    private final String URL = "https://someUrl";
    private final String USER_NAME = "username";
    private final String PASSWORD = "password";

    private final UUID id = UUID.randomUUID();

    private final AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
    private final DescriptorKey descriptorKey = new JiraServerChannelKey();
    private final PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
    private final Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
    private final AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet(
        "admin",
        "admin",
        () -> new PermissionMatrixModel(permissions)
    );

    @Test
    void getOneTest() {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createJiraServerGlobalConfigModel(id);

        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfiguration(id)).thenReturn(Optional.of(jiraServerGlobalConfigModel));

        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, createValidator());
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.getOne(id);

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertModelObfuscated(actionResponse);
    }

    @Test
    void getPagedTest() {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createJiraServerGlobalConfigModel(id);
        AlertPagedModel<JiraServerGlobalConfigModel> alertPagedModel = new AlertPagedModel<>(
            1,
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            List.of(jiraServerGlobalConfigModel)
        );

        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationPage(AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE)).thenReturn(alertPagedModel);

        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, createValidator());
        ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> actionResponse = crudActions.getPaged(AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE);

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());

        assertTrue(actionResponse.getContent().isPresent());
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = actionResponse.getContent().get();
        assertEquals(1, pagedModel.getModels().size());
        assertModelObfuscated(pagedModel.getModels().get(0));
    }

    @Test
    void createTest() throws AlertConfigurationException {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createJiraServerGlobalConfigModel(id);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.createConfiguration(Mockito.any())).thenReturn(jiraServerGlobalConfigModel);

        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, createValidator());
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.create(jiraServerGlobalConfigModel);

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertModelObfuscated(actionResponse);
    }

    @Test
    void createDuplicateTest() throws AlertConfigurationException {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createJiraServerGlobalConfigModel(id);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.existsConfigurationByName(Mockito.any())).thenReturn(true);
        Mockito.when(configAccessor.createConfiguration(Mockito.any())).thenReturn(jiraServerGlobalConfigModel);

        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, createValidator());
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.create(jiraServerGlobalConfigModel);

        assertTrue(actionResponse.isError());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponse.getHttpStatus());
    }

    @Test
    void updateTest() throws AlertConfigurationException {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createJiraServerGlobalConfigModel(id);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.existsConfigurationById(id)).thenReturn(true);
        Mockito.when(configAccessor.updateConfiguration(Mockito.eq(id), Mockito.any())).thenReturn(jiraServerGlobalConfigModel);

        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, createValidator());
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.update(id, jiraServerGlobalConfigModel);

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertModelObfuscated(actionResponse);
    }

    @Test
    void deleteTest() {
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.existsConfigurationById(id)).thenReturn(true);

        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, createValidator());
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.delete(id);

        Mockito.verify(configAccessor).deleteConfiguration(id);

        assertTrue(actionResponse.isSuccessful());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, actionResponse.getHttpStatus());
    }

    private JiraServerGlobalConfigurationValidator createValidator() {
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        return new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
    }

    private JiraServerGlobalConfigModel createJiraServerGlobalConfigModel(UUID id) {
        return new JiraServerGlobalConfigModel(
            String.valueOf(id),
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            CREATED_AT,
            UPDATED_AT,
            URL,
            USER_NAME,
            PASSWORD,
            Boolean.TRUE,
            Boolean.TRUE
        );
    }

    private void assertModelObfuscated(ActionResponse<JiraServerGlobalConfigModel> actionResponse) {
        Optional<JiraServerGlobalConfigModel> optionalJiraServerGlobalConfigModel = actionResponse.getContent();
        assertTrue(optionalJiraServerGlobalConfigModel.isPresent());
        assertModelObfuscated(optionalJiraServerGlobalConfigModel.get());
    }

    private void assertModelObfuscated(JiraServerGlobalConfigModel jiraServerGlobalConfigModel) {
        assertEquals(AlertRestConstants.DEFAULT_CONFIGURATION_NAME, jiraServerGlobalConfigModel.getName());
        assertEquals(CREATED_AT, jiraServerGlobalConfigModel.getCreatedAt());
        assertEquals(UPDATED_AT, jiraServerGlobalConfigModel.getLastUpdated());
        assertEquals(URL, jiraServerGlobalConfigModel.getUrl());
        assertEquals(USER_NAME, jiraServerGlobalConfigModel.getUserName());

        assertTrue(jiraServerGlobalConfigModel.getPassword().isEmpty());
        assertTrue(jiraServerGlobalConfigModel.getIsPasswordSet().isPresent());
        assertEquals(Boolean.TRUE, jiraServerGlobalConfigModel.getIsPasswordSet().get());
        assertTrue(jiraServerGlobalConfigModel.getDisablePluginCheck().isPresent());
        assertEquals(Boolean.TRUE, jiraServerGlobalConfigModel.getDisablePluginCheck().get());
    }
}
