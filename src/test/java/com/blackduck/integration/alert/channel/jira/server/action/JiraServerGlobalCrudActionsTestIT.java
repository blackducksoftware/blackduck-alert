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

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.blackduck.integration.alert.channel.jira.server.task.JiraServerSchedulingManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
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
import com.blackduck.integration.alert.test.common.AuthenticationTestUtils;
import com.blackduck.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
class JiraServerGlobalCrudActionsTestIT {
    private AuthorizationManager authorizationManager;
    @Autowired
    private JiraServerGlobalConfigAccessor configAccessor;
    @Autowired
    private JiraServerGlobalConfigurationValidator validator;
    @Autowired
    private JiraServerSchedulingManager jiraSchedulingManager;

    private static final Integer TEST_JIRA_TIMEOUT_SECONDS = 300;

    @BeforeEach
    public void init() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.JIRA_SERVER;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
    }

    @AfterEach
    public void cleanup() {
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = configAccessor.getConfigurationPage(0, 20, null, null, null);
        for (JiraServerGlobalConfigModel model : pagedModel.getModels()) {
            configAccessor.deleteConfiguration(UUID.fromString(model.getId()));
        }
    }

    @Test
    void getOneTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);
        ActionResponse<JiraServerGlobalConfigModel> createResponse = crudActions.create(createBasicJiraModel());

        Optional<JiraServerGlobalConfigModel> jiraServerModel = createResponse.getContent();
        assertTrue(jiraServerModel.isPresent());
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.getOne(UUID.fromString(jiraServerModel.get().getId()));

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
    }

    @Test
    void getOneNotFoundTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);

        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.getOne(UUID.randomUUID());

        assertTrue(actionResponse.isError());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, actionResponse.getHttpStatus());
    }

    @Test
    void getPagedTest() {
        int numOfModels = 10;
        int pageSize = 5;
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);
        for (int i = 0; i < numOfModels; i++) {
            crudActions.create(createJiraModelWithName(String.format("config-%d", i)));
        }

        ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> pagedActionResponse = crudActions.getPaged(0, pageSize, null, null, null);
        assertTrue(pagedActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, pagedActionResponse.getHttpStatus());
        assertTrue(pagedActionResponse.getContent().isPresent());
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = pagedActionResponse.getContent().get();
        assertEquals(numOfModels / pageSize, pagedModel.getTotalPages());
        assertEquals(pageSize, pagedModel.getModels().size());
    }

    @Test
    void getPagedSortAscendingTest() {
        int numOfModels = 10;
        int pageSize = 5;
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);
        for (int i = 0; i < numOfModels; i++) {
            crudActions.create(createJiraModelWithName(String.format("config-%d", i)));
        }

        ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> pagedActionResponse = crudActions.getPaged(0, pageSize, "", "name", "asc");
        assertTrue(pagedActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, pagedActionResponse.getHttpStatus());
        assertTrue(pagedActionResponse.getContent().isPresent());
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = pagedActionResponse.getContent().get();
        assertEquals(numOfModels / pageSize, pagedModel.getTotalPages());
        assertEquals(pageSize, pagedModel.getModels().size());
        for (int index = 0; index < pageSize; index++) {
            assertEquals(String.format("config-%d", index), pagedModel.getModels().get(index).getName());
        }
    }

    @Test
    void getPagedSearchTermAndSortAscendingTest() {
        int numOfModels = 10;
        int pageSize = 5;
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);
        for (int i = 0; i < numOfModels; i++) {
            crudActions.create(createJiraModelWithName(String.format("config-%d", i)));
        }

        ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> pagedActionResponse = crudActions.getPaged(0, pageSize, "config-2", "name", "asc");
        assertTrue(pagedActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, pagedActionResponse.getHttpStatus());
        assertTrue(pagedActionResponse.getContent().isPresent());
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = pagedActionResponse.getContent().get();
        assertEquals(1, pagedModel.getTotalPages());
        assertEquals(1, pagedModel.getModels().size());
        assertEquals("config-2", pagedModel.getModels().get(0).getName());
    }

    @Test
    void getPagedSortDecendingTest() {
        int numOfModels = 10;
        int pageSize = 5;
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);
        for (int i = 0; i < numOfModels; i++) {
            crudActions.create(createJiraModelWithName(String.format("config-%d", i)));
        }

        ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> pagedActionResponse = crudActions.getPaged(0, pageSize, "", "name", "asc");
        assertTrue(pagedActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, pagedActionResponse.getHttpStatus());
        assertTrue(pagedActionResponse.getContent().isPresent());
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = pagedActionResponse.getContent().get();
        assertEquals(numOfModels / pageSize, pagedModel.getTotalPages());
        assertEquals(pageSize, pagedModel.getModels().size());
        for (int index = pageSize - 1; index >= 0; index--) {
            assertEquals(String.format("config-%d", index), pagedModel.getModels().get(index).getName());
        }
    }

    @Test
    void getPagedMissingSearchTermTest() {
        int numOfModels = 10;
        int pageSize = 5;
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);
        for (int i = 0; i < numOfModels; i++) {
            crudActions.create(createJiraModelWithName(String.format("config-%d", i)));
        }

        ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> pagedActionResponse = crudActions.getPaged(0, pageSize, "config-99", "name", "asc");
        assertTrue(pagedActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, pagedActionResponse.getHttpStatus());
        assertTrue(pagedActionResponse.getContent().isPresent());
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = pagedActionResponse.getContent().get();
        assertEquals(0, pagedModel.getTotalPages());
        assertTrue(pagedModel.getModels().isEmpty());
    }

    @Test
    void getPageNotFoundTest() {
        int pageSize = 5;
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);

        ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> pagedActionResponse = crudActions.getPaged(1, pageSize, null, null, null);
        assertTrue(pagedActionResponse.isError());
        assertFalse(pagedActionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, pagedActionResponse.getHttpStatus());
    }

    @Test
    void createBasicAuthTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.create(createBasicJiraModel());

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
    }

    @Test
    void createPersonalAccessTokenAuthTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.create(createPersonalAccessTokenJiraModel());

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
    }

    @Test
    void createBadRequestTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = new JiraServerGlobalConfigModel();
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.create(jiraServerGlobalConfigModel);

        assertTrue(actionResponse.isError());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponse.getHttpStatus());
    }

    @Test
    void createDuplicateNameTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createBasicJiraModel();
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.create(jiraServerGlobalConfigModel);

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());

        ActionResponse<JiraServerGlobalConfigModel> actionResponseDuplicate = crudActions.create(jiraServerGlobalConfigModel);

        assertTrue(actionResponseDuplicate.isError());
        assertFalse(actionResponseDuplicate.hasContent());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponseDuplicate.getHttpStatus());
    }

    @Test
    void updateBasicAuthTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createBasicJiraModel();
        ActionResponse<JiraServerGlobalConfigModel> createActionResponse = crudActions.create(jiraServerGlobalConfigModel);
        assertTrue(createActionResponse.isSuccessful());
        assertTrue(createActionResponse.getContent().isPresent());

        UUID uuid = UUID.fromString(createActionResponse.getContent().get().getId());
        JiraServerGlobalConfigModel updatedJiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(
            uuid.toString(),
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "https://aNewURL",
            TEST_JIRA_TIMEOUT_SECONDS,
            JiraServerAuthorizationMethod.BASIC
        );
        updatedJiraServerGlobalConfigModel.setUserName("a-different-username");
        updatedJiraServerGlobalConfigModel.setPassword("newPassword");
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.update(uuid, updatedJiraServerGlobalConfigModel);

        assertTrue(createActionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertTrue(actionResponse.getContent().isPresent());
        assertEquals("https://aNewURL", actionResponse.getContent().get().getUrl(), "The updated model does not have the correct updated value.");
    }

    @Test
    void updatePersonalAccessTokenAuthTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createPersonalAccessTokenJiraModel();
        ActionResponse<JiraServerGlobalConfigModel> createActionResponse = crudActions.create(jiraServerGlobalConfigModel);
        assertTrue(createActionResponse.isSuccessful());
        assertTrue(createActionResponse.getContent().isPresent());

        UUID uuid = UUID.fromString(createActionResponse.getContent().get().getId());
        JiraServerGlobalConfigModel updatedJiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(
            uuid.toString(),
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "https://aNewURL",
            TEST_JIRA_TIMEOUT_SECONDS,
            JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN
        );
        updatedJiraServerGlobalConfigModel.setAccessToken("a-different-personal-access-token");
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.update(uuid, updatedJiraServerGlobalConfigModel);

        assertTrue(createActionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertTrue(actionResponse.getContent().isPresent());
        assertEquals("https://aNewURL", actionResponse.getContent().get().getUrl(), "The updated model does not have the correct updated value.");
    }

    @Test
    void updateNotFoundTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);

        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createBasicJiraModel();
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.update(UUID.randomUUID(), jiraServerGlobalConfigModel);

        assertTrue(actionResponse.isError());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, actionResponse.getHttpStatus());
    }

    @Test
    void updateBadRequestTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createBasicJiraModel();
        ActionResponse<JiraServerGlobalConfigModel> createActionResponse = crudActions.create(jiraServerGlobalConfigModel);
        assertTrue(createActionResponse.isSuccessful());
        assertTrue(createActionResponse.getContent().isPresent());

        UUID uuid = UUID.fromString(createActionResponse.getContent().get().getId());
        JiraServerGlobalConfigModel updatedJiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "https://aNewURL",
            TEST_JIRA_TIMEOUT_SECONDS,
            JiraServerAuthorizationMethod.BASIC
        );
        updatedJiraServerGlobalConfigModel.setUserName("a-different-username");
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.update(uuid, updatedJiraServerGlobalConfigModel);

        assertTrue(actionResponse.isError());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponse.getHttpStatus());
    }

    @Test
    void updateSwitchBetweenAuthorizationTypesTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createBasicJiraModel();
        ActionResponse<JiraServerGlobalConfigModel> createActionResponse = crudActions.create(jiraServerGlobalConfigModel);
        assertTrue(createActionResponse.isSuccessful());
        assertTrue(createActionResponse.getContent().isPresent());

        UUID uuid = UUID.fromString(createActionResponse.getContent().get().getId());
        JiraServerGlobalConfigModel updatedJiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(
            uuid.toString(),
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "https://aNewURL",
            TEST_JIRA_TIMEOUT_SECONDS,
            JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN
        );
        updatedJiraServerGlobalConfigModel.setAccessToken("a-different-personal-access-token");
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.update(uuid, updatedJiraServerGlobalConfigModel);

        assertTrue(createActionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertTrue(actionResponse.getContent().isPresent());
        assertEquals("https://aNewURL", actionResponse.getContent().get().getUrl(), "The updated model does not have the correct updated value.");
        assertTrue(actionResponse.getContent().get().getIsAccessTokenSet().orElse(Boolean.FALSE), "Access token was not set");
        assertTrue(actionResponse.getContent().get().getPassword().isEmpty(), "The password field was not removed after updating to use a personal access token");
    }

    @Test
    void deleteTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createBasicJiraModel();
        ActionResponse<JiraServerGlobalConfigModel> createActionResponse = crudActions.create(jiraServerGlobalConfigModel);
        assertTrue(createActionResponse.isSuccessful());
        assertTrue(createActionResponse.getContent().isPresent());

        UUID uuid = UUID.fromString(createActionResponse.getContent().get().getId());
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.delete(uuid);

        assertTrue(actionResponse.isSuccessful());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, actionResponse.getHttpStatus());

        ActionResponse<JiraServerGlobalConfigModel> getActionResponse = crudActions.getOne(uuid);

        assertTrue(getActionResponse.isError());
        assertEquals(HttpStatus.NOT_FOUND, getActionResponse.getHttpStatus());
    }

    @Test
    void deleteNotFoundTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator, jiraSchedulingManager);

        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.delete(UUID.randomUUID());

        assertTrue(actionResponse.isError());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, actionResponse.getHttpStatus());
    }

    private JiraServerGlobalConfigModel createBasicJiraModel() {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "https://url",
            TEST_JIRA_TIMEOUT_SECONDS,
            JiraServerAuthorizationMethod.BASIC
        );
        jiraServerGlobalConfigModel.setUserName("name");
        jiraServerGlobalConfigModel.setPassword("password");
        return jiraServerGlobalConfigModel;
    }

    private JiraServerGlobalConfigModel createJiraModelWithName(String name) {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(
            null,
            name,
            "https://url",
            TEST_JIRA_TIMEOUT_SECONDS,
            JiraServerAuthorizationMethod.BASIC
        );
        jiraServerGlobalConfigModel.setUserName("name");
        jiraServerGlobalConfigModel.setPassword("password");
        return jiraServerGlobalConfigModel;
    }

    private JiraServerGlobalConfigModel createPersonalAccessTokenJiraModel() {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(
            null,
            AlertRestConstants.DEFAULT_CONFIGURATION_NAME,
            "https://url",
            TEST_JIRA_TIMEOUT_SECONDS,
            JiraServerAuthorizationMethod.PERSONAL_ACCESS_TOKEN
        );
        jiraServerGlobalConfigModel.setAccessToken("accessToken");
        return jiraServerGlobalConfigModel;
    }
}
