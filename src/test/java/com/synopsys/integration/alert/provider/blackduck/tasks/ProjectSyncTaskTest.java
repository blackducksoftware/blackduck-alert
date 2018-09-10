package com.synopsys.integration.alert.provider.blackduck.tasks;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckGroupEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
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
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView2), ArgumentMatchers.same(ProjectView.USERGROUPS_LINK_RESPONSE))).thenReturn(Arrays.asList(userGroupView3));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView3), ArgumentMatchers.same(ProjectView.USERGROUPS_LINK_RESPONSE))).thenReturn(Arrays.asList(userGroupView2));

        final AssignedUserView user1 = createAssignedUserView("user1@email.com");
        final AssignedUserView user2 = createAssignedUserView("user2@email.com");
        final AssignedUserView user3 = createAssignedUserView("user3@email.com");
        final AssignedUserView user4 = createAssignedUserView("user4@email.com");

        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView3), ArgumentMatchers.same(ProjectView.USERS_LINK_RESPONSE))).thenReturn(Arrays.asList(user1, user2, user3));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView2), ArgumentMatchers.same(ProjectView.USERS_LINK_RESPONSE))).thenReturn(Arrays.asList(user3));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView), ArgumentMatchers.same(ProjectView.USERS_LINK_RESPONSE))).thenReturn(Arrays.asList(user2, user4));

        final ProjectSyncTask projectSyncTask = new ProjectSyncTask(null, blackDuckProperties, blackDuckUserRepositoryAccessor, blackDuckGroupRepositoryAccessor, userGroupRelationRepositoryAccessor, blackDuckProjectRepositoryAccessor,
            userProjectRelationRepositoryAccessor);
        projectSyncTask.run();

        assertEquals(3, blackDuckProjectRepositoryAccessor.readEntities().size());
        //No relations because there are no Users in the database yet
        assertEquals(0, userProjectRelationRepositoryAccessor.getUserProjectRelations().size());
    }

    @Test
    public void testRunAfterEmailAndGroupSync() throws Exception {
        final BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);

        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();
        final MockBlackDuckGroupRepositoryAccessor blackDuckGroupRepositoryAccessor = new MockBlackDuckGroupRepositoryAccessor();
        final MockUserGroupRelationRepositoryAccessor userGroupRelationRepositoryAccessor = new MockUserGroupRelationRepositoryAccessor();
        final MockBlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();
        final MockUserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor = new MockUserProjectRelationRepositoryAccessor();

        final String email1 = "user1@email.com";
        final String email2 = "user2@email.com";
        final String email3 = "user3@email.com";
        final String email4 = "user4@email.com";

        final String group1 = "group";
        final String group2 = "group_two";

        // Setup the databases as if the email and group tasks have already run //
        addGroupWithUsers(blackDuckUserRepositoryAccessor, blackDuckGroupRepositoryAccessor, userGroupRelationRepositoryAccessor, group1, email1, email2, email3);
        addGroupWithUsers(blackDuckUserRepositoryAccessor, blackDuckGroupRepositoryAccessor, userGroupRelationRepositoryAccessor, group2, email2, email4);

        //////////////////////////////////////////////////////////////////////////

        final HubService hubService = createMockHubService(blackDuckProperties);

        final ProjectView projectView = createProjectView("project", "description1", "projectUrl1");
        final ProjectView projectView2 = createProjectView("project2", "description2", "projectUrl2");
        final ProjectView projectView3 = createProjectView("project3", "description3", "projectUrl3");

        Mockito.when(hubService.getAllResponses(Mockito.any(HubPathMultipleResponses.class))).thenReturn(Arrays.asList(projectView, projectView2, projectView3));

        final AssignedUserGroupView userGroupView1 = createAssignedUserGroupView(group1, true);
        final AssignedUserGroupView userGroupView2 = createAssignedUserGroupView(group2, true);

        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView), ArgumentMatchers.same(ProjectView.USERGROUPS_LINK_RESPONSE))).thenReturn(Arrays.asList(userGroupView1, userGroupView2));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView2), ArgumentMatchers.same(ProjectView.USERGROUPS_LINK_RESPONSE))).thenReturn(Collections.emptyList());
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView3), ArgumentMatchers.same(ProjectView.USERGROUPS_LINK_RESPONSE))).thenReturn(Arrays.asList(userGroupView2));

        final AssignedUserView user1 = createAssignedUserView(email1);
        final AssignedUserView user2 = createAssignedUserView(email2);
        final AssignedUserView user3 = createAssignedUserView(email3);
        final AssignedUserView user4 = createAssignedUserView(email4);

        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView), ArgumentMatchers.same(ProjectView.USERS_LINK_RESPONSE))).thenReturn(Arrays.asList(user2, user4));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView2), ArgumentMatchers.same(ProjectView.USERS_LINK_RESPONSE))).thenReturn(Arrays.asList(user3));
        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView3), ArgumentMatchers.same(ProjectView.USERS_LINK_RESPONSE))).thenReturn(Arrays.asList(user1, user2, user3));

        final ProjectSyncTask projectSyncTask = new ProjectSyncTask(null, blackDuckProperties, blackDuckUserRepositoryAccessor, blackDuckGroupRepositoryAccessor, userGroupRelationRepositoryAccessor, blackDuckProjectRepositoryAccessor,
            userProjectRelationRepositoryAccessor);
        projectSyncTask.run();

        assertEquals(3, blackDuckProjectRepositoryAccessor.readEntities().size());
        assertEquals(9, userProjectRelationRepositoryAccessor.getUserProjectRelations().size());

        Mockito.when(hubService.getAllResponses(Mockito.any(HubPathMultipleResponses.class))).thenReturn(Arrays.asList(projectView, projectView2));
        projectSyncTask.run();

        assertEquals(2, blackDuckProjectRepositoryAccessor.readEntities().size());
        assertEquals(5, userProjectRelationRepositoryAccessor.getUserProjectRelations().size());
    }

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

    public void addGroupWithUsers(final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor, final MockBlackDuckGroupRepositoryAccessor blackDuckGroupRepositoryAccessor,
        final MockUserGroupRelationRepositoryAccessor userGroupRelationRepositoryAccessor, final String group, final String... users) {
        final List<BlackDuckUserEntity> userEntities = new ArrayList<>();
        for (final String user : users) {
            final Optional<? extends DatabaseEntity> entity = blackDuckUserRepositoryAccessor.readEntities().stream().filter(blackDuckUserEntity -> ((BlackDuckUserEntity) blackDuckUserEntity).getEmailAddress().equals(user)).findFirst();
            BlackDuckUserEntity blackDuckUserEntity;
            if (entity.isPresent()) {
                blackDuckUserEntity = (BlackDuckUserEntity) entity.get();
            } else {
                blackDuckUserEntity = new BlackDuckUserEntity(user, false);
                blackDuckUserEntity = (BlackDuckUserEntity) blackDuckUserRepositoryAccessor.saveEntity(blackDuckUserEntity);
            }
            userEntities.add(blackDuckUserEntity);
        }
        BlackDuckGroupEntity blackDuckGroupEntity = new BlackDuckGroupEntity(group, true, group + "Url");
        blackDuckGroupEntity = (BlackDuckGroupEntity) blackDuckGroupRepositoryAccessor.saveEntity(blackDuckGroupEntity);

        for (final BlackDuckUserEntity userEntity : userEntities) {
            userGroupRelationRepositoryAccessor.addUserGroupRelation(userEntity.getId(), blackDuckGroupEntity.getId());
        }
    }

}
