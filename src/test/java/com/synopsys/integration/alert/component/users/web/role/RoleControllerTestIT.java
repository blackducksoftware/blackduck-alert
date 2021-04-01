package com.synopsys.integration.alert.component.users.web.role;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.google.common.reflect.TypeToken;

import com.google.gson.Gson;
import com.synopsys.integration.alert.database.user.RoleRepository;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.util.AlertIntegrationTestConstants;

import junit.framework.AssertionFailedError;

@Transactional
@AlertIntegrationTest
public class RoleControllerTestIT {
    private final MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected HttpSessionCsrfTokenRepository csrfTokenRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private Gson gson;

    @Autowired
    private BlackDuckProviderKey blackDuckProviderKey;

    private MockMvc mockMvc;

    private final String roleName = "roleName";
    private final String context = "GLOBAL";

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @AfterEach
    public void cleanup() {
        roleRepository.flush();
        roleRepository.deleteAllInBatch();
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testCreate() throws Exception {
        PermissionModel permissionModel = new PermissionModel(blackDuckProviderKey.getUniversalKey(), context, true, true, true, true, true, true, true, true);
        RolePermissionModel rolePermissionModel = new RolePermissionModel(null, roleName, Set.of(permissionModel));

        String url = RoleController.ROLE_BASE_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                    .content(gson.toJson(rolePermissionModel))
                                                    .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testCreateDuplicate() throws Exception {
        PermissionModel permissionModel = new PermissionModel(blackDuckProviderKey.getUniversalKey(), context, true, true, true, true, true, true, true, true);
        RolePermissionModel rolePermissionModel = new RolePermissionModel(null, roleName, Set.of(permissionModel));

        String url = RoleController.ROLE_BASE_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                    .content(gson.toJson(rolePermissionModel))
                                                    .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated());

        PermissionModel permissionModel2 = new PermissionModel(blackDuckProviderKey.getUniversalKey(), context, true, true, true, true, true, true, true, true);
        RolePermissionModel rolePermissionModel2 = new RolePermissionModel(null, roleName, Set.of(permissionModel2));

        MockHttpServletRequestBuilder request2 = MockMvcRequestBuilders.post(new URI(url))
                                                     .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                     .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                     .content(gson.toJson(rolePermissionModel2))
                                                     .contentType(contentType);
        mockMvc.perform(request2).andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testGetOne() throws Exception {
        RolePermissionModel rolePermissionModel = createRolePermissionModel().orElseThrow(AssertionFailedError::new);

        String url = RoleController.ROLE_BASE_PATH + String.format("/%s", rolePermissionModel.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testUpdate() throws Exception {
        RolePermissionModel rolePermissionModel = createRolePermissionModel().orElseThrow(AssertionFailedError::new);
        PermissionModel permissionModel = new PermissionModel(blackDuckProviderKey.getUniversalKey(), context, false, false, false, false, false, false, false, false);
        RolePermissionModel updatedRolePermissionModel = new RolePermissionModel(rolePermissionModel.getId(), roleName, Set.of(permissionModel));

        String url = RoleController.ROLE_BASE_PATH + String.format("/%s", rolePermissionModel.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                    .content(gson.toJson(updatedRolePermissionModel))
                                                    .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testDelete() throws Exception {
        RolePermissionModel rolePermissionModel = createRolePermissionModel().orElseThrow(AssertionFailedError::new);

        String url = RoleController.ROLE_BASE_PATH + String.format("/%s", rolePermissionModel.getId());
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testGetAll() throws Exception {
        String url = RoleController.ROLE_BASE_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf());
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    public void testValidate() throws Exception {
        RolePermissionModel rolePermissionModel = createRolePermissionModel().orElseThrow(AssertionFailedError::new);

        String url = RoleController.ROLE_BASE_PATH + "/validate";
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                    .content(gson.toJson(rolePermissionModel))
                                                    .contentType(contentType);
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk());
    }

    private Optional<RolePermissionModel> createRolePermissionModel() throws Exception {
        PermissionModel permissionModel = new PermissionModel(blackDuckProviderKey.getUniversalKey(), context, true, true, true, true, true, true, true, true);
        RolePermissionModel rolePermissionModel = new RolePermissionModel(null, roleName, Set.of(permissionModel));

        String url = RoleController.ROLE_BASE_PATH;
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(new URI(url))
                                                    .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
                                                    .with(SecurityMockMvcRequestPostProcessors.csrf())
                                                    .content(gson.toJson(rolePermissionModel))
                                                    .contentType(contentType);
        MvcResult mvcResult = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isCreated()).andReturn();
        String response = mvcResult.getResponse().getContentAsString();

        TypeToken rolePermissionModelType = new TypeToken<RolePermissionModel>() {};
        RolePermissionModel newRolePermissionModel = gson.fromJson(response, rolePermissionModelType.getType());

        return Optional.of(newRolePermissionModel);
    }

}
