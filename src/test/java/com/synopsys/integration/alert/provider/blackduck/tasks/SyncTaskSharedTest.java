package com.synopsys.integration.alert.provider.blackduck.tasks;

import java.util.Optional;

import org.mockito.Mockito;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.HubServicesFactory;

public class SyncTaskSharedTest {

    public HubService createMockHubService(final BlackDuckProperties mockBlackDuckProperties) {
        Mockito.when(mockBlackDuckProperties.createRestConnectionAndLogErrors(Mockito.any())).thenReturn(Optional.of(Mockito.mock(BlackduckRestConnection.class)));
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        Mockito.when(mockBlackDuckProperties.createBlackDuckServicesFactory(Mockito.any(), Mockito.any())).thenReturn(hubServicesFactory);

        final HubService hubService = Mockito.mock(HubService.class);
        Mockito.when(hubServicesFactory.createHubService()).thenReturn(hubService);
        return hubService;
    }

    public UserView createUserView(final String email) {
        final UserView userView = new UserView();
        userView.email = email;
        return userView;
    }

    public UserGroupView createUserGroupView(final String name, final Boolean active, final String href) {
        final UserGroupView userGroupView = new UserGroupView();
        userGroupView.name = name;
        userGroupView.active = active;
        final ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.href = href;
        userGroupView._meta = resourceMetadata;
        return userGroupView;
    }

    public ProjectView createProjectView(final String name, final String description, final String href) {
        final ProjectView projectView = new ProjectView();
        projectView.name = name;
        projectView.description = description;
        final ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.href = href;
        projectView._meta = resourceMetadata;
        return projectView;
    }

}
