package com.synopsys.integration.alert.component.settings.proxy.actions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyTestService;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.component.settings.proxy.action.SettingsProxyTestAction;
import com.synopsys.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.synopsys.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;
import com.synopsys.integration.alert.test.common.AuthenticationTestUtils;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@Transactional
@AlertIntegrationTest
class SettingsProxyTestActionTestIT {
    @Autowired
    private SettingsProxyValidator settingsProxyValidator;
    @Autowired
    private SettingsDescriptorKey settingsDescriptorKey;
    @Autowired
    private ProxyTestService proxyTestService;
    @Autowired
    private SettingsProxyConfigAccessor settingsProxyConfigAccessor;

    private final TestProperties testProperties = new TestProperties();
    private final String validTargetUrl = "https://google.com";

    private SettingsProxyTestAction settingsProxyTestAction;

    @AfterEach
    void cleanup() {
        settingsProxyConfigAccessor.deleteConfiguration();
    }

    @Test
    void testWithPermissionCheckTest() {
        SettingsProxyModel settingsProxyModel = createSettingsProxyModel(testProperties);
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        settingsProxyTestAction = new SettingsProxyTestAction(authorizationManager, settingsProxyValidator, settingsDescriptorKey, proxyTestService, settingsProxyConfigAccessor);

        ActionResponse<ValidationResponseModel> testResult = settingsProxyTestAction.testWithPermissionCheck(validTargetUrl, settingsProxyModel);

        assertTrue(testResult.isSuccessful());
        assertTrue(testResult.getContent().isPresent());
        ValidationResponseModel validationResponseModel = testResult.getContent().get();
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    void testWithoutPermissionsCheckTest() {
        SettingsProxyModel settingsProxyModel = createSettingsProxyModel(testProperties);
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.NO_PERMISSIONS);
        settingsProxyTestAction = new SettingsProxyTestAction(authorizationManager, settingsProxyValidator, settingsDescriptorKey, proxyTestService, settingsProxyConfigAccessor);

        ActionResponse<ValidationResponseModel> testResult = settingsProxyTestAction.testWithPermissionCheck(validTargetUrl, settingsProxyModel);

        assertTrue(testResult.isError());
        assertTrue(testResult.getContent().isPresent());
        ValidationResponseModel validationResponseModel = testResult.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    void testValidationFailureTest() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        settingsProxyTestAction = new SettingsProxyTestAction(authorizationManager, settingsProxyValidator, settingsDescriptorKey, proxyTestService, settingsProxyConfigAccessor);

        ActionResponse<ValidationResponseModel> testResult = settingsProxyTestAction.testWithPermissionCheck(validTargetUrl, settingsProxyModel);

        assertTrue(testResult.isSuccessful());
        assertTrue(testResult.getContent().isPresent());
        ValidationResponseModel validationResponseModel = testResult.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    void missingTargetUrlTest() {
        SettingsProxyModel settingsProxyModel = createSettingsProxyModel(testProperties);
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        settingsProxyTestAction = new SettingsProxyTestAction(authorizationManager, settingsProxyValidator, settingsDescriptorKey, proxyTestService, settingsProxyConfigAccessor);

        ActionResponse<ValidationResponseModel> testResult = settingsProxyTestAction.testWithPermissionCheck("", settingsProxyModel);
        assertTrue(testResult.isSuccessful());
        assertTrue(testResult.getContent().isPresent());
        ValidationResponseModel validationResponseModel = testResult.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    void malformedTargetUrlTest() {
        SettingsProxyModel settingsProxyModel = createSettingsProxyModel(testProperties);
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        settingsProxyTestAction = new SettingsProxyTestAction(authorizationManager, settingsProxyValidator, settingsDescriptorKey, proxyTestService, settingsProxyConfigAccessor);

        ActionResponse<ValidationResponseModel> testResult = settingsProxyTestAction.testWithPermissionCheck("Not a valid url", settingsProxyModel);
        assertTrue(testResult.isSuccessful());
        assertTrue(testResult.getContent().isPresent());
        ValidationResponseModel validationResponseModel = testResult.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    void testUrlWithBadResponseTest() {
        SettingsProxyModel settingsProxyModel = createSettingsProxyModel(testProperties);
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        settingsProxyTestAction = new SettingsProxyTestAction(authorizationManager, settingsProxyValidator, settingsDescriptorKey, proxyTestService, settingsProxyConfigAccessor);

        ActionResponse<ValidationResponseModel> testResult = settingsProxyTestAction.testWithPermissionCheck("http://thisUrlWillReturnFailures", settingsProxyModel);
        assertTrue(testResult.isSuccessful());
        assertTrue(testResult.getContent().isPresent());
        ValidationResponseModel validationResponseModel = testResult.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    void testConfigurationWithPasswordSaved() throws AlertConfigurationException {
        SettingsProxyModel settingsProxyModel = createSettingsProxyModel(testProperties);
        settingsProxyConfigAccessor.createConfiguration(createSettingsProxyModel(testProperties));

        settingsProxyModel.setProxyPassword(null);
        settingsProxyModel.setIsProxyPasswordSet(true);
        AuthorizationManager authorizationManager = createAuthorizationManager(AuthenticationTestUtils.FULL_PERMISSIONS);
        settingsProxyTestAction = new SettingsProxyTestAction(authorizationManager, settingsProxyValidator, settingsDescriptorKey, proxyTestService, settingsProxyConfigAccessor);

        ActionResponse<ValidationResponseModel> testResult = settingsProxyTestAction.testWithPermissionCheck(validTargetUrl, settingsProxyModel);
        assertTrue(testResult.isSuccessful());
        assertTrue(testResult.getContent().isPresent());
        ValidationResponseModel validationResponseModel = testResult.getContent().get();
        assertFalse(validationResponseModel.hasErrors());
    }

    private SettingsProxyModel createSettingsProxyModel(TestProperties testProperties) {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();

        settingsProxyModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        settingsProxyModel.setProxyHost(testProperties.getProperty(TestPropertyKey.TEST_PROXY_HOST));
        settingsProxyModel.setProxyPort(Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_PROXY_PORT)));

        testProperties.getOptionalProperty(TestPropertyKey.TEST_PROXY_USERNAME).ifPresent(settingsProxyModel::setProxyUsername);
        testProperties.getOptionalProperty(TestPropertyKey.TEST_PROXY_PASSWORD).ifPresent(settingsProxyModel::setProxyPassword);

        return settingsProxyModel;
    }

    private AuthorizationManager createAuthorizationManager(int assignedPermissions) {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), settingsDescriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, assignedPermissions);
        return authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
    }
}
