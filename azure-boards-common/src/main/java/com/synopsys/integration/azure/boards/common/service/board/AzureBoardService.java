/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.board;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;

/**
 * <a href="https://docs.microsoft.com/en-us/rest/api/azure/devops/work/boards?view=azure-devops-rest-5.1">Documentation</a>
 */
public class AzureBoardService {
    private final AzureHttpService azureHttpService;

    public AzureBoardService(AzureHttpService azureHttpService) {
        this.azureHttpService = azureHttpService;
    }

    public AzureArrayResponseModel<BoardReferenceResponseModel> getBoards(String organizationName, String projectIdOrName, String teamIdOrName) throws HttpServiceException {
        String requestSpec = String.format("/%s/%s/%s/_apis/work/boards", organizationName, projectIdOrName, teamIdOrName);
        Type responseType = new TypeToken<AzureArrayResponseModel<BoardReferenceResponseModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

    public BoardResponseModel getBoard(String organizationName, String projectIdOrName, String teamIdOrName, String boardIdOrBacklogLevel) throws HttpServiceException {
        String requestSpec = String.format("/%s/%s/%s/_apis/work/boards/%s", organizationName, projectIdOrName, teamIdOrName, boardIdOrBacklogLevel);
        return azureHttpService.get(requestSpec, BoardResponseModel.class);
    }

}
