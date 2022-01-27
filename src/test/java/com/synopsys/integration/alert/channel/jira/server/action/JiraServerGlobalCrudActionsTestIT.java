package com.synopsys.integration.alert.channel.jira.server.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.database.configuration.JiraServerConfigurationRepository;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.validator.JiraServerGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
public class JiraServerGlobalCrudActionsTestIT {
    private AuthorizationManager authorizationManager;
    @Autowired
    private JiraServerGlobalConfigAccessor configAccessor;
    @Autowired
    private JiraServerGlobalConfigurationValidator validator;
    @Autowired
    private JiraServerConfigurationRepository jiraServerConfigurationRepository;

    @BeforeEach
    public void init() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        DescriptorKey descriptorKey = ChannelKeys.JIRA_SERVER;
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
    }

    //@Transactional
    @AfterEach
    public void cleanup() {
        //jiraServerConfigurationRepository.deleteAllInBatch();
        //jiraServerConfigurationRepository.flush();
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = configAccessor.getConfigurationPage(0, 20);
        for (JiraServerGlobalConfigModel model : pagedModel.getModels()) {
            configAccessor.deleteConfiguration(UUID.fromString(model.getId()));
        }
    }

    @Test
    void getOneTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator);
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
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator);

        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.getOne(UUID.randomUUID());

        assertTrue(actionResponse.isError());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, actionResponse.getHttpStatus());
    }

    @Test
    void getPagedTest() {
        int numOfModels = 10;
        int pageSize = 5;
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator);
        for (int i = 0; i < numOfModels; i++) {
            crudActions.create(createJiraModelWithName(String.format("config-%d", i)));
        }

        ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> pagedActionResponse = crudActions.getPaged(0, pageSize);
        assertTrue(pagedActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, pagedActionResponse.getHttpStatus());
        assertTrue(pagedActionResponse.getContent().isPresent());
        AlertPagedModel<JiraServerGlobalConfigModel> pagedModel = pagedActionResponse.getContent().get();
        assertEquals(numOfModels / pageSize, pagedModel.getTotalPages());
        assertEquals(pageSize, pagedModel.getModels().size());
    }

    @Test
    void getPageNotFoundTest() {
        int pageSize = 5;
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator);

        ActionResponse<AlertPagedModel<JiraServerGlobalConfigModel>> pagedActionResponse = crudActions.getPaged(0, pageSize);
        assertTrue(pagedActionResponse.isError());
        assertFalse(pagedActionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, pagedActionResponse.getHttpStatus());
    }

    @Test
    void createTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator);
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.create(createBasicJiraModel());

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
    }

    @Test
    void createBadRequestTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator);
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = new JiraServerGlobalConfigModel();
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.create(jiraServerGlobalConfigModel);

        assertTrue(actionResponse.isError());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponse.getHttpStatus());
    }

    @Test
    void createDuplicateNameTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator);
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createBasicJiraModel();
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.create(jiraServerGlobalConfigModel);

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());

        ActionResponse<JiraServerGlobalConfigModel> actionResponseDuplicate = crudActions.create(jiraServerGlobalConfigModel);

        assertTrue(actionResponseDuplicate.isError());
        assertFalse(actionResponseDuplicate.hasContent());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponse.getHttpStatus());
    }

    @Test
    void updateTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator);
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createBasicJiraModel();
        ActionResponse<JiraServerGlobalConfigModel> createActionResponse = crudActions.create(jiraServerGlobalConfigModel);
        assertTrue(createActionResponse.isSuccessful());
        assertTrue(createActionResponse.getContent().isPresent());

        UUID uuid = UUID.fromString(createActionResponse.getContent().get().getId());
        JiraServerGlobalConfigModel updatedJiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "https://aNewURL", "a-different-username");
        updatedJiraServerGlobalConfigModel.setPassword("newPassword");
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.update(uuid, updatedJiraServerGlobalConfigModel);

        assertTrue(createActionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertTrue(actionResponse.getContent().isPresent());
        assertEquals("https://aNewURL", actionResponse.getContent().get().getUrl(), "The updated model does not have the correct updated value.");
    }

    @Test
    void updateNotFoundTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator);

        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createBasicJiraModel();
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.update(UUID.randomUUID(), jiraServerGlobalConfigModel);

        assertTrue(actionResponse.isError());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, actionResponse.getHttpStatus());
    }

    @Test
    void updateBadRequestTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator);
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = createBasicJiraModel();
        ActionResponse<JiraServerGlobalConfigModel> createActionResponse = crudActions.create(jiraServerGlobalConfigModel);
        assertTrue(createActionResponse.isSuccessful());
        assertTrue(createActionResponse.getContent().isPresent());

        UUID uuid = UUID.fromString(createActionResponse.getContent().get().getId());
        JiraServerGlobalConfigModel updatedJiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "https://aNewURL", "a-different-username");
        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.update(uuid, updatedJiraServerGlobalConfigModel);

        assertTrue(actionResponse.isError());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponse.getHttpStatus());
    }

    @Test
    void deleteTest() {
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator);
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
        JiraServerGlobalCrudActions crudActions = new JiraServerGlobalCrudActions(authorizationManager, configAccessor, validator);

        ActionResponse<JiraServerGlobalConfigModel> actionResponse = crudActions.delete(UUID.randomUUID());

        assertTrue(actionResponse.isError());
        assertFalse(actionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, actionResponse.getHttpStatus());
    }

    private JiraServerGlobalConfigModel createBasicJiraModel() {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "https://url", "name");
        jiraServerGlobalConfigModel.setPassword("password");
        return jiraServerGlobalConfigModel;
    }

    private JiraServerGlobalConfigModel createJiraModelWithName(String name) {
        JiraServerGlobalConfigModel jiraServerGlobalConfigModel = new JiraServerGlobalConfigModel(null, name, "https://url", "name");
        jiraServerGlobalConfigModel.setPassword("password");
        return jiraServerGlobalConfigModel;
    }

}
