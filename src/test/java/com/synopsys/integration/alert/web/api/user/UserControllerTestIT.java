package com.synopsys.integration.alert.web.api.user;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.users.UserManagementDescriptorKey;
import com.synopsys.integration.alert.component.users.UserSystemValidator;
import com.synopsys.integration.alert.database.api.DefaultAuthenticationTypeAccessor;
import com.synopsys.integration.alert.database.api.DefaultAuthorizationAccessor;
import com.synopsys.integration.alert.database.api.DefaultUserAccessor;
import com.synopsys.integration.alert.database.user.UserRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

import junit.framework.AssertionFailedError;

public class UserControllerTestIT extends AlertIntegrationTest {
    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    @Autowired
    private UserRepository userRepository;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected HttpSessionCsrfTokenRepository csrfTokenRepository;

    @Autowired
    private Gson gson;

    @Autowired
    private UserManagementDescriptorKey userManagementDescriptorKey;
    @Autowired
    private DefaultUserAccessor defaultUserAccessor;
    @Autowired
    private DefaultAuthorizationAccessor defaultAuthorizationUtility;
    @Autowired
    private DefaultAuthenticationTypeAccessor defaultAuthenticationTypeAccessor;
    @Autowired
    private UserSystemValidator userSystemValidator;

    private UserActions userActions;
    private MockMvc mockMvc;

    private final Long id = 4L;
    private final String name = "user";
    private final String password = "password";
    private final String emailAddress = "noreply@synopsys.com";
    private final Set<UserRoleModel> roles = Set.of();
    private final AuthenticationType authenticationType = AuthenticationType.DATABASE;

    @BeforeEach
    public void init() {
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasCreatePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasDeletePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        Mockito.when(authorizationManager.hasExecutePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        userActions = new UserActions(userManagementDescriptorKey, defaultUserAccessor, defaultAuthorizationUtility, authorizationManager, defaultAuthenticationTypeAccessor, userSystemValidator);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @AfterEach
    public void cleanup() {
        userRepository.flush();
        userRepository.deleteAllInBatch();
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testCreate() throws Exception {
        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, password, emailAddress, roleNames, false, false, false, true, false, authenticationType.name(), false);

        String url = UserController.USER_BASE_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                    .content(gson.toJson(userConfig))
                                                    .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testGetOne() throws Exception {
        UserConfig userConfig = createDefaultUserConfig().orElseThrow(AssertionFailedError::new);

        String url = UserController.USER_BASE_PATH + String.format("/%s", userConfig.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testUpdate() throws Exception {
        UserConfig userConfig = createDefaultUserConfig().orElseThrow(AssertionFailedError::new);
        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig updatedUserConfig = new UserConfig(userConfig.getId(), userConfig.getUsername(), password, "newEmailAddress", roleNames, false, false, false, true, false, userConfig.getAuthenticationType(), false);

        String url = UserController.USER_BASE_PATH + String.format("/%s", userConfig.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                    .content(gson.toJson(updatedUserConfig))
                                                    .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testDelete() throws Exception {
        UserConfig userConfig = createDefaultUserConfig().orElseThrow(AssertionFailedError::new);

        String url = UserController.USER_BASE_PATH + String.format("/%s", userConfig.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testGetAll() throws Exception {
        String url = UserController.USER_BASE_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTest.ROLE_ALERT_ADMIN)
    public void testValidate() throws Exception {
        UserConfig userConfig = createDefaultUserConfig().orElseThrow(AssertionFailedError::new);
        String url = UserController.USER_BASE_PATH + "/validate";
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTest.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                    .content(gson.toJson(userConfig))
                                                    .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    private Optional<UserConfig> createDefaultUserConfig() {
        Set<String> roleNames = roles
                                    .stream()
                                    .map(UserRoleModel::getName)
                                    .collect(Collectors.toSet());
        UserConfig userConfig = new UserConfig(id.toString(), name, password, emailAddress, roleNames, false, false, false, true, false, authenticationType.name(), false);
        ActionResponse<UserConfig> savedUserConfig = userActions.create(userConfig);
        return savedUserConfig.getContent();
    }
}
