package com.synopsys.integration.alert.issuetracker.jira.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.issuetracker.IssueConfig;
import com.synopsys.integration.alert.issuetracker.IssueTrackerContext;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.ProjectComponent;
import com.synopsys.integration.jira.common.model.response.IssueTypeResponseModel;
import com.synopsys.integration.jira.common.model.response.UserDetailsResponseModel;
import com.synopsys.integration.jira.common.rest.service.IssueMetaDataService;
import com.synopsys.integration.jira.common.rest.service.IssueTypeService;
import com.synopsys.integration.jira.common.server.service.ProjectService;
import com.synopsys.integration.jira.common.server.service.UserSearchService;

public class JiraServerIssueConfigValidatorTest {
    @Test
    public void validateSuccessTest() throws IntegrationException {
        ProjectService projectService = Mockito.mock(ProjectService.class);
        UserSearchService userSearchService = Mockito.mock(UserSearchService.class);
        IssueTypeService issueTypeService = Mockito.mock(IssueTypeService.class);
        IssueMetaDataService issueMetaDataService = Mockito.mock(IssueMetaDataService.class);
        Mockito.when(issueMetaDataService.doesProjectContainIssueType(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
        JiraServerIssueConfigValidator jiraIssueConfigValidator = new JiraServerIssueConfigValidator(projectService, userSearchService, issueTypeService, issueMetaDataService);

        ConfigurationFieldModel resolveTransition = ConfigurationFieldModel.create(JiraServerProperties.KEY_RESOLVE_WORKFLOW_TRANSITION);
        String resolveTransitionString = "Resolve";
        resolveTransition.setFieldValue(resolveTransitionString);

        ConfigurationFieldModel project = ConfigurationFieldModel.create(JiraServerProperties.KEY_JIRA_PROJECT_NAME);
        String projectName = "ProjectName";
        project.setFieldValue(projectName);

        ConfigurationFieldModel issueType = ConfigurationFieldModel.create(JiraServerProperties.KEY_ISSUE_TYPE);
        String issueTypeString = "IssueType";
        issueType.setFieldValue(issueTypeString);

        ConfigurationFieldModel issueCreator = ConfigurationFieldModel.create(JiraServerProperties.KEY_ISSUE_CREATOR);
        String issueCreatorString = "IssueCreator";
        issueCreator.setFieldValue(issueCreatorString);

        IssueConfig issueConfig = new IssueConfig();
        issueConfig.setResolveTransition(resolveTransitionString);
        issueConfig.setProjectName(projectName);
        issueConfig.setIssueType(issueTypeString);
        issueConfig.setIssueCreator(issueCreatorString);

        IssueTypeResponseModel issue = Mockito.mock(IssueTypeResponseModel.class);
        Mockito.when(issue.getName()).thenReturn(issueTypeString);
        Mockito.when(issueTypeService.getAllIssueTypes()).thenReturn(List.of(issue));

        UserDetailsResponseModel user = Mockito.mock(UserDetailsResponseModel.class);
        Mockito.when(user.getName()).thenReturn(issueCreatorString);
        Mockito.when(userSearchService.findUserByUsername(Mockito.anyString())).thenReturn(Optional.of(user));

        ProjectComponent projectComponent = Mockito.mock(ProjectComponent.class);
        Mockito.when(projectComponent.getName()).thenReturn(projectName);
        Mockito.when(projectService.getProjectsByName(Mockito.anyString())).thenReturn(List.of(projectComponent));

        try {
            IssueTrackerContext context = new IssueTrackerContext(null, issueConfig);
            jiraIssueConfigValidator.validate(context);
            assertEquals(resolveTransitionString, issueConfig.getResolveTransition().orElse(""));
            assertEquals(projectName, issueConfig.getProjectName());
            assertEquals(issueCreatorString, issueConfig.getIssueCreator());
            assertEquals(issueTypeString, issueConfig.getIssueType());
        } catch (AlertFieldException e) {
            fail();
        }
    }

    @Test
    public void validateMissingRequiredTest() throws IntegrationException {
        ProjectService projectService = Mockito.mock(ProjectService.class);
        UserSearchService userSearchService = Mockito.mock(UserSearchService.class);
        IssueTypeService issueTypeService = Mockito.mock(IssueTypeService.class);
        IssueMetaDataService issueMetaDataService = Mockito.mock(IssueMetaDataService.class);
        JiraServerIssueConfigValidator jiraIssueConfigValidator = new JiraServerIssueConfigValidator(projectService, userSearchService, issueTypeService, issueMetaDataService);

        ConfigurationFieldModel resolveTransition = ConfigurationFieldModel.create(JiraServerProperties.KEY_RESOLVE_WORKFLOW_TRANSITION);
        String resolveTransitionString = "Resolve";
        resolveTransition.setFieldValue(resolveTransitionString);

        ConfigurationFieldModel issueType = ConfigurationFieldModel.create(JiraServerProperties.KEY_ISSUE_TYPE);
        String issueTypeString = "IssueType";
        issueType.setFieldValue(issueTypeString);

        IssueConfig issueConfig = new IssueConfig();
        issueConfig.setResolveTransition(resolveTransitionString);
        issueConfig.setIssueType(issueTypeString);

        IssueTypeResponseModel issue = Mockito.mock(IssueTypeResponseModel.class);
        Mockito.when(issue.getName()).thenReturn(issueTypeString);
        Mockito.when(issueTypeService.getAllIssueTypes()).thenReturn(List.of(issue));

        try {
            IssueTrackerContext context = new IssueTrackerContext(null, issueConfig);
            jiraIssueConfigValidator.validate(context);
            fail();
        } catch (AlertFieldException e) {
            assertTrue(e.getFieldErrors().containsKey(JiraServerProperties.KEY_JIRA_PROJECT_NAME));
            assertTrue(e.getFieldErrors().containsKey(JiraServerProperties.KEY_ISSUE_CREATOR));
            assertFalse(e.getFieldErrors().containsKey(JiraServerProperties.KEY_ISSUE_TYPE));
        }
    }
}
