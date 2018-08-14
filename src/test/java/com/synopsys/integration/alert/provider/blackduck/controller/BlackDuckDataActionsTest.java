package com.synopsys.integration.alert.provider.blackduck.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckDataActions;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckGroup;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckProject;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserGroupView;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;

public class BlackDuckDataActionsTest {

    @Test
    public void testGetHubGroupsNullHubServicesFactory() throws Exception {
        final BlackDuckProperties hubProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(hubProperties.createBlackDuckServicesFactory(Mockito.any(), Mockito.any())).thenReturn(null);
        final BlackDuckDataActions blackDuckDataActions = new BlackDuckDataActions(hubProperties);
        try {
            blackDuckDataActions.getBlackDuckGroups();
            fail();
        } catch (final AlertException e) {
            assertEquals("Missing global configuration.", e.getMessage());
        }
    }

    @Test
    public void testGetHubGroupsNoGroups() throws Exception {
        final BlackDuckProperties hubProperties = Mockito.mock(BlackDuckProperties.class);
        final BlackduckRestConnection restConnection = Mockito.mock(BlackduckRestConnection.class);
        Mockito.when(hubProperties.createRestConnectionAndLogErrors(Mockito.any())).thenReturn(Optional.of(restConnection));
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final HubService hubService = Mockito.mock(HubService.class);
        Mockito.when(hubService.getAllResponses(ApiDiscovery.USERGROUPS_LINK_RESPONSE)).thenReturn(Collections.emptyList());
        Mockito.when(hubServicesFactory.createHubService()).thenReturn(hubService);
        Mockito.when(hubProperties.createBlackDuckServicesFactory(Mockito.any(), Mockito.any())).thenReturn(hubServicesFactory);
        final BlackDuckDataActions blackDuckDataActions = new BlackDuckDataActions(hubProperties);
        final List<BlackDuckGroup> blackDuckGroups = blackDuckDataActions.getBlackDuckGroups();
        assertEquals(0, blackDuckGroups.size());
    }

    @Test
    public void testGetHubGroups() throws Exception {
        final BlackDuckProperties hubProperties = Mockito.mock(BlackDuckProperties.class);
        final BlackduckRestConnection restConnection = Mockito.mock(BlackduckRestConnection.class);
        Mockito.when(hubProperties.createRestConnectionAndLogErrors(Mockito.any())).thenReturn(Optional.of(restConnection));
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final HubService hubService = Mockito.mock(HubService.class);

        final List<UserGroupView> userGroups = new ArrayList<>();
        final Boolean active = true;
        final String username = "User";
        final String href = "href";
        final UserGroupView userGroup = new UserGroupView();
        final ResourceMetadata metaView = new ResourceMetadata();
        metaView.href = href;
        userGroup._meta = metaView;
        userGroup.active = active;
        userGroup.name = username;
        userGroups.add(userGroup);

        Mockito.when(hubService.getAllResponses(ApiDiscovery.USERGROUPS_LINK_RESPONSE)).thenReturn(userGroups);
        Mockito.when(hubServicesFactory.createHubService()).thenReturn(hubService);
        Mockito.when(hubServicesFactory.createHubService()).thenReturn(hubService);
        Mockito.when(hubProperties.createBlackDuckServicesFactory(Mockito.any(), Mockito.any())).thenReturn(hubServicesFactory);
        final BlackDuckDataActions blackDuckDataActions = new BlackDuckDataActions(hubProperties);
        final List<BlackDuckGroup> blackDuckGroups = blackDuckDataActions.getBlackDuckGroups();
        assertEquals(1, blackDuckGroups.size());
        final BlackDuckGroup blackDuckGroup = blackDuckGroups.get(0);
        assertEquals(active, blackDuckGroup.getActive());
        assertEquals(username, blackDuckGroup.getName());
        assertEquals(href, blackDuckGroup.getUrl());
    }

    @Test
    public void testGetHubProjectsNullHubServicesFactory() throws Exception {
        final BlackDuckProperties hubProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(hubProperties.createBlackDuckServicesFactory(Mockito.any(), Mockito.any())).thenReturn(null);
        final BlackDuckDataActions blackDuckDataActions = new BlackDuckDataActions(hubProperties);
        try {
            blackDuckDataActions.getBlackDuckProjects();
            fail();
        } catch (final AlertException e) {
            assertEquals("Missing global configuration.", e.getMessage());
        }
    }

    @Test
    public void testGetHubProjectsNoProjects() throws Exception {
        final BlackDuckProperties hubProperties = Mockito.mock(BlackDuckProperties.class);
        final BlackduckRestConnection restConnection = Mockito.mock(BlackduckRestConnection.class);
        Mockito.when(hubProperties.createRestConnectionAndLogErrors(Mockito.any())).thenReturn(Optional.of(restConnection));
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final ProjectService projectRequestService = Mockito.mock(ProjectService.class);
        final HubService hubService = Mockito.mock(HubService.class);
        Mockito.when(hubService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE)).thenReturn(Collections.emptyList());
        Mockito.when(hubServicesFactory.createProjectService()).thenReturn(projectRequestService);
        Mockito.when(hubServicesFactory.createHubService()).thenReturn(hubService);
        Mockito.when(hubProperties.createBlackDuckServicesFactory(Mockito.any(), Mockito.any())).thenReturn(hubServicesFactory);
        final BlackDuckDataActions blackDuckDataActions = new BlackDuckDataActions(hubProperties);
        final List<BlackDuckProject> blackDuckProjects = blackDuckDataActions.getBlackDuckProjects();
        assertEquals(0, blackDuckProjects.size());
    }

    @Test
    public void testGetHubProjects() throws Exception {
        final BlackDuckProperties hubProperties = Mockito.mock(BlackDuckProperties.class);
        final BlackduckRestConnection restConnection = Mockito.mock(BlackduckRestConnection.class);
        Mockito.when(hubProperties.createRestConnectionAndLogErrors(Mockito.any())).thenReturn(Optional.of(restConnection));
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final ProjectService projectRequestService = Mockito.mock(ProjectService.class);
        final HubService hubService = Mockito.mock(HubService.class);

        final List<ProjectView> projectViews = new ArrayList<>();
        final String projectName = "projectName";

        final ProjectView projectView = new ProjectView();
        projectView.name = projectName;
        projectViews.add(projectView);

        Mockito.when(hubService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE)).thenReturn(projectViews);
        Mockito.when(hubServicesFactory.createProjectService()).thenReturn(projectRequestService);
        Mockito.when(hubServicesFactory.createHubService()).thenReturn(hubService);
        Mockito.when(hubProperties.createBlackDuckServicesFactory(Mockito.any(), Mockito.any())).thenReturn(hubServicesFactory);
        final BlackDuckDataActions blackDuckDataActions = new BlackDuckDataActions(hubProperties);
        final List<BlackDuckProject> blackDuckProjects = blackDuckDataActions.getBlackDuckProjects();
        assertEquals(1, blackDuckProjects.size());
        final BlackDuckProject blackDuckProject = blackDuckProjects.get(0);
        assertEquals(projectName, blackDuckProject.getName());
    }
}
