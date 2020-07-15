package com.synopsys.integration.alert.jira.cloud;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueContentModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueCreationRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueResolutionRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueSearchProperties;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.alert.common.channel.issuetracker.service.TestIssueRequestCreator;
import com.synopsys.integration.alert.jira.cloud.model.TestIssueCreator;
import com.synopsys.integration.alert.jira.cloud.model.TestIssueResponse;
import com.synopsys.integration.alert.jira.cloud.model.TestIssueTypeResponseModel;
import com.synopsys.integration.alert.jira.cloud.model.TestNewStatusDetailsComponent;
import com.synopsys.integration.alert.jira.cloud.model.TestTransitionResponsesModel;
import com.synopsys.integration.alert.jira.common.JiraConstants;
import com.synopsys.integration.alert.jira.common.JiraIssueSearchProperties;
import com.synopsys.integration.jira.common.cloud.model.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.cloud.service.IssueSearchService;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.cloud.service.ProjectService;
import com.synopsys.integration.jira.common.cloud.service.UserSearchService;
import com.synopsys.integration.jira.common.model.components.IdComponent;
import com.synopsys.integration.jira.common.model.components.ProjectComponent;
import com.synopsys.integration.jira.common.model.components.StatusDetailsComponent;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import com.synopsys.integration.jira.common.model.response.IssueTypeResponseModel;
import com.synopsys.integration.jira.common.model.response.PageOfProjectsResponseModel;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;
import com.synopsys.integration.jira.common.model.response.UserDetailsResponseModel;
import com.synopsys.integration.jira.common.rest.service.IssueMetaDataService;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.rest.service.IssueTypeService;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;

public class JiraCloudTestActionTest {
    private final Gson gson = new Gson();
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
    public void testCreateTestAction() throws Exception {
        Mockito.when(pluginManagerService.isAppInstalled(Mockito.anyString(), Mockito.anyString(), Mockito.eq(JiraConstants.JIRA_APP_KEY))).thenReturn(true);
        List<ProjectComponent> pageComponents = new ArrayList<>();
        pageComponents.add(new ProjectComponent(null, "1", "project", "project", null, null, null, null));
        PageOfProjectsResponseModel pageOfProjects = new PageOfProjectsResponseModel(pageComponents);
        Mockito.when(projectService.getProjectsByName(Mockito.anyString())).thenReturn(pageOfProjects);
        List<IssueTypeResponseModel> issueTypes = new ArrayList<>();
        issueTypes.add(new TestIssueTypeResponseModel());
        Mockito.when(issueTypeService.getAllIssueTypes()).thenReturn(issueTypes);
        List<UserDetailsResponseModel> userDetails = new ArrayList<>();
        userDetails.add(new TestIssueCreator());
        Mockito.when(userSearchService.findUser(Mockito.anyString())).thenReturn(userDetails);
        Mockito.when(issueMetaDataService.doesProjectContainIssueType(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        IssueSearchResponseModel searchResponseModel = new IssueSearchResponseModel();
        List<IssueResponseModel> issues = new ArrayList<>();
        issues.add(createIssueResponse());
        searchResponseModel.setIssues(issues);
        Mockito.when(issueSearchService.queryForIssues(Mockito.anyString())).thenReturn(searchResponseModel);
        StatusDetailsComponent statusDetailsComponent = new TestNewStatusDetailsComponent();
        Mockito.when(issueService.getStatus(Mockito.anyString())).thenReturn(statusDetailsComponent);
        TransitionsResponseModel transitionsResponseModel = new TestTransitionResponsesModel();
        Mockito.when(issueService.getTransitions(Mockito.anyString())).thenReturn(transitionsResponseModel);

        JiraCloudService service = new JiraCloudService(gson);
        IssueContentModel content = createContentModel();
        IssueSearchProperties searchProperties = Mockito.mock(JiraIssueSearchProperties.class);
        JiraCloudCreateIssueTestAction testAction = new JiraCloudCreateIssueTestAction(service, gson, new TestIssueRequestCreator() {
            @Override
            public IssueTrackerRequest createRequest(IssueOperation operation, String messageId) {
                AlertIssueOrigin alertIssueOrigin = new AlertIssueOrigin(null, null);
                if (operation == IssueOperation.RESOLVE) {
                    return IssueResolutionRequest.of(searchProperties, content, alertIssueOrigin);
                }
                return IssueCreationRequest.of(searchProperties, content, alertIssueOrigin);
            }
        });

        IssueTrackerResponse response = testAction.testConfig(createContext());
        assertNotNull(response);
        assertNotNull(response.getStatusMessage());
        assertTrue(response.getUpdatedIssues().contains("project-1"));
    }

    private JiraCloudServiceFactory createMockServiceFactory() {
        JiraCloudServiceFactory serviceFactory = Mockito.mock(JiraCloudServiceFactory.class);
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

    private JiraCloudContext createContext() throws IssueTrackerException {
        return new JiraCloudContext(createMockServerConfig(), createIssueConfig());
    }

    private JiraCloudProperties createMockServerConfig() throws IssueTrackerException {
        JiraCloudProperties config = Mockito.mock(JiraCloudProperties.class);
        JiraCloudServiceFactory serviceFactory = createMockServiceFactory();
        Mockito.when(config.createJiraServicesCloudFactory(Mockito.any(), Mockito.eq(gson))).thenReturn(serviceFactory);
        Mockito.when(config.getUrl()).thenReturn("");
        Mockito.when(config.getUsername()).thenReturn("");
        Mockito.when(config.getAccessToken()).thenReturn("");

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
