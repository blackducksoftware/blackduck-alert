/**
 * provider
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.issues;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.manual.throwaway.generated.view.IssueView;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;

public class BlackDuckProviderIssueHandler {
    public static final String ISSUE_ENDPOINT_MEDIA_TYPE_V6 = "application/vnd.blackducksoftware.bill-of-materials-6+json";
    private final Gson gson;
    private final BlackDuckService blackDuckService;
    private final RequestFactory requestFactory;

    public BlackDuckProviderIssueHandler(Gson gson, BlackDuckService blackDuckService, RequestFactory requestFactory) {
        this.gson = gson;
        this.blackDuckService = blackDuckService;
        this.requestFactory = requestFactory;
    }

    public void createOrUpdateBlackDuckIssue(String bomComponentVersionIssuesUrl, BlackDuckProviderIssueModel issueModel) throws IntegrationException {
        Optional<IssueView> optionalExistingIssue = retrieveExistingIssue(bomComponentVersionIssuesUrl, issueModel.getKey());

        Date currentDate = Date.from(Instant.now());
        HttpUrl requestUri = new HttpUrl(bomComponentVersionIssuesUrl);
        IssueView issueRequestModel = createIssueRequestModel(issueModel);

        BiFunction<HttpUrl, String, BlackDuckRequestBuilder> requestBuilderCreator;
        if (optionalExistingIssue.isPresent()) {
            IssueView existingIssue = optionalExistingIssue.get();
            issueRequestModel.setIssueDescription(existingIssue.getIssueDescription());
            issueRequestModel.setIssueCreatedAt(existingIssue.getIssueCreatedAt());
            issueRequestModel.setIssueUpdatedAt(currentDate);

            // The request uri should point at the specific issue for PUT requests
            requestUri = existingIssue.getMeta().getHref();
            requestBuilderCreator = requestFactory::createCommonPutRequestBuilder;
        } else {
            issueRequestModel.setIssueCreatedAt(currentDate);
            issueRequestModel.setIssueUpdatedAt(null);
            requestBuilderCreator = requestFactory::createCommonPostRequestBuilder;
        }
        performRequest(requestUri, issueRequestModel, requestBuilderCreator);
    }

    // TODO fix this logic once the bomComponentVersionIssuesUrl supports GET requests directly
    private Optional<IssueView> retrieveExistingIssue(String bomComponentVersionIssuesUrl, String issueKey) throws IntegrationException {
        String issueLookupUrl = createIssueLookupUrl(bomComponentVersionIssuesUrl);
        HttpUrl issueLookupHttpUrl = new HttpUrl(issueLookupUrl);
        BlackDuckRequestBuilder requestBuilder = requestFactory.createCommonGetRequestBuilder(issueLookupHttpUrl)
                                                     .addHeader(HttpHeaders.ACCEPT, ISSUE_ENDPOINT_MEDIA_TYPE_V6);

        // This is really a List<BomComponentIssueView>, but BomComponentIssueView is not considered a BlackDuckResponse.
        List<IssueView> bomComponentIssues = blackDuckService.getAllResponses(requestBuilder, IssueView.class);
        return bomComponentIssues
                   .stream()
                   .filter(issue -> issue.getIssueId().equals(issueKey))
                   .findAny();
    }

    private void performRequest(HttpUrl httpUrl, IssueView requestModel, BiFunction<HttpUrl, String, BlackDuckRequestBuilder> requestBuilderCreator) throws IntegrationException {
        String requestJson = gson.toJson(requestModel);
        Request request = requestBuilderCreator.apply(httpUrl, requestJson)
                              .addHeader(HttpHeaders.CONTENT_TYPE, ISSUE_ENDPOINT_MEDIA_TYPE_V6)
                              .addHeader(HttpHeaders.ACCEPT, ISSUE_ENDPOINT_MEDIA_TYPE_V6)
                              .build();
        blackDuckService.execute(request);
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
