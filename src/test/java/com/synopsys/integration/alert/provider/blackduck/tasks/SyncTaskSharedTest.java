package com.synopsys.integration.alert.provider.blackduck.tasks;

import java.util.Optional;

import org.mockito.Mockito;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
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

}
