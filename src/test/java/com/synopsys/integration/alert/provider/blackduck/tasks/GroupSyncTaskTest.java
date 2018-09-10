package com.synopsys.integration.alert.provider.blackduck.tasks;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckGroupRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockUserGroupRelationRepositoryAccessor;
import com.synopsys.integration.blackduck.api.core.HubPathMultipleResponses;
import com.synopsys.integration.blackduck.api.core.LinkMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.service.HubService;

public class GroupSyncTaskTest extends SyncTaskSharedTest {

    @Test
    public void testRunInitial() throws Exception {
        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();
        final MockBlackDuckGroupRepositoryAccessor blackDuckGroupRepositoryAccessor = new MockBlackDuckGroupRepositoryAccessor();
        final MockUserGroupRelationRepositoryAccessor userGroupRelationRepositoryAccessor = new MockUserGroupRelationRepositoryAccessor();

        final HubService hubService = createMockHubService(blackDuckProperties);

        final UserGroupView userGroupView1 = createUserGroupView("group", true, "url1");
        final UserGroupView userGroupView2 = createUserGroupView("group_two", false, "url2");
        final UserGroupView userGroupView3 = createUserGroupView("group_three", true, "url3");

        final List<UserGroupView> userGroupViews = Arrays.asList(userGroupView1, userGroupView2, userGroupView3);

        Mockito.when(hubService.getAllResponses(Mockito.any(HubPathMultipleResponses.class))).thenReturn(userGroupViews);

        final UserView user1 = createUserView("user1@email.com");
        final UserView user2 = createUserView("user1@email.com");
        final UserView user3 = createUserView("user2@email.com");
        final UserView user4 = createUserView("user3@email.com");
        final UserView user5 = createUserView("user4@email.com");

        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(userGroupView1), Mockito.any(LinkMultipleResponses.class))).thenReturn(Arrays.asList(user1, user3));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(userGroupView1), Mockito.any(LinkMultipleResponses.class))).thenReturn(Arrays.asList(user1, user2, user4));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(userGroupView1), Mockito.any(LinkMultipleResponses.class))).thenReturn(Arrays.asList(user4, user5));

        final GroupSyncTask groupSyncTask = new GroupSyncTask(null, blackDuckProperties, blackDuckUserRepositoryAccessor, blackDuckGroupRepositoryAccessor, userGroupRelationRepositoryAccessor);
        groupSyncTask.run();

        assertEquals(3, blackDuckGroupRepositoryAccessor.readEntities().size());
        //No relations because there are no Users in the database yet
        assertEquals(0, userGroupRelationRepositoryAccessor.getUserGroupRelationSet().size());
    }

    @Test
    public void testRunAfterEmailSync() throws Exception {
        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();
        final MockBlackDuckGroupRepositoryAccessor blackDuckGroupRepositoryAccessor = new MockBlackDuckGroupRepositoryAccessor();
        final MockUserGroupRelationRepositoryAccessor userGroupRelationRepositoryAccessor = new MockUserGroupRelationRepositoryAccessor();

        final String email1 = "user1@email.com";
        final String email2 = "user2@email.com";
        final String email3 = "user3@email.com";
        final String email4 = "user4@email.com";
        // Populate the user table As if the email sync task already ran
        blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email1, false));
        blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email2, false));
        blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email3, false));
        blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email4, false));

        final HubService hubService = createMockHubService(blackDuckProperties);

        final UserGroupView userGroupView1 = createUserGroupView("group", true, "url1");
        final UserGroupView userGroupView2 = createUserGroupView("group_two", false, "url2");
        final UserGroupView userGroupView3 = createUserGroupView("group_three", true, "url3");

        final List<UserGroupView> userGroupViews = Arrays.asList(userGroupView1, userGroupView2, userGroupView3);

        Mockito.when(hubService.getAllResponses(Mockito.any(HubPathMultipleResponses.class))).thenReturn(userGroupViews);

        final UserView user1 = createUserView(email1);
        final UserView user2 = createUserView(email1);
        final UserView user3 = createUserView(email2);
        final UserView user4 = createUserView(email3);
        final UserView user5 = createUserView(email4);

        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(userGroupView1), Mockito.any(LinkMultipleResponses.class))).thenReturn(Arrays.asList(user1, user2, user3));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(userGroupView2), Mockito.any(LinkMultipleResponses.class))).thenReturn(Arrays.asList(user1, user2, user4));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(userGroupView3), Mockito.any(LinkMultipleResponses.class))).thenReturn(Arrays.asList(user4, user5));

        final GroupSyncTask groupSyncTask = new GroupSyncTask(null, blackDuckProperties, blackDuckUserRepositoryAccessor, blackDuckGroupRepositoryAccessor, userGroupRelationRepositoryAccessor);
        groupSyncTask.run();

        assertEquals(3, blackDuckGroupRepositoryAccessor.readEntities().size());
        assertEquals(6, userGroupRelationRepositoryAccessor.getUserGroupRelationSet().size());

        Mockito.when(hubService.getAllResponses(Mockito.any(HubPathMultipleResponses.class))).thenReturn(Arrays.asList(userGroupView1, userGroupView2));
        groupSyncTask.run();

        assertEquals(2, blackDuckGroupRepositoryAccessor.readEntities().size());
        assertEquals(4, userGroupRelationRepositoryAccessor.getUserGroupRelationSet().size());
    }

}
