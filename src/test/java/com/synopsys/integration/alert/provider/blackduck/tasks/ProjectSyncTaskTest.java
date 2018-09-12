package com.synopsys.integration.alert.provider.blackduck.tasks;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockUserProjectRelationRepositoryAccessor;
import com.synopsys.integration.blackduck.api.core.HubPathMultipleResponses;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.response.AssignedUserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.AssignedUserView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.HubServicesFactory;

public class ProjectSyncTaskTest {

    @Test
    public void testRun() throws Exception {
        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();
        final MockBlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();
        final MockUserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor = new MockUserProjectRelationRepositoryAccessor();

        final String email1 = "user1@email.com";
        final String email2 = "user2@email.com";
        final String email3 = "user3@email.com";
        final String email4 = "user4@email.com";

        final String group1 = "group";
        final String group2 = "group_two";

        final String groupURL1 = "groupURL1";
        final String groupURL2 = "groupURL2";

        final HubService hubService = createMockHubService(blackDuckProperties);

        final ProjectView projectView = createProjectView("project", "description1", "projectUrl1");
        final ProjectView projectView2 = createProjectView("project2", "description2", "projectUrl2");
        final ProjectView projectView3 = createProjectView("project3", "description3", "projectUrl3");

        Mockito.when(hubService.getAllResponses(Mockito.any(HubPathMultipleResponses.class))).thenReturn(Arrays.asList(projectView, projectView2, projectView3));

        final AssignedUserGroupView assignedUserGroupView1 = createAssignedUserGroupView(group1, true, groupURL1);
        final AssignedUserGroupView assignedUserGroupView2 = createAssignedUserGroupView(group2, true, groupURL2);

        final UserGroupView userGroupView1 = createUserGroupView(group1, true, groupURL1);
        final UserGroupView userGroupView2 = createUserGroupView(group2, true, groupURL2);

        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView), ArgumentMatchers.same(ProjectView.USERGROUPS_LINK_RESPONSE))).thenReturn(Arrays.asList(assignedUserGroupView1, assignedUserGroupView2));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView2), ArgumentMatchers.same(ProjectView.USERGROUPS_LINK_RESPONSE))).thenReturn(Collections.emptyList());
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView3), ArgumentMatchers.same(ProjectView.USERGROUPS_LINK_RESPONSE))).thenReturn(Arrays.asList(assignedUserGroupView2));

        Mockito.when(hubService.getResponse(ArgumentMatchers.same(groupURL1), ArgumentMatchers.same(UserGroupView.class))).thenReturn(userGroupView1);
        Mockito.when(hubService.getResponse(ArgumentMatchers.same(groupURL2), ArgumentMatchers.same(UserGroupView.class))).thenReturn(userGroupView2);

        final UserView user1 = createUserView(email1, true);
        final UserView user2 = createUserView(email2, true);
        final UserView user3 = createUserView(email3, true);
        final UserView user4 = createUserView(email4, true);
        final UserView user5 = createUserView("NOT ACTIVE", false);
        final UserView user6 = createUserView("DOESNT MATTER", false);

        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(userGroupView1), ArgumentMatchers.same(UserGroupView.USERS_LINK_RESPONSE))).thenReturn(Arrays.asList(user1, user2, user3, user5, user6));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(userGroupView2), ArgumentMatchers.same(UserGroupView.USERS_LINK_RESPONSE))).thenReturn(Arrays.asList(user2, user4, user5, user6));

        final AssignedUserView assignedUser1 = createAssignedUserView(email1, true);
        final AssignedUserView assignedUser2 = createAssignedUserView(email2, true);
        final AssignedUserView assignedUser3 = createAssignedUserView(email3, true);
        final AssignedUserView assignedUser4 = createAssignedUserView(email4, true);
        final AssignedUserView assignedUser5 = createAssignedUserView("NOT ACTIVE", false);
        final AssignedUserView assignedUser6 = createAssignedUserView("DOESNT MATTER", false);

        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView), ArgumentMatchers.same(ProjectView.USERS_LINK_RESPONSE))).thenReturn(Arrays.asList(assignedUser2, assignedUser4, assignedUser5));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView2), ArgumentMatchers.same(ProjectView.USERS_LINK_RESPONSE))).thenReturn(Arrays.asList(assignedUser3));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView3), ArgumentMatchers.same(ProjectView.USERS_LINK_RESPONSE))).thenReturn(Arrays.asList(assignedUser1, assignedUser2, assignedUser3, assignedUser6));

        final ProjectSyncTask projectSyncTask = new ProjectSyncTask(null, blackDuckProperties, blackDuckUserRepositoryAccessor, blackDuckProjectRepositoryAccessor,
            userProjectRelationRepositoryAccessor);
        projectSyncTask.run();

        assertEquals(4, blackDuckUserRepositoryAccessor.readEntities().size());
        assertEquals(3, blackDuckProjectRepositoryAccessor.readEntities().size());
        assertEquals(9, userProjectRelationRepositoryAccessor.readEntities().size());

        Mockito.when(hubService.getAllResponses(Mockito.any(HubPathMultipleResponses.class))).thenReturn(Arrays.asList(projectView, projectView2));

        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(userGroupView1), ArgumentMatchers.same(UserGroupView.USERS_LINK_RESPONSE))).thenReturn(Arrays.asList(user1, user2, user5, user6));

        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView2), ArgumentMatchers.same(ProjectView.USERS_LINK_RESPONSE))).thenReturn(Collections.emptyList());
        projectSyncTask.run();

        assertEquals(3, blackDuckUserRepositoryAccessor.readEntities().size());
        assertEquals(2, blackDuckProjectRepositoryAccessor.readEntities().size());
        assertEquals(3, userProjectRelationRepositoryAccessor.readEntities().size());
    }

    public HubService createMockHubService(final BlackDuckProperties mockBlackDuckProperties) {
        Mockito.when(mockBlackDuckProperties.createRestConnectionAndLogErrors(Mockito.any())).thenReturn(Optional.of(Mockito.mock(BlackduckRestConnection.class)));
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        Mockito.when(mockBlackDuckProperties.createBlackDuckServicesFactory(Mockito.any(), Mockito.any())).thenReturn(hubServicesFactory);

        final HubService hubService = Mockito.mock(HubService.class);
        Mockito.when(hubServicesFactory.createHubService()).thenReturn(hubService);
        return hubService;
    }

    public AssignedUserView createAssignedUserView(final String email, final Boolean active) {
        final AssignedUserView userView = new AssignedUserView();
        userView.email = email;
        userView.active = active;
        return userView;
    }

    public UserView createUserView(final String email, final Boolean active) {
        final UserView userView = new UserView();
        userView.email = email;
        userView.active = active;
        return userView;
    }

    public AssignedUserGroupView createAssignedUserGroupView(final String name, final Boolean active, final String href) {
        final AssignedUserGroupView userGroupView = new AssignedUserGroupView();
        userGroupView.name = name;
        userGroupView.active = active;
        userGroupView.group = href;
        return userGroupView;
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
