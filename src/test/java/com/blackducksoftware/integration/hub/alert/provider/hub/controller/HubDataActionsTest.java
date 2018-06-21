package com.blackducksoftware.integration.hub.alert.provider.hub.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.provider.hub.model.HubGroup;
import com.blackducksoftware.integration.hub.alert.provider.hub.model.HubProject;
import com.blackducksoftware.integration.hub.api.core.ResourceMetadata;
import com.blackducksoftware.integration.hub.api.generated.discovery.ApiDiscovery;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.api.generated.view.UserGroupView;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.ProjectService;
import com.blackducksoftware.integration.rest.connection.RestConnection;

public class HubDataActionsTest {

    @Test
    public void testGetHubGroupsNullHubServicesFactory() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        Mockito.when(globalProperties.createHubServicesFactory(Mockito.any())).thenReturn(null);
        final HubDataActions hubDataActions = new HubDataActions(globalProperties);
        try {
            hubDataActions.getHubGroups();
            fail();
        } catch (final AlertException e) {
            assertEquals("Missing global configuration.", e.getMessage());
        }
    }

    @Test
    public void testGetHubGroupsNoGroups() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        Mockito.when(globalProperties.createRestConnectionAndLogErrors(Mockito.any())).thenReturn(restConnection);
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final HubService hubService = Mockito.mock(HubService.class);
        Mockito.when(hubService.getAllResponses(ApiDiscovery.USERGROUPS_LINK_RESPONSE)).thenReturn(Collections.emptyList());
        Mockito.when(hubServicesFactory.createHubService()).thenReturn(hubService);
        Mockito.when(globalProperties.createHubServicesFactory(Mockito.any())).thenReturn(hubServicesFactory);
        final HubDataActions hubDataActions = new HubDataActions(globalProperties);
        final List<HubGroup> hubGroups = hubDataActions.getHubGroups();
        assertEquals(0, hubGroups.size());
    }

    @Test
    public void testGetHubGroups() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        Mockito.when(globalProperties.createRestConnectionAndLogErrors(Mockito.any())).thenReturn(restConnection);
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
        Mockito.when(globalProperties.createHubServicesFactory(Mockito.any())).thenReturn(hubServicesFactory);
        final HubDataActions hubDataActions = new HubDataActions(globalProperties);
        final List<HubGroup> hubGroups = hubDataActions.getHubGroups();
        assertEquals(1, hubGroups.size());
        final HubGroup hubGroup = hubGroups.get(0);
        assertEquals(active, hubGroup.getActive());
        assertEquals(username, hubGroup.getName());
        assertEquals(href, hubGroup.getUrl());
    }

    @Test
    public void testGetHubProjectsNullHubServicesFactory() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        Mockito.when(globalProperties.createHubServicesFactory(Mockito.any())).thenReturn(null);
        final HubDataActions hubDataActions = new HubDataActions(globalProperties);
        try {
            hubDataActions.getHubProjects();
            fail();
        } catch (final AlertException e) {
            assertEquals("Missing global configuration.", e.getMessage());
        }
    }

    @Test
    public void testGetHubProjectsNoProjects() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        Mockito.when(globalProperties.createRestConnectionAndLogErrors(Mockito.any())).thenReturn(restConnection);
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final ProjectService projectRequestService = Mockito.mock(ProjectService.class);
        final HubService hubService = Mockito.mock(HubService.class);
        Mockito.when(hubService.getAllResponses(ApiDiscovery.PROJECTS_LINK_RESPONSE)).thenReturn(Collections.emptyList());
        Mockito.when(hubServicesFactory.createProjectService()).thenReturn(projectRequestService);
        Mockito.when(hubServicesFactory.createHubService()).thenReturn(hubService);
        Mockito.when(globalProperties.createHubServicesFactory(Mockito.any())).thenReturn(hubServicesFactory);
        final HubDataActions hubDataActions = new HubDataActions(globalProperties);
        final List<HubProject> hubProjects = hubDataActions.getHubProjects();
        assertEquals(0, hubProjects.size());
    }

    @Test
    public void testGetHubProjects() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        Mockito.when(globalProperties.createRestConnectionAndLogErrors(Mockito.any())).thenReturn(restConnection);
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
        Mockito.when(globalProperties.createHubServicesFactory(Mockito.any())).thenReturn(hubServicesFactory);
        final HubDataActions hubDataActions = new HubDataActions(globalProperties);
        final List<HubProject> hubProjects = hubDataActions.getHubProjects();
        assertEquals(1, hubProjects.size());
        final HubProject hubProject = hubProjects.get(0);
        assertEquals(projectName, hubProject.getName());
    }
}
