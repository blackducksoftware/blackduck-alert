package com.synopsys.integration.alert.provider.blackduck.tasks;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockUserProjectRelationRepositoryAccessor;
import com.synopsys.integration.blackduck.api.core.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.component.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.rest.BlackDuckRestConnection;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;

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

        Mockito.when(blackDuckProperties.createRestConnectionAndLogErrors(Mockito.any())).thenReturn(Optional.of(Mockito.mock(BlackDuckRestConnection.class)));
        final BlackDuckServicesFactory BlackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        Mockito.when(blackDuckProperties.createBlackDuckServicesFactory(Mockito.any(), Mockito.any())).thenReturn(BlackDuckServicesFactory);

        final BlackDuckService hubService = Mockito.mock(BlackDuckService.class);
        Mockito.when(BlackDuckServicesFactory.createBlackDuckService()).thenReturn(hubService);

        final ProjectService projectService = Mockito.mock(ProjectService.class);
        Mockito.when(BlackDuckServicesFactory.createProjectService()).thenReturn(projectService);

        final ProjectView projectView = createProjectView("project", "description1", "projectUrl1");
        final ProjectView projectView2 = createProjectView("project2", "description2", "projectUrl2");
        final ProjectView projectView3 = createProjectView("project3", "description3", "projectUrl3");

        Mockito.when(hubService.getAllResponses(Mockito.any(BlackDuckPathMultipleResponses.class))).thenReturn(Arrays.asList(projectView, projectView2, projectView3));

        final UserView user1 = createUserView(email1, true);
        final UserView user2 = createUserView(email2, true);
        final UserView user3 = createUserView(email3, true);
        final UserView user4 = createUserView(email4, true);

        Mockito.when(projectService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView))).thenReturn(new HashSet<>(Arrays.asList(user2, user4)));
        Mockito.when(projectService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView2))).thenReturn(new HashSet<>(Arrays.asList(user3)));
        Mockito.when(projectService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView3))).thenReturn(new HashSet<>(Arrays.asList(user1, user2, user3)));

        final ProjectSyncTask projectSyncTask = new ProjectSyncTask(null, blackDuckProperties, blackDuckUserRepositoryAccessor, blackDuckProjectRepositoryAccessor,
                userProjectRelationRepositoryAccessor);
        projectSyncTask.run();

        assertEquals(4, blackDuckUserRepositoryAccessor.readEntities().size());
        assertEquals(3, blackDuckProjectRepositoryAccessor.readEntities().size());
        assertEquals(6, userProjectRelationRepositoryAccessor.readEntities().size());

        Mockito.when(hubService.getAllResponses(Mockito.any(BlackDuckPathMultipleResponses.class))).thenReturn(Arrays.asList(projectView, projectView2));

        Mockito.when(projectService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView))).thenReturn(new HashSet<>(Arrays.asList(user2, user4)));
        Mockito.when(projectService.getAllActiveUsersForProject(ArgumentMatchers.same(projectView2))).thenReturn(new HashSet<>(Arrays.asList(user3)));

        Mockito.when(hubService.getAllResponses(ArgumentMatchers.same(projectView2), ArgumentMatchers.same(ProjectView.USERS_LINK_RESPONSE))).thenReturn(Collections.emptyList());
        projectSyncTask.run();

        assertEquals(3, blackDuckUserRepositoryAccessor.readEntities().size());
        assertEquals(2, blackDuckProjectRepositoryAccessor.readEntities().size());
        assertEquals(3, userProjectRelationRepositoryAccessor.readEntities().size());
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
