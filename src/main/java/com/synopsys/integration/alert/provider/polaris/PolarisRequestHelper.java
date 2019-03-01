/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.provider.polaris;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.gson.Gson;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.BranchV0;
import com.synopsys.integration.polaris.common.api.BranchV0Resources;
import com.synopsys.integration.polaris.common.api.ProjectV0;
import com.synopsys.integration.polaris.common.api.ProjectV0Resources;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class PolarisRequestHelper {
    public static final String API_MIME_TYPE = "application/vnd.api+json";
    public static final String PAGE_OFFSET_PARAM_NAME = "page[offset]";
    public static final String PAGE_LIMIT_PARAM_NAME = "page[limit]";
    public static final String FILTER_BRANCH_PROJECT_ID_PARAM_NAME = "filter[branch][project][id][$eq]";

    public static final String PROJECT_ID_PARAM_NAME = "project-id";
    public static final String BRANCH_ID_PARAM_NAME = "branch-id";

    public static final String COMMON_API_SPEC = "/api/common/v0";
    public static final String PROJECT_API_SPEC = COMMON_API_SPEC + "/projects";
    public static final String BRANCHES_API_SPEC = COMMON_API_SPEC + "/branches";

    public static final String QUERY_API_SPEC = "/api/query/v0";
    public static final String ISSUES_API_SPEC = QUERY_API_SPEC + "/issues";

    private final AccessTokenPolarisHttpClient polarisHttpClient;
    private final String baseUrl;
    private final Gson gson;

    public PolarisRequestHelper(final AccessTokenPolarisHttpClient polarisHttpClient, final String baseUrl, final Gson gson) {
        this.polarisHttpClient = polarisHttpClient;
        this.baseUrl = baseUrl;
        this.gson = gson;
    }

    public List<ProjectV0> getAllProjects() throws IntegrationException {
        return handlePagedRequest(ProjectV0Resources.class, this::createProjectGetRequest, ProjectV0Resources::getData);
    }

    public Request createProjectGetRequest(final Integer pageOffset, final Integer pageLimit) {
        final String uri = baseUrl + PROJECT_API_SPEC;
        return new Request.Builder()
                   .uri(uri)
                   .method(HttpMethod.GET)
                   .mimeType(API_MIME_TYPE)
                   .addQueryParameter(PAGE_OFFSET_PARAM_NAME, pageOffset.toString())
                   .addQueryParameter(PAGE_LIMIT_PARAM_NAME, pageLimit.toString())
                   .build();
    }

    public List<BranchV0> getBranchesForProject(final String projectId) throws IntegrationException {
        return handlePagedRequest(BranchV0Resources.class, (offset, limit) -> createBranchesGetRequest(offset, limit, projectId), BranchV0Resources::getData);
    }

    public Request createBranchesGetRequest(final Integer pageOffset, final Integer pageLimit, final String projectId) {
        final String uri = baseUrl + BRANCHES_API_SPEC;
        return new Request.Builder()
                   .uri(uri)
                   .method(HttpMethod.GET)
                   .mimeType(API_MIME_TYPE)
                   .addQueryParameter(PAGE_OFFSET_PARAM_NAME, pageOffset.toString())
                   .addQueryParameter(PAGE_LIMIT_PARAM_NAME, pageLimit.toString())
                   .addQueryParameter(FILTER_BRANCH_PROJECT_ID_PARAM_NAME, projectId)
                   .build();
    }

    // FIXME create a model for issues
    //    public List<IssueV0> getIssuesForProjectBranch(final String projectId, final String branchId) throws IntegrationException {
    //        return handlePagedRequest(IssueV0Resources.class, (offset, limit) -> createIssuesGetRequest(offset, limit, projectId, branchId), IssueV0::getData);
    //    }

    public Request createIssuesGetRequest(final Integer pageOffset, final Integer pageLimit, final String projectId, final String branchId) {
        final String uri = baseUrl + ISSUES_API_SPEC;
        return new Request.Builder()
                   .uri(uri)
                   .method(HttpMethod.GET)
                   .mimeType(API_MIME_TYPE)
                   .addQueryParameter(PAGE_OFFSET_PARAM_NAME, pageOffset.toString())
                   .addQueryParameter(PAGE_LIMIT_PARAM_NAME, pageLimit.toString())
                   .addQueryParameter(PROJECT_ID_PARAM_NAME, projectId)
                   .addQueryParameter(BRANCH_ID_PARAM_NAME, branchId)
                   .build();
    }

    private <R, W> List<R> handlePagedRequest(final Class<W> wrapperClass, final BiFunction<Integer, Integer, Request> createPagedRequest, final Function<W, List<R>> getResponseList) throws IntegrationException {
        final List<R> allResults = new ArrayList<>();
        List<R> chunkedResults;

        int offset = 0;
        final Integer defaultPageLimit = 25;
        do {
            final Request pagedRequest = createPagedRequest.apply(offset, defaultPageLimit);
            try (final Response response = polarisHttpClient.execute(pagedRequest)) {
                response.throwExceptionForError();
                final W projectsWrapper = gson.fromJson(response.getContentString(), wrapperClass);

                if (projectsWrapper != null) {
                    final List<R> data = getResponseList.apply(projectsWrapper);
                    if (data != null) {
                        chunkedResults = data;
                        allResults.addAll(chunkedResults);
                        offset++;
                        continue;
                    }
                }
                break;
            } catch (final IOException e) {
                throw new IntegrationException("Problem handling request", e);
            }
        } while (chunkedResults.size() == defaultPageLimit);
        return allResults;
    }

}
