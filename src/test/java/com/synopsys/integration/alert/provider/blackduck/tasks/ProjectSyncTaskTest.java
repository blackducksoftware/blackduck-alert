package com.synopsys.integration.alert.provider.blackduck.tasks;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckGroupRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockUserGroupRelationRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockUserProjectRelationRepositoryAccessor;
import com.synopsys.integration.blackduck.api.core.HubPathMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.response.AssignedUserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.AssignedUserView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.service.HubService;

public class ProjectSyncTaskTest extends SyncTaskSharedTest {

    @Test
    public void testRunInitial() throws Exception {
        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();
        final MockBlackDuckGroupRepositoryAccessor blackDuckGroupRepositoryAccessor = new MockBlackDuckGroupRepositoryAccessor();
        final MockUserGroupRelationRepositoryAccessor userGroupRelationRepositoryAccessor = new MockUserGroupRelationRepositoryAccessor();
        final MockBlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();
        final MockUserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor = new MockUserProjectRelationRepositoryAccessor();

        final HubService hubService = createMockHubService(blackDuckProperties);

        final ProjectView projectView = createProjectView("project", "description1", "projectUrl1");
        final ProjectView projectView2 = createProjectView("project2", "description2", "projectUrl2");
        final ProjectView projectView3 = createProjectView("project3", "description3", "projectUrl3");

        final List<ProjectView> projectViews = Arrays.asList(projectView, projectView2, projectView3);

        Mockito.when(hubService.getAllResponses(Mockito.any(HubPathMultipleResponses.class))).thenReturn(projectViews);

        final AssignedUserGroupView userGroupView1 = createAssignedUserGroupView("group", true);
        final AssignedUserGroupView userGroupView2 = createAssignedUserGroupView("group_two", false);
        final AssignedUserGroupView userGroupView3 = createAssignedUserGroupView("group_three", true);

        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView), ArgumentMatchers.same(ProjectView.USERGROUPS_LINK_RESPONSE))).thenReturn(Arrays.asList(userGroupView1, userGroupView2, userGroupView3));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView), ArgumentMatchers.same(ProjectView.USERGROUPS_LINK_RESPONSE))).thenReturn(Arrays.asList(userGroupView3));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView), ArgumentMatchers.same(ProjectView.USERGROUPS_LINK_RESPONSE))).thenReturn(Arrays.asList(userGroupView2));

        final AssignedUserView user1 = createAssignedUserView("user1@email.com");
        final AssignedUserView user2 = createAssignedUserView("user2@email.com");
        final AssignedUserView user3 = createAssignedUserView("user3@email.com");
        final AssignedUserView user4 = createAssignedUserView("user4@email.com");

        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView), ArgumentMatchers.same(ProjectView.USERS_LINK_RESPONSE))).thenReturn(Arrays.asList(user1, user2, user3));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView), ArgumentMatchers.same(ProjectView.USERS_LINK_RESPONSE))).thenReturn(Arrays.asList(user3));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView), ArgumentMatchers.same(ProjectView.USERS_LINK_RESPONSE))).thenReturn(Arrays.asList(user2, user4));

        final ProjectSyncTask projectSyncTask = new ProjectSyncTask(null, blackDuckProperties, blackDuckUserRepositoryAccessor, blackDuckGroupRepositoryAccessor, userGroupRelationRepositoryAccessor, blackDuckProjectRepositoryAccessor,
            userProjectRelationRepositoryAccessor);
        projectSyncTask.run();

        assertEquals(3, blackDuckProjectRepositoryAccessor.readEntities().size());
        //No relations because there are no Users in the database yet
        assertEquals(0, userProjectRelationRepositoryAccessor.getUserProjectRelations().size());
    }

    //    @Test
    //    public void testRunAfterEmailAndGroupSync() throws Exception {
    //        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
    //
    //        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();
    //        final MockBlackDuckGroupRepositoryAccessor blackDuckGroupRepositoryAccessor = new MockBlackDuckGroupRepositoryAccessor();
    //        final MockUserGroupRelationRepositoryAccessor userGroupRelationRepositoryAccessor = new MockUserGroupRelationRepositoryAccessor();
    //
    //        final String email1 = "user1@email.com";
    //        final String email2 = "user2@email.com";
    //        final String email3 = "user3@email.com";
    //        final String email4 = "user4@email.com";
    //        // Populate the user table As if the email sync task already ran
    //        blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email1, false));
    //        blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email2, false));
    //        blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email3, false));
    //        blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email4, false));
    //
    //        final HubService hubService = createMockHubService(blackDuckProperties);
    //
    //        final AssignedUserGroupView userGroupView1 = createAssignedUserGroupView("group", true);
    //        final AssignedUserGroupView userGroupView2 = createAssignedUserGroupView("group_two", false);
    //
    //        final List<AssignedUserGroupView> userGroupViews = Arrays.asList(userGroupView1, userGroupView2);
    //
    //        Mockito.when(hubService.getAllResponses(Mockito.any(HubPathMultipleResponses.class))).thenReturn(userGroupViews);
    //
    //        final AssignedUserView user1 = createAssignedUserView(email1);
    //        final AssignedUserView user2 = createAssignedUserView(email1);
    //        final AssignedUserView user3 = createAssignedUserView(email2);
    //        final AssignedUserView user4 = createAssignedUserView(email3);
    //        final AssignedUserView user5 = createAssignedUserView(email4);
    //
    //        final ProjectSyncTask projectSyncTask = new ProjectSyncTask(null, blackDuckProperties, blackDuckUserRepositoryAccessor, blackDuckGroupRepositoryAccessor, userGroupRelationRepositoryAccessor);
    //        projectSyncTask.run();
    //
    //        assertEquals(3, blackDuckGroupRepositoryAccessor.readEntities().size());
    //        //No relations because there are no Users in the database yet
    //        assertEquals(6, userGroupRelationRepositoryAccessor.getUserGroupRelationSet().size());
    //
    //        Mockito.when(hubService.getAllResponses(Mockito.any(HubPathMultipleResponses.class))).thenReturn(Arrays.asList(userGroupView1, userGroupView2));
    //        projectSyncTask.run();
    //
    //        assertEquals(2, blackDuckGroupRepositoryAccessor.readEntities().size());
    //        //No relations because there are no Users in the database yet
    //        assertEquals(4, userGroupRelationRepositoryAccessor.getUserGroupRelationSet().size());
    //    }

    public AssignedUserView createAssignedUserView(final String email) {
        final AssignedUserView userView = new AssignedUserView();
        userView.email = email;
        return userView;
    }

    public AssignedUserGroupView createAssignedUserGroupView(final String name, final Boolean active) {
        final AssignedUserGroupView userGroupView = new AssignedUserGroupView();
        userGroupView.name = name;
        userGroupView.active = active;
        return userGroupView;
    }
}
