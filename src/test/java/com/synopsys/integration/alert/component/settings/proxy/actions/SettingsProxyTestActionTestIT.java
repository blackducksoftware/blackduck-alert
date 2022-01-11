package com.synopsys.integration.alert.component.settings.proxy.actions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.SettingsProxyModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.rest.proxy.ProxyTestService;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.component.settings.proxy.action.SettingsProxyTestAction;
import com.synopsys.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.synopsys.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@Transactional
@AlertIntegrationTest
public class SettingsProxyTestActionTestIT {
    @Autowired
    private SettingsProxyValidator settingsProxyValidator;
    @Autowired
    private SettingsDescriptorKey settingsDescriptorKey;
    @Autowired
    private ProxyTestService proxyTestService;
    @Autowired
    private SettingsProxyConfigAccessor settingsProxyConfigAccessor;

    private SettingsProxyTestAction settingsProxyTestAction;

    public static final String HOST = "host";
    public static final Integer PORT = 9999;
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String NON_PROXY_HOST = "nonProxyHostUrl";

    @BeforeEach
    public void init() {
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasCreatePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasDeletePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasWritePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasExecutePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(Boolean.TRUE);
        settingsProxyTestAction = new SettingsProxyTestAction(authorizationManager, settingsProxyValidator, settingsDescriptorKey, proxyTestService, settingsProxyConfigAccessor);
    }

    @Test
    public void testWithPermissionCheckTest() {
        String testUrl = "https://google.com";
        SettingsProxyModel settingsProxyModel = createSettingsProxyModel();
        ActionResponse<ValidationResponseModel> testResult = settingsProxyTestAction.testWithPermissionCheck(testUrl, settingsProxyModel);

        assertTrue(testResult.isSuccessful());
        assertTrue(testResult.getContent().isPresent());
        ValidationResponseModel validationResponseModel = testResult.getContent().get();
        assertFalse(validationResponseModel.hasErrors());
    }

    private SettingsProxyModel createSettingsProxyModel() {
        SettingsProxyModel settingsProxyModel = new SettingsProxyModel();
        settingsProxyModel.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        settingsProxyModel.setProxyHost(HOST);
        settingsProxyModel.setProxyPort(PORT);
        settingsProxyModel.setProxyUsername(USERNAME);
        settingsProxyModel.setProxyPassword(PASSWORD);
        settingsProxyModel.setIsSmtpPasswordSet(false);
        return settingsProxyModel;
    }
}
