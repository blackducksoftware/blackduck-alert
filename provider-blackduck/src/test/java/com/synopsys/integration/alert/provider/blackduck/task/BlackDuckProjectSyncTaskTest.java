package com.synopsys.integration.alert.provider.blackduck.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.MockProviderDataAccessor;
import com.synopsys.integration.blackduck.api.core.ResourceLink;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectUsersService;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckProjectSyncTaskTest {
    @Test
    public void testRun() throws Exception {
        MockProviderDataAccessor providerDataAccessor = new MockProviderDataAccessor();

        ApiDiscovery apiDiscovery = new ApiDiscovery(new HttpUrl("https://someblackduckserver"));
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        ProjectUsersService projectUsersService = Mockito.mock(ProjectUsersService.class);

        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        Mockito.when(blackDuckServicesFactory.getBlackDuckApiClient()).thenReturn(blackDuckApiClient);
        Mockito.when(blackDuckServicesFactory.createProjectUsersService()).thenReturn(projectUsersService);
        Mockito.when(blackDuckServicesFactory.getApiDiscovery()).thenReturn(apiDiscovery);

        Long providerConfigId = 1000L;
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckProperties.getConfigId()).thenReturn(providerConfigId);
        Mockito.when(blackDuckProperties.createBlackDuckHttpClientAndLogErrors(Mockito.any())).thenReturn(Optional.of(Mockito.mock(BlackDuckHttpClient.class)));
        Mockito.when(blackDuckProperties.createBlackDuckServicesFactory(Mockito.any(), Mockito.any())).thenReturn(blackDuckServicesFactory);

        ProjectView projectView = createProjectView("project", "description1", "https://projectUrl1");
        ProjectView projectView2 = createProjectView("project2", "description2", "https://projectUrl2");
        ProjectView projectView3 = createProjectView("project3", "description3", "https://projectUrl3");

        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.eq(apiDiscovery.metaProjectsLink()))).thenReturn(List.of(projectView, projectView2, projectView3));
        Mockito.doReturn(null).when(blackDuckApiClient).getResponse(Mockito.any(BlackDuckRequest.class));

        String email1 = "user1@email.com";
        String email2 = "user2@email.com";
        String email3 = "user3@email.com";
        String email4 = "user4@email.com";
        UserView user1 = createUserView(email1, true);
        UserView user2 = createUserView(email2, true);
        UserView user3 = createUserView(email3, true);
        UserView user4 = createUserView(email4, true);

        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.eq(apiDiscovery.metaUsersLink()))).thenReturn(List.of(user1, user2, user3, user4));

        Mockito.when(projectUsersService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView))).thenReturn(new HashSet<>(List.of(user2, user4)));
        Mockito.when(projectUsersService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView2))).thenReturn(new HashSet<>(List.of(user3)));
        Mockito.when(projectUsersService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView3))).thenReturn(new HashSet<>(List.of(user1, user2, user3)));
        Mockito.doNothing().when(projectUsersService).addUserToProject(Mockito.any(), Mockito.any(UserView.class));

        BlackDuckDataSyncTask projectSyncTask = new BlackDuckDataSyncTask(new BlackDuckProviderKey(), null, providerDataAccessor, blackDuckProperties);
        projectSyncTask.run();

        assertEquals(3, providerDataAccessor.getProjectsByProviderConfigId(providerConfigId).size());
        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.eq(apiDiscovery.metaProjectsLink()))).thenReturn(List.of(projectView, projectView2));

        Mockito.when(projectUsersService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView))).thenReturn(new HashSet<>(List.of(user2, user4)));
        Mockito.when(projectUsersService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView2))).thenReturn(new HashSet<>(List.of(user3)));

        Mockito.when(blackDuckApiClient.getAllResponses(ArgumentMatchers.same(projectView2.metaUsersLink()))).thenReturn(List.of());
        projectSyncTask = new BlackDuckDataSyncTask(new BlackDuckProviderKey(), null, providerDataAccessor, blackDuckProperties);
        projectSyncTask.run();

        assertEquals(2, providerDataAccessor.getProjectsByProviderConfigId(providerConfigId).size());
    }

    private UserView createUserView(String email, Boolean active) {
        UserView userView = new UserView();
        userView.setEmail(email);
        userView.setActive(active);
        return userView;
    }

    private ProjectView createProjectView(String name, String description, String href) throws IntegrationException {
        HttpUrl projectUrl = new HttpUrl(href);

        ResourceLink usersLink = new ResourceLink();
        usersLink.setRel(ProjectView.USERS_LINK);
        usersLink.setHref(projectUrl.appendRelativeUrl(ProjectView.USERS_LINK));

        ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setHref(new HttpUrl(href));
        resourceMetadata.setLinks(List.of(usersLink));

        ProjectView projectView = new ProjectView();
        projectView.setName(name);
        projectView.setDescription(description);
        projectView.setMeta(resourceMetadata);

        return projectView;
    }

}
