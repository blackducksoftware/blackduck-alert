/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraSchedulingManager;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.blackduck.integration.alert.channel.jira.server.task.JiraServerSchedulingManager;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.blackduck.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.model.PermissionKey;
import com.blackduck.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.model.AlertPagedModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.test.common.AuthenticationTestUtils;
import org.springframework.scheduling.TaskScheduler;

class JiraServerGlobalCrudActionsTest {
    private static final String CREATED_AT = String.valueOf(DateUtils.createCurrentDateTimestamp().minusMinutes(5L));
    private static final String UPDATED_AT = String.valueOf(DateUtils.createCurrentDateTimestamp());
    private static final String URL = "https://someUrl";
    private static final Integer TEST_JIRA_TIMEOUT_SECONDS = 300;
    private static final String USER_NAME = "username";
    private static final String PASSWORD = "password";
    private static final String ACCESS_TOKEN = "accessToken";

    private final UUID id = UUID.randomUUID();

    private final Gson gson = new Gson();
    private final AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
    private final DescriptorKey descriptorKey = new JiraServerChannelKey();
    private final PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
    private final Map<PermissionKey, Integer> permissions = Map.of(permissionKey, 255);
    private final AuthorizationManager authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet(
        "admin",
        "admin",
        () -> new PermissionMatrixModel(permissions)
    );

    private final TaskManager taskManager = new TaskManager();
    private final JiraSchedulingManager jiraSchedulingManager = new JiraSchedulingManager(taskManager);
    private final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
    private final JiraServerPropertiesFactory jiraPropertiesFactory = Mockito.mock(JiraServerPropertiesFactory.class);
    private final JiraServerSchedulingManager jiraServerSchedulingManager = new JiraServerSchedulingManager(gson, jiraSchedulingManager, taskScheduler, taskManager, jiraPropertiesFactory);

    @Test
    void getOneTest() {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createJiraServerGlobalConfigModel(id);

        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfiguration(id)).thenReturn(Optional.of(jiraServerGlobalConfigModel));

        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, createValidator(), jiraServerSchedulingManager);
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
        Mockito.when(configAccessor.getConfigurationPage(AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE, null, null, null)).thenReturn(alertPagedModel);

        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, createValidator(), jiraServerSchedulingManager);
        ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> actionResponse = crudActions.getPaged(
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            null,
            null,
            null
        );

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());

        assertTrue(actionResponse.getContent().isPresent());
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = actionResponse.getContent().get();
        assertEquals(1, pagedModel.getModels().size());
        assertModelObfuscated(pagedModel.getModels().get(0));
    }

    @Test
    void getPagedSearchTermTest() {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createJiraServerGlobalConfigModel(id);
        AlertPagedModel<JiraServerGlobalConfigModel> alertPagedModel = new AlertPagedModel<>(
            1,
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            List.of(jiraServerGlobalConfigModel)
        );

        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationPage(AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE, "", null, null)).thenReturn(alertPagedModel);

        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, createValidator(), jiraServerSchedulingManager);
        ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> actionResponse = crudActions.getPaged(
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            "",
            null,
            null
        );

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());

        assertTrue(actionResponse.getContent().isPresent());
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = actionResponse.getContent().get();
        assertEquals(1, pagedModel.getModels().size());
        assertModelObfuscated(pagedModel.getModels().get(0));
    }

    @Test
    void getPagedSortAscendingTest() {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createJiraServerGlobalConfigModel(id);
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel2 = new JiraServerGlobalConfigModel(
            String.valueOf(UUID.randomUUID()),
            "Another Job",
            CREATED_AT,
            UPDATED_AT,
            URL,
            TEST_JIRA_TIMEOUT_SECONDS,
            JiraServerAuthorizationMethod.BASIC,
            USER_NAME,
            PASSWORD,
            Boolean.TRUE,
            null,
            Boolean.FALSE,
            Boolean.TRUE
        );
        AlertPagedModel<JiraServerGlobalConfigModel> alertPagedModel = new AlertPagedModel<>(
            1,
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            List.of(jiraServerGlobalConfigModel2, jiraServerGlobalConfigModel)
        );

        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationPage(AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE, "", "name", "asc")).thenReturn(alertPagedModel);

        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, createValidator(), jiraServerSchedulingManager);
        ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> actionResponse = crudActions.getPaged(
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            "",
            "name",
            "asc"
        );

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());

        assertTrue(actionResponse.getContent().isPresent());
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = actionResponse.getContent().get();
        assertEquals(2, pagedModel.getModels().size());
        assertEquals("Another Job", pagedModel.getModels().get(0).getName());
        assertEquals(AlertRestConstants.DEFAULT_CONFIGURATION_NAME, pagedModel.getModels().get(1).getName());

    }

    @Test
    void getPagedSortDescendingTest() {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createJiraServerGlobalConfigModel(id);
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel2 = new JiraServerGlobalConfigModel(
            String.valueOf(UUID.randomUUID()),
            "Another Job",
            CREATED_AT,
            UPDATED_AT,
            URL,
            TEST_JIRA_TIMEOUT_SECONDS,
            JiraServerAuthorizationMethod.BASIC,
            USER_NAME,
            PASSWORD,
            Boolean.TRUE,
            null,
            Boolean.FALSE,
            Boolean.TRUE
        );
        AlertPagedModel<JiraServerGlobalConfigModel> alertPagedModel = new AlertPagedModel<>(
            1,
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            List.of(jiraServerGlobalConfigModel, jiraServerGlobalConfigModel2)
        );

        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.getConfigurationPage(AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE, "", "name", "desc")).thenReturn(alertPagedModel);

        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, createValidator(), jiraServerSchedulingManager);
        ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> actionResponse = crudActions.getPaged(
            AlertPagedModel.DEFAULT_PAGE_NUMBER,
            AlertPagedModel.DEFAULT_PAGE_SIZE,
            "",
            "name",
            "desc"
        );

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());

        assertTrue(actionResponse.getContent().isPresent());
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = actionResponse.getContent().get();
        assertEquals(2, pagedModel.getModels().size());
        assertEquals(AlertRestConstants.DEFAULT_CONFIGURATION_NAME, pagedModel.getModels().get(0).getName());
        assertEquals("Another Job", pagedModel.getModels().get(1).getName());
    }

    @Test
    void createTest() throws AlertConfigurationException {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createJiraServerGlobalConfigModel(id);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.createConfiguration(Mockito.any())).thenReturn(jiraServerGlobalConfigModel);

        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, createValidator(), jiraServerSchedulingManager);
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.create(jiraServerGlobalConfigModel);

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertModelObfuscated(actionResponse);
    }

    @Test
    void createWithPersonalAccessTokenTesT() throws AlertConfigurationException {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createJiraServerGlobalConfigModelWithAccessToken(id);
        JiraServerGlobalConfigAccessor configAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(configAccessor.createConfiguration(Mockito.any())).thenReturn(jiraServerGlobalConfigModel);

        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, createValidator(), jiraServerSchedulingManager);
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

        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, createValidator(), jiraServerSchedulingManager);
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

        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, createValidator(), jiraServerSchedulingManager);
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

        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, createValidator(), jiraServerSchedulingManager);
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
            TEST_JIRA_TIMEOUT_SECONDS,
            JiraServerAuthorizationMethod.BASIC,
            USER_NAME,
            PASSWORD,
            Boolean.TRUE,
            null,
            Boolean.FALSE,
            Boolean.TRUE
        );
    }

    private JiraServerGlobalConfigModel createJiraServerGlobalConfigModelWithAccessToken(UUID id) {
        return new JiraServerGlobalConfigModel(
            String.valueOf(id),
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            CREATED_AT,
            UPDATED_AT,
            URL,
            TEST_JIRA_TIMEOUT_SECONDS,
            JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN,
            null,
            null,
            Boolean.FALSE,
            ACCESS_TOKEN,
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

        if (jiraServerGlobalConfigModel.getAuthorizationMethod() == JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN) {
            assertTrue(jiraServerGlobalConfigModel.getUserName().isEmpty());
            assertTrue(jiraServerGlobalConfigModel.getPassword().isEmpty());
            assertFalse(jiraServerGlobalConfigModel.getIsPasswordSet().orElse(Boolean.TRUE));
            assertTrue(jiraServerGlobalConfigModel.getAccessToken().isEmpty());
            assertTrue(jiraServerGlobalConfigModel.getIsAccessTokenSet().orElse(Boolean.FALSE));
        } else {
            assertEquals(USER_NAME, jiraServerGlobalConfigModel.getUserName().orElse("Username missing"));
            assertTrue(jiraServerGlobalConfigModel.getPassword().isEmpty());
            assertTrue(jiraServerGlobalConfigModel.getIsPasswordSet().orElse(Boolean.FALSE));
            assertTrue(jiraServerGlobalConfigModel.getAccessToken().isEmpty());
            assertFalse(jiraServerGlobalConfigModel.getIsAccessTokenSet().orElse(Boolean.TRUE));
        }
        assertTrue(jiraServerGlobalConfigModel.getDisablePluginCheck().orElse(Boolean.FALSE));
    }
}
