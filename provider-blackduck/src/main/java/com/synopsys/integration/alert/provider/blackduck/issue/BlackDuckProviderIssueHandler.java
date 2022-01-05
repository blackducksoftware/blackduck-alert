/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.issue;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionIssuesView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.IssueRequest;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.dataservice.IssueService;
import com.synopsys.integration.blackduck.service.request.BlackDuckResponseRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.body.BodyContentConverter;
import com.synopsys.integration.rest.body.StringBodyContent;

public class BlackDuckProviderIssueHandler {
    private final Gson gson;
    private final BlackDuckApiClient blackDuckApiClient;
    private final IssueService issueService;

    public BlackDuckProviderIssueHandler(Gson gson, BlackDuckApiClient blackDuckApiClient, IssueService issueService) {
        this.gson = gson;
        this.blackDuckApiClient = blackDuckApiClient;
        this.issueService = issueService;
    }

    public void createOrUpdateBlackDuckIssue(BlackDuckProviderIssueModel issueModel, @Nullable String bomComponentVersionIssuesUrl, String projectVersionUrl) throws IntegrationException {
        Optional<ProjectVersionIssuesView> optionalExistingIssue = retrieveExistingIssue(projectVersionUrl, issueModel.getKey());

        Date currentDate = Date.from(Instant.now());
        IssueRequest issueRequestModel = createIssueRequestModel(issueModel);

        if (optionalExistingIssue.isPresent()) {
            ProjectVersionIssuesView existingIssue = optionalExistingIssue.get();
            issueRequestModel.setIssueDescription(existingIssue.getIssueDescription());
            issueRequestModel.setIssueCreatedAt(existingIssue.getIssueCreatedAt());
            issueRequestModel.setIssueUpdatedAt(currentDate);

            // The request uri should point at the specific issue for PUT requests
            HttpUrl requestUri = existingIssue.getHref();
            performRequest(requestUri, HttpMethod.PUT, issueRequestModel);
        } else if (null != bomComponentVersionIssuesUrl) {
            issueRequestModel.setIssueCreatedAt(currentDate);
            issueRequestModel.setIssueUpdatedAt(null);

            HttpUrl requestUri = new HttpUrl(bomComponentVersionIssuesUrl);
            performRequest(requestUri, HttpMethod.POST, issueRequestModel);
        }
    }

    private Optional<ProjectVersionIssuesView> retrieveExistingIssue(String projectVersionUrl, String blackDuckIssueId) throws IntegrationException {
        HttpUrl projectVersionHttpUrl = new HttpUrl(projectVersionUrl);
        ProjectVersionView projectVersion = blackDuckApiClient.getResponse(projectVersionHttpUrl, ProjectVersionView.class);
        List<ProjectVersionIssuesView> bomComponentIssues = issueService.getIssuesForProjectVersion(projectVersion);
        return bomComponentIssues
                   .stream()
                   .filter(issue -> issue.getIssueId().equals(blackDuckIssueId))
                   .findAny();
    }

    private void performRequest(HttpUrl httpUrl, HttpMethod httpMethod, IssueRequest issueRequest) throws IntegrationException {
        BlackDuckResponseRequest request = new BlackDuckRequestBuilder()
                              .method(httpMethod)
                              .bodyContent(new StringBodyContent(gson.toJson(issueRequest), BodyContentConverter.DEFAULT))
                              .buildBlackDuckResponseRequest(httpUrl);
        blackDuckApiClient.execute(request);
    }

    private IssueRequest createIssueRequestModel(BlackDuckProviderIssueModel issueModel) {
        IssueRequest blackDuckIssueRequest = new IssueRequest();
        blackDuckIssueRequest.setIssueId(issueModel.getKey());
        blackDuckIssueRequest.setIssueLink(issueModel.getLink());
        blackDuckIssueRequest.setIssueAssignee(issueModel.getAssignee());
        blackDuckIssueRequest.setIssueStatus(issueModel.getStatus());
        blackDuckIssueRequest.setIssueDescription(issueModel.getSummary());
        return blackDuckIssueRequest;
    }

}
