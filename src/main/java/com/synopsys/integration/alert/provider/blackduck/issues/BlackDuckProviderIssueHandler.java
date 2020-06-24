package com.synopsys.integration.alert.provider.blackduck.issues;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import com.synopsys.integration.blackduck.api.manual.component.ResourceMetadata;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.view.IssueView;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.model.RequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.request.Request;

public class BlackDuckProviderIssueHandler {
    public static final String ISSUE_ENDPOINT_MEDIA_TYPE_V6 = "application/vnd.blackducksoftware.bill-of-materials-6+json";
    private final BlackDuckService blackDuckService;

    public BlackDuckProviderIssueHandler(BlackDuckService blackDuckService) {
        this.blackDuckService = blackDuckService;
    }

    public void createOrUpdateBlackDuckIssue(String bomComponentVersionIssuesUrl, BlackDuckProviderIssueModel issueModel) throws IntegrationException {
        Optional<IssueView> optionalExistingIssue = retrieveExistingIssue(bomComponentVersionIssuesUrl, issueModel.getKey());

        Date currentDate = Date.from(Instant.now());
        IssueView issueRequestModel = createIssueRequestModel(issueModel);
        if (optionalExistingIssue.isPresent()) {
            issueRequestModel.setIssueCreatedAt(optionalExistingIssue.get().getIssueCreatedAt());
            issueRequestModel.setIssueUpdatedAt(currentDate);
            putIssue(bomComponentVersionIssuesUrl, issueRequestModel);
        } else {
            issueRequestModel.setIssueCreatedAt(currentDate);
            issueRequestModel.setIssueUpdatedAt(null);
            postIssue(bomComponentVersionIssuesUrl, issueRequestModel);
        }
    }

    // TODO fix this logic once the bomComponentVersionIssuesUrl supports GET requests directly
    private Optional<IssueView> retrieveExistingIssue(String bomComponentVersionIssuesUrl, String issueKey) throws IntegrationException {
        String issueLookupUrl = createIssueLookupUrl(bomComponentVersionIssuesUrl);
        Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(issueLookupUrl)
                                             .addAdditionalHeader("Accept", ISSUE_ENDPOINT_MEDIA_TYPE_V6);

        // This is really a List<BomComponentIssueView>, but BomComponentIssueView is not considered a BlackDuckResponse.
        List<IssueView> bomComponentIssues = blackDuckService.getAllResponses(requestBuilder, IssueView.class);
        return bomComponentIssues
                   .stream()
                   .filter(issue -> issue.getIssueId().equals(issueKey))
                   .findAny();
    }

    private void postIssue(String uri, IssueView requestModel) throws IntegrationException {
        blackDuckService.post(uri, requestModel);
    }

    private void putIssue(String uri, IssueView requestModel) throws IntegrationException {
        ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setHref(uri);
        requestModel.setMeta(resourceMetadata);

        blackDuckService.put(requestModel);
    }

    private IssueView createIssueRequestModel(BlackDuckProviderIssueModel issueModel) {
        IssueView blackDuckIssueView = new IssueView();
        blackDuckIssueView.setIssueId(issueModel.getKey());
        blackDuckIssueView.setIssueLink(issueModel.getLink());
        blackDuckIssueView.setIssueAssignee(issueModel.getAssignee());
        blackDuckIssueView.setIssueStatus(issueModel.getStatus());
        blackDuckIssueView.setIssueDescription(issueModel.getSummary());
        return blackDuckIssueView;
    }

    /**
     * @param bomComponentVersionIssuesUrl a string in the format of<br/>
     *                                     {blackDuckUrl}/api/projects/{projectId}/versions/{projectVersionId}/components/{componentId}/issues<br/>
     *                                     or<br/>
     *                                     {blackDuckUrl}/api/projects/{projectId}/versions/{projectVersionId}/components/{componentId}/component-versions/{componentVersionId}/issues
     * @return a string in the format of {blackDuckUrl}/api/projects/{projectId}/versions/{projectVersionId}/issues
     */
    private String createIssueLookupUrl(String bomComponentVersionIssuesUrl) {
        String projectVersionUrl = StringUtils.substringBefore(bomComponentVersionIssuesUrl, "/components");
        return projectVersionUrl + "/issues";
    }

}
