package com.blackduck.integration.alert.api.channel.issue.tracker.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssuePolicyDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueVulnerabilityDetails;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.ProjectIssueModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.enumeration.IssueCategory;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernType;

public class IssueCategoryRetrieverTest {
    @Test
    public void retrieveIssueCategoryFromProjectIssueModel_PolicyTest() {
        IssuePolicyDetails issuePolicyDetails = Mockito.mock(IssuePolicyDetails.class);
        ProjectIssueModel projectIssueModel = Mockito.mock(ProjectIssueModel.class);
        Mockito.when(projectIssueModel.getPolicyDetails()).thenReturn(Optional.of(issuePolicyDetails));
        Mockito.when(projectIssueModel.getVulnerabilityDetails()).thenReturn(Optional.empty());

        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        IssueCategory issueCategory = issueCategoryRetriever.retrieveIssueCategoryFromProjectIssueModel(projectIssueModel);
        assertEquals(IssueCategory.POLICY, issueCategory);
    }

    @Test
    public void retrieveIssueCategoryFromProjectIssueModel_VulnerabilityTest() {
        IssueVulnerabilityDetails issueVulnerabilityDetails = Mockito.mock(IssueVulnerabilityDetails.class);
        ProjectIssueModel projectIssueModel = Mockito.mock(ProjectIssueModel.class);
        Mockito.when(projectIssueModel.getPolicyDetails()).thenReturn(Optional.empty());
        Mockito.when(projectIssueModel.getVulnerabilityDetails()).thenReturn(Optional.of(issueVulnerabilityDetails));

        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        IssueCategory issueCategory = issueCategoryRetriever.retrieveIssueCategoryFromProjectIssueModel(projectIssueModel);
        assertEquals(IssueCategory.VULNERABILITY, issueCategory);
    }

    @Test
    public void retrieveIssueCategoryFromComponentConcernType_PolicyTest() {
        assertIssueCategoryMatchesConcernType(ComponentConcernType.POLICY);
    }

    @Test
    public void retrieveIssueCategoryFromComponentConcernType_VulnerabilityTest() {
        assertIssueCategoryMatchesConcernType(ComponentConcernType.VULNERABILITY);
    }

    private static void assertIssueCategoryMatchesConcernType(ComponentConcernType componentConcernType) {
        IssueCategoryRetriever issueCategoryRetriever = new IssueCategoryRetriever();
        IssueCategory issueCategory = issueCategoryRetriever.retrieveIssueCategoryFromComponentConcernType(componentConcernType);
        assertEquals(componentConcernType.name(), issueCategory.name());
    }

}
