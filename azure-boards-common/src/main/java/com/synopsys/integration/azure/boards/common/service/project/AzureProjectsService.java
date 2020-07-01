package com.synopsys.integration.azure.boards.common.service.project;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayWithCountResponseModel;
import com.synopsys.integration.azure.boards.common.model.AzureSpecTemplate;

public class AzureProjectsService {
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECTS = new AzureSpecTemplate("/{organization}/_apis/projects");
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECTS_INDIVIDUAL = new AzureSpecTemplate("/{organization}/_apis/projects/{projectId}");

    private final AzureHttpService azureHttpService;

    public AzureProjectsService(AzureHttpService azureHttpService) {
        this.azureHttpService = azureHttpService;
    }

    public AzureArrayWithCountResponseModel<TeamProjectReferenceResponseModel> getProjects(String organizationName) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECTS
                                 .defineReplacement("{organization}", organizationName)
                                 .populateSpec();
        Type responseType = new TypeToken<AzureArrayWithCountResponseModel<TeamProjectReferenceResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public TeamProjectResponseModel getProject(String organizationName, String projectId) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECTS_INDIVIDUAL
                                 .defineReplacement("{organization}", organizationName)
                                 .defineReplacement("{projectId}", projectId)
                                 .populateSpec();
        Type responseType = new TypeToken<TeamProjectResponseModel>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

}
