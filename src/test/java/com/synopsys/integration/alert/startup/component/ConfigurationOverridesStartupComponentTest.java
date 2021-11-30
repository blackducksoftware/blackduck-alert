package com.synopsys.integration.alert.startup.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.component.authentication.actions.AuthenticationApiAction;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.component.authentication.web.AuthenticationActions;
import com.synopsys.integration.alert.component.authentication.web.LoginConfig;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
public class ConfigurationOverridesStartupComponentTest {
    private static final String DEFAULT_ADMIN_USER = "sysadmin";
    private static final String DEFAULT_PASSWORD = "blackduck";
    private static final String DEFAULT_PASSWORD_ENCODED = "$2a$16$Q3wfnhwA.1Qm3Tz3IkqDC.743C5KI7nJIuYlZ4xKXre/WBYpjUEFy";
    private static final String UPDATED_PASSWORD = "blah blah blah";

    @Autowired
    private AuthenticationDescriptorKey descriptorKey;
    @Autowired
    private ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    @Autowired
    private AuthenticationApiAction apiAction;
    @Autowired
    private ConfigurationFieldModelConverter configurationFieldModelConverter;
    @Autowired
    private UserAccessor userAccessor;

    @Autowired
    private AuthenticationActions authenticationActions;

    @AfterEach
    public void cleanUp() throws AlertException {
        Optional<UserModel> sysadminOptional = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID);
        assertTrue(sysadminOptional.isPresent());
        UserModel sysadmin = sysadminOptional.get();
        UserModel updatedSysadmin = changeUserPassword(sysadmin, DEFAULT_PASSWORD_ENCODED);
        userAccessor.updateUser(updatedSysadmin, true);
    }

    @Test
    public void testInitializeNoChange() throws AlertException {
        Environment environment = Mockito.mock(Environment.class);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);

        ConfigurationOverridesStartupComponent configurationOverridesStartupComponent = new ConfigurationOverridesStartupComponent(environmentVariableUtility, userAccessor, descriptorKey, configurationModelConfigurationAccessor,
            apiAction, configurationFieldModelConverter);
        // Update the sysadmin password
        Optional<UserModel> sysadminOptional = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID);
        assertTrue(sysadminOptional.isPresent());
        UserModel sysadmin = sysadminOptional.get();
        assertEquals(DEFAULT_PASSWORD_ENCODED, sysadmin.getPassword());
        UserModel updatedSysadmin = changeUserPassword(sysadmin, UPDATED_PASSWORD);
        userAccessor.updateUser(updatedSysadmin, false);

        // Run the initialize method
        configurationOverridesStartupComponent.initialize();

        // Verify the sysadmin password is the updated password
        sysadminOptional = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID);
        assertTrue(sysadminOptional.isPresent());
        sysadmin = sysadminOptional.get();
        assertNotEquals(DEFAULT_PASSWORD_ENCODED, sysadmin.getPassword());

        HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.when(servletRequest.getSession()).thenReturn(session);
        HttpServletResponse servletResponse = Mockito.mock(HttpServletResponse.class);

        // Try to login with the updated password
        LoginConfig updatedLoginConfig = new LoginConfig(DEFAULT_ADMIN_USER, UPDATED_PASSWORD);
        ActionResponse<Void> actionResponse = authenticationActions.authenticateUser(servletRequest, servletResponse, updatedLoginConfig);
        assertEquals(HttpStatus.NO_CONTENT, actionResponse.getHttpStatus());

        // Try to login with the default password
        LoginConfig defaultLoginConfig = new LoginConfig(DEFAULT_ADMIN_USER, DEFAULT_PASSWORD);
        actionResponse = authenticationActions.authenticateUser(servletRequest, servletResponse, defaultLoginConfig);
        assertEquals(HttpStatus.UNAUTHORIZED, actionResponse.getHttpStatus());
    }

    @Test
    public void testInitializeResetPassword() throws AlertException {
        Environment environment = Mockito.mock(Environment.class);
        Mockito.when(environment.getProperty(ConfigurationOverridesStartupComponent.ENV_VAR_ADMIN_USER_PASSWORD_RESET)).thenReturn("true");
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);

        ConfigurationOverridesStartupComponent configurationOverridesStartupComponent = new ConfigurationOverridesStartupComponent(environmentVariableUtility, userAccessor, descriptorKey, configurationModelConfigurationAccessor,
            apiAction, configurationFieldModelConverter);

        // Update the sysadmin password
        Optional<UserModel> sysadminOptional = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID);
        assertTrue(sysadminOptional.isPresent());
        UserModel sysadmin = sysadminOptional.get();
        assertEquals(DEFAULT_PASSWORD_ENCODED, sysadmin.getPassword());
        UserModel updatedSysadmin = changeUserPassword(sysadmin, UPDATED_PASSWORD);
        userAccessor.updateUser(updatedSysadmin, false);

        // Run the initialize method
        configurationOverridesStartupComponent.initialize();

        // Verify the sysadmin password is the default password
        sysadminOptional = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID);
        assertTrue(sysadminOptional.isPresent());
        sysadmin = sysadminOptional.get();
        assertEquals(DEFAULT_PASSWORD_ENCODED, sysadmin.getPassword());

        HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.when(servletRequest.getSession()).thenReturn(session);
        HttpServletResponse servletResponse = Mockito.mock(HttpServletResponse.class);

        // Try to login with the updated password
        LoginConfig updatedLoginConfig = new LoginConfig(DEFAULT_ADMIN_USER, UPDATED_PASSWORD);
        ActionResponse<Void> actionResponse = authenticationActions.authenticateUser(servletRequest, servletResponse, updatedLoginConfig);
        assertEquals(HttpStatus.UNAUTHORIZED, actionResponse.getHttpStatus());

        // Try to login with the default password
        LoginConfig defaultLoginConfig = new LoginConfig(DEFAULT_ADMIN_USER, DEFAULT_PASSWORD);
        actionResponse = authenticationActions.authenticateUser(servletRequest, servletResponse, defaultLoginConfig);
        assertEquals(HttpStatus.NO_CONTENT, actionResponse.getHttpStatus());
    }

    @Test
    public void testInitializeResetPasswordDifferentUsername() throws AlertException {
        Environment environment = Mockito.mock(Environment.class);
        Mockito.when(environment.getProperty(ConfigurationOverridesStartupComponent.ENV_VAR_ADMIN_USER_PASSWORD_RESET)).thenReturn("true");
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);

        ConfigurationOverridesStartupComponent configurationOverridesStartupComponent = new ConfigurationOverridesStartupComponent(environmentVariableUtility, userAccessor, descriptorKey, configurationModelConfigurationAccessor,
            apiAction, configurationFieldModelConverter);
        
        String newUsername = "UpdatedAdmin";

        // Update the sysadmin username and password
        Optional<UserModel> sysadminOptional = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID);
        assertTrue(sysadminOptional.isPresent());
        UserModel sysadmin = sysadminOptional.get();
        assertEquals(DEFAULT_PASSWORD_ENCODED, sysadmin.getPassword());
        UserModel updatedSysadmin = changeUserNameAndPassword(sysadmin, newUsername, UPDATED_PASSWORD);
        userAccessor.updateUser(updatedSysadmin, false);

        // Run the initialize method
        configurationOverridesStartupComponent.initialize();

        // Verify the sysadmin password is the default password
        sysadminOptional = userAccessor.getUser(UserAccessor.DEFAULT_ADMIN_USER_ID);
        assertTrue(sysadminOptional.isPresent());
        sysadmin = sysadminOptional.get();
        assertEquals(DEFAULT_PASSWORD_ENCODED, sysadmin.getPassword());

        HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.when(servletRequest.getSession()).thenReturn(session);
        HttpServletResponse servletResponse = Mockito.mock(HttpServletResponse.class);

        // Try to login with the updated password
        LoginConfig updatedLoginConfig = new LoginConfig(newUsername, UPDATED_PASSWORD);
        ActionResponse<Void> actionResponse = authenticationActions.authenticateUser(servletRequest, servletResponse, updatedLoginConfig);
        assertEquals(HttpStatus.UNAUTHORIZED, actionResponse.getHttpStatus());

        // Try to login with the default password
        LoginConfig defaultLoginConfig = new LoginConfig(newUsername, DEFAULT_PASSWORD);
        actionResponse = authenticationActions.authenticateUser(servletRequest, servletResponse, defaultLoginConfig);
        assertEquals(HttpStatus.NO_CONTENT, actionResponse.getHttpStatus());
    }

    private UserModel changeUserPassword(UserModel oldUserModel, String newPassword) {
        return UserModel.existingUser(oldUserModel.getId(), DEFAULT_ADMIN_USER,
            newPassword, oldUserModel.getEmailAddress(), oldUserModel.getAuthenticationType(), oldUserModel.getRoles(), oldUserModel.isEnabled());
    }

    private UserModel changeUserNameAndPassword(UserModel oldUserModel, String newUsername, String newPassword) {
        return UserModel.existingUser(oldUserModel.getId(), newUsername,
            newPassword, oldUserModel.getEmailAddress(), oldUserModel.getAuthenticationType(), oldUserModel.getRoles(), oldUserModel.isEnabled());
    }

}
