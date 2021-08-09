package com.synopsys.integration.alert.api.channel.issue.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.channel.issue.model.IssuePolicyDetails;
import com.synopsys.integration.alert.api.channel.issue.model.IssueVulnerabilityDetails;
import com.synopsys.integration.alert.api.channel.issue.model.ProjectIssueModel;
import com.synopsys.integration.alert.api.channel.issue.search.enumeration.IssueCategory;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernType;

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
