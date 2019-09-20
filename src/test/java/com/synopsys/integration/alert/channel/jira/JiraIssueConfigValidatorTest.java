package com.synopsys.integration.alert.channel.jira;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.channel.jira.JiraIssueConfigValidator.JiraIssueConfig;
import com.synopsys.integration.alert.channel.jira.descriptor.JiraDescriptor;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.model.components.ProjectComponent;
import com.synopsys.integration.jira.common.cloud.model.response.IssueTypeResponseModel;
import com.synopsys.integration.jira.common.cloud.model.response.PageOfProjectsResponseModel;
import com.synopsys.integration.jira.common.cloud.model.response.UserDetailsResponseModel;
import com.synopsys.integration.jira.common.cloud.rest.service.IssueTypeService;
import com.synopsys.integration.jira.common.cloud.rest.service.ProjectService;
import com.synopsys.integration.jira.common.cloud.rest.service.UserSearchService;

public class JiraIssueConfigValidatorTest {

    @Test
    public void validateSuccessTest() throws IntegrationException {
        ProjectService projectService = Mockito.mock(ProjectService.class);
        UserSearchService userSearchService = Mockito.mock(UserSearchService.class);
        IssueTypeService issueTypeService = Mockito.mock(IssueTypeService.class);
        JiraIssueConfigValidator jiraIssueConfigValidator = new JiraIssueConfigValidator(projectService, userSearchService, issueTypeService);

        final ConfigurationFieldModel resolveTransition = ConfigurationFieldModel.create(JiraDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION);
        String resolveTransitionString = "Resolve";
        resolveTransition.setFieldValue(resolveTransitionString);

        ConfigurationFieldModel project = ConfigurationFieldModel.create(JiraDescriptor.KEY_JIRA_PROJECT_NAME);
        String projectName = "ProjectName";
        project.setFieldValue(projectName);

        ConfigurationFieldModel issueType = ConfigurationFieldModel.create(JiraDescriptor.KEY_ISSUE_TYPE);
        String issueTypeString = "IssueType";
        issueType.setFieldValue(issueTypeString);

        ConfigurationFieldModel issueCreator = ConfigurationFieldModel.create(JiraDescriptor.KEY_ISSUE_CREATOR);
        String issueCreatorString = "IssueCreator";
        issueCreator.setFieldValue(issueCreatorString);

        Map<String, ConfigurationFieldModel> fields = new HashMap<>();
        fields.put(resolveTransition.getFieldKey(), resolveTransition);
        fields.put(project.getFieldKey(), project);
        fields.put(issueType.getFieldKey(), issueType);
        fields.put(issueCreator.getFieldKey(), issueCreator);

        FieldAccessor fieldAccessor = new FieldAccessor(fields);

        IssueTypeResponseModel issue = Mockito.mock(IssueTypeResponseModel.class);
        Mockito.when(issue.getName()).thenReturn(issueTypeString);
        Mockito.when(issueTypeService.getAllIssueTypes()).thenReturn(List.of(issue));

        UserDetailsResponseModel user = Mockito.mock(UserDetailsResponseModel.class);
        Mockito.when(user.getEmailAddress()).thenReturn(issueCreatorString);
        Mockito.when(userSearchService.findUser(Mockito.anyString())).thenReturn(List.of(user));

        ProjectComponent projectComponent = Mockito.mock(ProjectComponent.class);
        Mockito.when(projectComponent.getName()).thenReturn(projectName);
        PageOfProjectsResponseModel projectResponse = Mockito.mock(PageOfProjectsResponseModel.class);
        Mockito.when(projectResponse.getProjects()).thenReturn(List.of(projectComponent));
        Mockito.when(projectService.getProjectsByName(Mockito.anyString())).thenReturn(projectResponse);

        try {
            final JiraIssueConfig jiraIssueConfig = jiraIssueConfigValidator.validate(fieldAccessor);
            assertEquals(resolveTransitionString, jiraIssueConfig.getResolveTransition().orElse(""));
            assertEquals(projectName, jiraIssueConfig.getProjectComponent().getName());
            assertEquals(issueCreatorString, jiraIssueConfig.getIssueCreator());
            assertEquals(issueTypeString, jiraIssueConfig.getIssueType());
        } catch (AlertFieldException e) {
            fail();
        }
    }

    @Test
    public void validateMissingRequiredTest() throws IntegrationException {
        ProjectService projectService = Mockito.mock(ProjectService.class);
        UserSearchService userSearchService = Mockito.mock(UserSearchService.class);
        IssueTypeService issueTypeService = Mockito.mock(IssueTypeService.class);
        JiraIssueConfigValidator jiraIssueConfigValidator = new JiraIssueConfigValidator(projectService, userSearchService, issueTypeService);

        final ConfigurationFieldModel resolveTransition = ConfigurationFieldModel.create(JiraDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION);
        String resolveTransitionString = "Resolve";
        resolveTransition.setFieldValue(resolveTransitionString);

        ConfigurationFieldModel issueType = ConfigurationFieldModel.create(JiraDescriptor.KEY_ISSUE_TYPE);
        String issueTypeString = "IssueType";
        issueType.setFieldValue(issueTypeString);

        Map<String, ConfigurationFieldModel> fields = new HashMap<>();
        fields.put(resolveTransition.getFieldKey(), resolveTransition);
        fields.put(issueType.getFieldKey(), issueType);

        FieldAccessor fieldAccessor = new FieldAccessor(fields);

        IssueTypeResponseModel issue = Mockito.mock(IssueTypeResponseModel.class);
        Mockito.when(issue.getName()).thenReturn(issueTypeString);
        Mockito.when(issueTypeService.getAllIssueTypes()).thenReturn(List.of(issue));

        try {
            jiraIssueConfigValidator.validate(fieldAccessor);
            fail();
        } catch (AlertFieldException e) {
            assertTrue(e.getFieldErrors().containsKey(JiraDescriptor.KEY_JIRA_PROJECT_NAME));
            assertTrue(e.getFieldErrors().containsKey(JiraDescriptor.KEY_ISSUE_CREATOR));
            assertFalse(e.getFieldErrors().containsKey(JiraDescriptor.KEY_ISSUE_TYPE));
        }
    }
}
