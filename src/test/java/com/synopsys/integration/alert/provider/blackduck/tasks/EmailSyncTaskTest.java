package com.synopsys.integration.alert.provider.blackduck.tasks;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckUserRepositoryAccessor;
import com.synopsys.integration.blackduck.api.core.HubPathMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.service.HubService;

public class EmailSyncTaskTest extends SyncTaskSharedTest {

    @Test
    public void testRunInitial() throws Exception {
        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();

        final HubService hubService = createMockHubService(blackDuckProperties);

        final List<UserView> userViews = new ArrayList<>();
        userViews.add(createUserView("email"));
        userViews.add(createUserView("email_two"));
        userViews.add(createUserView("email_three"));

        Mockito.when(hubService.getAllResponses(Mockito.any(HubPathMultipleResponses.class))).thenReturn(userViews);

        final EmailSyncTask emailSyncTask = new EmailSyncTask(null, blackDuckProperties, blackDuckUserRepositoryAccessor);
        emailSyncTask.run();

        assertEquals(3, blackDuckUserRepositoryAccessor.readEntities().size());
    }

    @Test
    public void testRunAdd() throws Exception {
        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();

        final HubService hubService = createMockHubService(blackDuckProperties);

        final List<UserView> userViews = new ArrayList<>();
        userViews.add(createUserView("email"));
        userViews.add(createUserView("email_two"));

        Mockito.when(hubService.getAllResponses(Mockito.any(HubPathMultipleResponses.class))).thenReturn(userViews);

        final EmailSyncTask emailSyncTask = new EmailSyncTask(null, blackDuckProperties, blackDuckUserRepositoryAccessor);
        emailSyncTask.run();
        assertEquals(2, blackDuckUserRepositoryAccessor.readEntities().size());

        userViews.add(createUserView("email_three"));
        Mockito.when(hubService.getAllResponses(Mockito.any(HubPathMultipleResponses.class))).thenReturn(userViews);

        emailSyncTask.run();
        assertEquals(3, blackDuckUserRepositoryAccessor.readEntities().size());
    }

    @Test
    public void testRunDelete() throws Exception {
        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();

        final HubService hubService = createMockHubService(blackDuckProperties);

        final List<UserView> userViews = new ArrayList<>();
        userViews.add(createUserView("email"));
        userViews.add(createUserView("email_two"));
        final UserView userToDelete = createUserView("email_three");
        userViews.add(userToDelete);

        Mockito.when(hubService.getAllResponses(Mockito.any(HubPathMultipleResponses.class))).thenReturn(userViews);

        final EmailSyncTask emailSyncTask = new EmailSyncTask(null, blackDuckProperties, blackDuckUserRepositoryAccessor);
        emailSyncTask.run();
        assertEquals(3, blackDuckUserRepositoryAccessor.readEntities().size());

        userViews.remove(userToDelete);
        Mockito.when(hubService.getAllResponses(Mockito.any(HubPathMultipleResponses.class))).thenReturn(userViews);

        emailSyncTask.run();
        assertEquals(2, blackDuckUserRepositoryAccessor.readEntities().size());
    }
}
