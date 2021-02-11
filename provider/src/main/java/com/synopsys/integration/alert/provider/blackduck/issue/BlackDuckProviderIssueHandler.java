/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.provider.blackduck.issue;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionIssuesView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.IssueRequest;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.body.StringBodyContent;
import com.synopsys.integration.rest.request.Request;

public class BlackDuckProviderIssueHandler {
    private final Gson gson;
    private final BlackDuckApiClient blackDuckApiClient;
    private final ProjectService projectService;

    public BlackDuckProviderIssueHandler(Gson gson, BlackDuckApiClient blackDuckApiClient, ProjectService projectService) {
        this.gson = gson;
        this.blackDuckApiClient = blackDuckApiClient;
        this.projectService = projectService;
    }

    public void createOrUpdateBlackDuckIssue(String bomComponentVersionIssuesUrl, BlackDuckProviderIssueModel issueModel, String projectName, String projectVersionName) throws IntegrationException {
        Optional<ProjectVersionIssuesView> optionalExistingIssue = retrieveExistingIssue(projectName, projectVersionName, issueModel.getKey());

        Date currentDate = Date.from(Instant.now());
        HttpUrl requestUri = new HttpUrl(bomComponentVersionIssuesUrl);
        IssueRequest issueRequestModel = createIssueRequestModel(issueModel);

        HttpMethod httpMethod = HttpMethod.POST;
        if (optionalExistingIssue.isPresent()) {
            ProjectVersionIssuesView existingIssue = optionalExistingIssue.get();
            issueRequestModel.setIssueDescription(existingIssue.getIssueDescription());
            issueRequestModel.setIssueCreatedAt(existingIssue.getIssueCreatedAt());
            issueRequestModel.setIssueUpdatedAt(currentDate);

            // The request uri should point at the specific issue for PUT requests
            httpMethod = HttpMethod.PUT;
            requestUri = existingIssue.getHref();
        } else {
            issueRequestModel.setIssueCreatedAt(currentDate);
            issueRequestModel.setIssueUpdatedAt(null);
        }
        performRequest(requestUri, httpMethod, issueRequestModel);
    }

    private Optional<ProjectVersionIssuesView> retrieveExistingIssue(String projectName, String projectVersionName, String issueKey) throws IntegrationException {
        Optional<ProjectVersionWrapper> projectVersionWrapperOptional = projectService.getProjectVersion(projectName, projectVersionName);
        if (projectVersionWrapperOptional.isPresent()) {
            ProjectVersionWrapper projectVersionWrapper = projectVersionWrapperOptional.get();
            List<ProjectVersionIssuesView> bomComponentIssues = blackDuckApiClient.getAllResponses(projectVersionWrapper.getProjectVersionView(), ProjectVersionView.ISSUES_LINK_RESPONSE);
            return bomComponentIssues
                       .stream()
                       .filter(issue -> issue.getIssueId().equals(issueKey))
                       .findAny();
        }
        return Optional.empty();
    }

    private void performRequest(HttpUrl httpUrl, HttpMethod httpMethod, IssueRequest issueRequest) throws IntegrationException {
        Request request = new BlackDuckRequestBuilder(new Request.Builder())
                              .url(httpUrl)
                              .method(httpMethod)
                              .bodyContent(new StringBodyContent(gson.toJson(issueRequest)))
                              .build();
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
