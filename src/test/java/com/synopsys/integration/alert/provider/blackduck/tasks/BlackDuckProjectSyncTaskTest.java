package com.synopsys.integration.alert.provider.blackduck.tasks;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.mock.MockProviderDataAccessor;
import com.synopsys.integration.blackduck.api.core.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.core.BlackDuckPathSingleResponse;
import com.synopsys.integration.blackduck.api.generated.component.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectUsersService;

public class BlackDuckProjectSyncTaskTest {

    @Test
    public void testRun() throws Exception {
        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        final MockProviderDataAccessor providerDataAccessor = new MockProviderDataAccessor();

        final ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT);
        configurationFieldModel.setFieldValues(List.of("project", "project2"));
        final ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, ConfigContextEnum.DISTRIBUTION);
        configurationModel.put(configurationFieldModel);
        final ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(UUID.randomUUID(), Set.of(configurationModel));
        Mockito.when(configurationAccessor.getAllJobs()).thenReturn(List.of(configurationJobModel));

        final String email1 = "user1@email.com";
        final String email2 = "user2@email.com";
        final String email3 = "user3@email.com";
        final String email4 = "user4@email.com";

        Mockito.when(blackDuckProperties.createBlackDuckHttpClientAndLogErrors(Mockito.any())).thenReturn(Optional.of(Mockito.mock(BlackDuckHttpClient.class)));
        final BlackDuckServicesFactory BlackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        Mockito.when(blackDuckProperties.createBlackDuckServicesFactory(Mockito.any(), Mockito.any())).thenReturn(BlackDuckServicesFactory);

        final BlackDuckService hubService = Mockito.mock(BlackDuckService.class);
        Mockito.when(BlackDuckServicesFactory.createBlackDuckService()).thenReturn(hubService);

        final ProjectUsersService projectUsersService = Mockito.mock(ProjectUsersService.class);
        Mockito.when(BlackDuckServicesFactory.createProjectUsersService()).thenReturn(projectUsersService);

        final ProjectView projectView = createProjectView("project", "description1", "projectUrl1");
        final ProjectView projectView2 = createProjectView("project2", "description2", "projectUrl2");
        final ProjectView projectView3 = createProjectView("project3", "description3", "projectUrl3");

        Mockito.when(hubService.getAllResponses(Mockito.any(BlackDuckPathMultipleResponses.class))).thenReturn(Arrays.asList(projectView, projectView2, projectView3));
        Mockito.doReturn(null).when(hubService).getResponse(Mockito.any(BlackDuckPathSingleResponse.class));

        final UserView user1 = createUserView(email1, true);
        final UserView user2 = createUserView(email2, true);
        final UserView user3 = createUserView(email3, true);
        final UserView user4 = createUserView(email4, true);

        Mockito.when(projectUsersService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView))).thenReturn(new HashSet<>(Arrays.asList(user2, user4)));
        Mockito.when(projectUsersService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView2))).thenReturn(new HashSet<>(Arrays.asList(user3)));
        Mockito.when(projectUsersService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView3))).thenReturn(new HashSet<>(Arrays.asList(user1, user2, user3)));
        Mockito.doNothing().when(projectUsersService).addUserToProject(Mockito.any(), Mockito.any(UserView.class));

        final BlackDuckProjectSyncTask projectSyncTask = new BlackDuckProjectSyncTask(null, blackDuckProperties, providerDataAccessor, configurationAccessor);
        projectSyncTask.run();

        assertEquals(3, providerDataAccessor.findByProviderName(BlackDuckProvider.COMPONENT_NAME).size());

        Mockito.when(hubService.getAllResponses(Mockito.any(BlackDuckPathMultipleResponses.class))).thenReturn(Arrays.asList(projectView, projectView2));

        Mockito.when(projectUsersService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView))).thenReturn(new HashSet<>(Arrays.asList(user2, user4)));
        Mockito.when(projectUsersService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView2))).thenReturn(new HashSet<>(Arrays.asList(user3)));

        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView2), ArgumentMatchers.same(ProjectView.USERS_LINK_RESPONSE))).thenReturn(Collections.emptyList());
        projectSyncTask.run();

        assertEquals(2, providerDataAccessor.findByProviderName(BlackDuckProvider.COMPONENT_NAME).size());
    }

    public UserView createUserView(final String email, final Boolean active) {
        final UserView userView = new UserView();
        userView.setEmail(email);
        userView.setActive(active);
        return userView;
    }

    public ProjectView createProjectView(final String name, final String description, final String href) {
        final ProjectView projectView = new ProjectView();
        projectView.setName(name);
        projectView.setDescription(description);
        final ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setHref(href);
        projectView.setMeta(resourceMetadata);
        return projectView;
    }

}
