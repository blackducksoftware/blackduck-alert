/**
 * azure-boards-common
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
package com.synopsys.integration.azure.boards.common.service.project;

import java.io.IOException;
import java.lang.reflect.Type;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.util.AzureSpecTemplate;

/**
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/core/projects?view=azure-devops-rest-5.1">Projects</a>
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/core/projects/get%20project%20properties?view=azure-devops-rest-5.1">Project Properties</a>
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/wit/fields/create?view=azure-devops-rest-5.1">Project Fields</a>
 */
public class AzureProjectService {
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECTS = new AzureSpecTemplate("/{organization}/_apis/projects");
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECTS_INDIVIDUAL = new AzureSpecTemplate("/{organization}/_apis/projects/{projectId}");
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECT_PROPERTIES = new AzureSpecTemplate("/{organization}/_apis/projects/{projectId}/properties");
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECT_FIELDS = new AzureSpecTemplate("/{organization}/{project}/_apis/wit/fields");

    public static final String PATH_ORGANIZATION_REPLACEMENT = "{organization}";
    public static final String PATH_PROJECT_ID_REPLACEMENT = "{projectId}";

    public static final String PROPERTIES_ENDPOINT_API_VERSION = "5.1-preview.1";

    private final AzureHttpService azureHttpService;

    public AzureProjectService(AzureHttpService azureHttpService) {
        this.azureHttpService = azureHttpService;
    }

    public AzureArrayResponseModel<TeamProjectReferenceResponseModel> getProjects(String organizationName) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECTS
                                 .defineReplacement(PATH_ORGANIZATION_REPLACEMENT, organizationName)
                                 .populateSpec();
        Type responseType = new TypeToken<AzureArrayResponseModel<TeamProjectReferenceResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public TeamProjectResponseModel getProject(String organizationName, String projectId) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECTS_INDIVIDUAL
                                 .defineReplacement(PATH_ORGANIZATION_REPLACEMENT, organizationName)
                                 .defineReplacement(PATH_PROJECT_ID_REPLACEMENT, projectId)
                                 .populateSpec();
        Type responseType = new TypeToken<TeamProjectResponseModel>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public AzureArrayResponseModel<ProjectPropertyResponseModel> getProjectProperties(String organizationName, String projectId) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_PROPERTIES
                                 .defineReplacement(PATH_ORGANIZATION_REPLACEMENT, organizationName)
                                 .defineReplacement(PATH_PROJECT_ID_REPLACEMENT, projectId)
                                 .populateSpec();
        requestSpec = String.format("%s?%s=%s", requestSpec, AzureHttpService.AZURE_API_VERSION_QUERY_PARAM_NAME, PROPERTIES_ENDPOINT_API_VERSION);
        Type responseType = new TypeToken<AzureArrayResponseModel<ProjectPropertyResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public ProjectWorkItemFieldResponseModel createProjectField(String organizationName, String projectNameOrId, JsonObject requestModel) throws IOException, HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_FIELDS
                                 .defineReplacement(PATH_ORGANIZATION_REPLACEMENT, organizationName)
                                 .defineReplacement("{project}", projectNameOrId)
                                 .populateSpec();
        return azureHttpService.post(requestSpec, requestModel, ProjectWorkItemFieldResponseModel.class);
    }

}
