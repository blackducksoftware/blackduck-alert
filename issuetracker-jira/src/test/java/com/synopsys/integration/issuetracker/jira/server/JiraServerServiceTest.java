package com.synopsys.integration.issuetracker.jira.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.issuetracker.message.IssueCommentRequest;
import com.synopsys.integration.alert.issuetracker.message.IssueContentModel;
import com.synopsys.integration.alert.issuetracker.message.IssueCreationRequest;
import com.synopsys.integration.alert.issuetracker.message.IssueResolutionRequest;
import com.synopsys.integration.alert.issuetracker.message.IssueSearchProperties;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.issuetracker.jira.cloud.model.TestIssueCreator;
import com.synopsys.integration.issuetracker.jira.common.JiraConstants;
import com.synopsys.integration.issuetracker.jira.common.JiraIssueSearchProperties;
import com.synopsys.integration.issuetracker.jira.server.model.TestIssueResponse;
import com.synopsys.integration.issuetracker.jira.server.model.TestIssueSearchIssueComponent;
import com.synopsys.integration.issuetracker.jira.server.model.TestIssueTypeResponseModel;
import com.synopsys.integration.issuetracker.jira.server.model.TestNewStatusDetailsComponent;
import com.synopsys.integration.issuetracker.jira.server.model.TestTransitionResponsesModel;
import com.synopsys.integration.jira.common.model.components.IdComponent;
import com.synopsys.integration.jira.common.model.components.ProjectComponent;
import com.synopsys.integration.jira.common.model.components.StatusDetailsComponent;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import com.synopsys.integration.jira.common.model.response.IssueTypeResponseModel;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;
import com.synopsys.integration.jira.common.model.response.UserDetailsResponseModel;
import com.synopsys.integration.jira.common.rest.service.IssueMetaDataService;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.rest.service.IssueTypeService;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;
import com.synopsys.integration.jira.common.server.model.IssueCreationRequestModel;
import com.synopsys.integration.jira.common.server.model.IssueSearchIssueComponent;
import com.synopsys.integration.jira.common.server.model.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.server.service.IssueSearchService;
import com.synopsys.integration.jira.common.server.service.IssueService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.jira.common.server.service.ProjectService;
import com.synopsys.integration.jira.common.server.service.UserSearchService;

public class JiraServerServiceTest {
    private Gson gson = new Gson();
    // mock services
    private PluginManagerService pluginManagerService;
    private ProjectService projectService;
    private UserSearchService userSearchService;
    private IssueTypeService issueTypeService;
    private IssueMetaDataService issueMetaDataService;
    private IssueService issueService;
    private IssuePropertyService issuePropertyService;
    private IssueSearchService issueSearchService;

    @BeforeEach
    public void init() {
        pluginManagerService = Mockito.mock(PluginManagerService.class);
        projectService = Mockito.mock(ProjectService.class);
        userSearchService = Mockito.mock(UserSearchService.class);
        issueTypeService = Mockito.mock(IssueTypeService.class);
        issueMetaDataService = Mockito.mock(IssueMetaDataService.class);
        issueService = Mockito.mock(IssueService.class);
        issuePropertyService = Mockito.mock(IssuePropertyService.class);
        issueSearchService = Mockito.mock(IssueSearchService.class);
    }

    @Test
    public void testContextNull() throws Exception {
        JiraServerService service = new JiraServerService(gson);
        List<IssueTrackerRequest> requests = new ArrayList<>();
        try {
            service.sendRequests(null, requests);
            fail();
        } catch (IssueTrackerException ex) {
            assertTrue(ex.getMessage().contains("Context missing."));
        }
    }

    @Test
    public void testRequestsNull() throws Exception {
        JiraServerService service = new JiraServerService(gson);
        List<IssueTrackerRequest> requests = null;
        try {
            service.sendRequests(createContext(), requests);
            fail();
        } catch (IssueTrackerException ex) {
            assertTrue(ex.getMessage().contains("Requests missing."));
        }
    }

    @Test
    public void testRequestsEmpty() throws Exception {
        JiraServerService service = new JiraServerService(gson);
        List<IssueTrackerRequest> requests = new ArrayList<>();
        try {
            service.sendRequests(createContext(), requests);
            fail();
        } catch (IssueTrackerException ex) {
            assertTrue(ex.getMessage().contains("Requests missing."));
        }
    }

    @Test
    public void testAppMissing() throws Exception {
        JiraServerService service = new JiraServerService(gson);
        List<IssueTrackerRequest> requests = new ArrayList<>();
        IssueContentModel content = createContentModel();
        IssueSearchProperties searchProperties = createSearchProperties();
        requests.add(IssueCreationRequest.of(searchProperties, content));
        requests.add(IssueCommentRequest.of(searchProperties, content));
        requests.add(IssueResolutionRequest.of(searchProperties, content));
        Mockito.when(pluginManagerService.isAppInstalled(Mockito.anyString(), Mockito.anyString(), Mockito.eq(JiraConstants.JIRA_APP_KEY))).thenReturn(false);
        try {
            service.sendRequests(createContext(), requests);
            fail();
        } catch (IssueTrackerException ex) {
            assertTrue(ex.getMessage().contains("Please configure the Jira Server plugin"));
        }
    }

    @Test
    public void testCreateIssue() throws Exception {
        Mockito.when(pluginManagerService.isAppInstalled(Mockito.anyString(), Mockito.anyString(), Mockito.eq(JiraConstants.JIRA_APP_KEY))).thenReturn(true);
        List<ProjectComponent> projectComponents = new ArrayList<>();
        projectComponents.add(new ProjectComponent(null, "1", "project", "project", null, null, null, null));
        Mockito.when(projectService.getProjectsByName(Mockito.anyString())).thenReturn(projectComponents);
        List<IssueTypeResponseModel> issueTypes = new ArrayList<>();
        issueTypes.add(new TestIssueTypeResponseModel());
        Mockito.when(issueTypeService.getAllIssueTypes()).thenReturn(issueTypes);
        Optional<UserDetailsResponseModel> userDetails = Optional.of(new TestIssueCreator());
        Mockito.when(userSearchService.findUserByUsername(Mockito.anyString())).thenReturn(userDetails);
        Mockito.when(issueMetaDataService.doesProjectContainIssueType(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        List<IssueSearchIssueComponent> issues = new ArrayList<>();
        IssueSearchResponseModel searchResponseModel = new IssueSearchResponseModel("", issues);
        Mockito.when(issueSearchService.queryForIssues(Mockito.anyString())).thenReturn(searchResponseModel);
        IssueResponseModel issue = createIssueResponse();
        Mockito.when(issueService.createIssue(Mockito.any(IssueCreationRequestModel.class))).thenReturn(issue);

        JiraServerService service = new JiraServerService(gson);
        List<IssueTrackerRequest> requests = new ArrayList<>();
        IssueContentModel content = createContentModel();
        IssueSearchProperties searchProperties = Mockito.mock(JiraIssueSearchProperties.class);
        requests.add(IssueCreationRequest.of(searchProperties, content));
        IssueTrackerResponse response = service.sendRequests(createContext(), requests);
        assertNotNull(response);
        assertNotNull(response.getStatusMessage());
        assertTrue(response.getUpdatedIssueKeys().contains("project-1"));
    }

    @Test
    public void testResolveIssue() throws Exception {
        Mockito.when(pluginManagerService.isAppInstalled(Mockito.anyString(), Mockito.anyString(), Mockito.eq(JiraConstants.JIRA_APP_KEY))).thenReturn(true);
        List<ProjectComponent> projectComponents = new ArrayList<>();
        projectComponents.add(new ProjectComponent(null, "1", "project", "project", null, null, null, null));
        Mockito.when(projectService.getProjectsByName(Mockito.anyString())).thenReturn(projectComponents);
        List<IssueTypeResponseModel> issueTypes = new ArrayList<>();
        issueTypes.add(new TestIssueTypeResponseModel());
        Mockito.when(issueTypeService.getAllIssueTypes()).thenReturn(issueTypes);
        Optional<UserDetailsResponseModel> userDetails = Optional.of(new TestIssueCreator());
        Mockito.when(userSearchService.findUserByUsername(Mockito.anyString())).thenReturn(userDetails);
        Mockito.when(issueMetaDataService.doesProjectContainIssueType(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        List<IssueSearchIssueComponent> issues = new ArrayList<>();
        issues.add(new TestIssueSearchIssueComponent());
        IssueSearchResponseModel searchResponseModel = new IssueSearchResponseModel("", issues);
        Mockito.when(issueSearchService.queryForIssues(Mockito.anyString())).thenReturn(searchResponseModel);
        Mockito.when(issueService.getIssue(Mockito.anyString())).thenReturn(createIssueResponse());
        StatusDetailsComponent statusDetailsComponent = new TestNewStatusDetailsComponent();
        Mockito.when(issueService.getStatus(Mockito.anyString())).thenReturn(statusDetailsComponent);
        TransitionsResponseModel transitionsResponseModel = new TestTransitionResponsesModel();
        Mockito.when(issueService.getTransitions(Mockito.anyString())).thenReturn(transitionsResponseModel);

        JiraServerService service = new JiraServerService(gson);
        List<IssueTrackerRequest> requests = new ArrayList<>();
        IssueContentModel content = createContentModel();
        IssueSearchProperties searchProperties = Mockito.mock(JiraIssueSearchProperties.class);
        requests.add(IssueCommentRequest.of(searchProperties, content));
        requests.add(IssueResolutionRequest.of(searchProperties, content));
        IssueTrackerResponse response = service.sendRequests(createContext(), requests);
        assertNotNull(response);
        assertNotNull(response.getStatusMessage());
        assertTrue(response.getUpdatedIssueKeys().contains("project-1"));
    }

    private JiraServerServiceFactory createMockServiceFactory() {
        JiraServerServiceFactory serviceFactory = Mockito.mock(JiraServerServiceFactory.class);
        Mockito.when(serviceFactory.createPluginManagerService()).thenReturn(pluginManagerService);
        Mockito.when(serviceFactory.createProjectService()).thenReturn(projectService);
        Mockito.when(serviceFactory.createUserSearchService()).thenReturn(userSearchService);
        Mockito.when(serviceFactory.createIssueTypeService()).thenReturn(issueTypeService);
        Mockito.when(serviceFactory.createIssueMetadataService()).thenReturn(issueMetaDataService);
        Mockito.when(serviceFactory.createIssueService()).thenReturn(issueService);
        Mockito.when(serviceFactory.createIssuePropertyService()).thenReturn(issuePropertyService);
        Mockito.when(serviceFactory.createIssueSearchService()).thenReturn(issueSearchService);

        return serviceFactory;
    }

    private JiraServerContext createContext() throws IssueTrackerException {
        return new JiraServerContext(createMockServerConfig(), createIssueConfig());
    }

    private JiraServerProperties createMockServerConfig() throws IssueTrackerException {
        JiraServerProperties config = Mockito.mock(JiraServerProperties.class);
        JiraServerServiceFactory serviceFactory = createMockServiceFactory();
        Mockito.when(config.createJiraServicesServerFactory(Mockito.any(), Mockito.eq(gson))).thenReturn(serviceFactory);
        Mockito.when(config.getUrl()).thenReturn("");
        Mockito.when(config.getUsername()).thenReturn("");
        Mockito.when(config.getPassword()).thenReturn("");

        return config;
    }

    private JiraIssueSearchProperties createSearchProperties() {
        return new JiraIssueSearchProperties("provider",
            "providerUrl",
            "topicName",
            "topicValue",
            "subTopicName",
            "subTopicValue",
            "category",
            "componentName",
            "componentValue",
            "subComponentName",
            "subComponentValue",
            "additionalKey");
    }

    private IssueConfig createIssueConfig() {
        IssueConfig issueConfig = new IssueConfig();
        issueConfig.setCommentOnIssues(true);
        issueConfig.setIssueType("task");
        issueConfig.setProjectName("project");
        issueConfig.setResolveTransition("done");
        issueConfig.setOpenTransition("new");
        issueConfig.setIssueCreator("creator");

        return issueConfig;
    }

    private IssueContentModel createContentModel() {
        String title = "Valid Title Length";
        String description = "Valid Description Length";
        List<String> descriptionComments = new ArrayList<>();
        descriptionComments.add("Description Comment");
        List<String> additionalComments = new ArrayList<>();
        additionalComments.add("Additional Comment");
        return IssueContentModel.of(title, description, descriptionComments, additionalComments);
    }

    private IssueResponseModel createIssueResponse() {
        TestIssueResponse issueResponse = new TestIssueResponse();
        List<IdComponent> ids = new ArrayList<>();
        ids.add(new IdComponent("1"));
        ids.add(new IdComponent("2"));
        issueResponse.getTransitions().addAll(ids);
        return issueResponse;
    }
}
