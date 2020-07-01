package com.synopsys.integration.azure.boards.common.service.workitem;

import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.util.AzureSpecTemplate;

public class AzureWorkitemService {
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECT_WORKITEMS_INDIVIDUAL = new AzureSpecTemplate("/{organization}/{project}/_apis/wit/workitems/{id}");

    private final AzureHttpService azureHttpService;

    public AzureWorkitemService(AzureHttpService azureHttpService) {
        this.azureHttpService = azureHttpService;
    }

    public WorkItemResponseModel getWorkitem(String organization, String project, Integer workitemId) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_WORKITEMS_INDIVIDUAL
                                 .defineReplacement("{organization}", organization)
                                 .defineReplacement("{project}", project)
                                 .defineReplacement("{id}", workitemId.toString())
                                 .populateSpec();
        return azureHttpService.get(requestSpec, WorkItemResponseModel.class);
    }

}
