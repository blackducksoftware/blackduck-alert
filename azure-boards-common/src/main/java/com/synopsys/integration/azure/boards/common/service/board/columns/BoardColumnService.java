/*
 * azure-boards-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.azure.boards.common.service.board.columns;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayResponseModel;
import com.synopsys.integration.azure.boards.common.model.NameModel;

public class BoardColumnService {
    private final AzureHttpService azureHttpService;

    public BoardColumnService(AzureHttpService azureHttpService) {
        this.azureHttpService = azureHttpService;
    }

    public AzureArrayResponseModel<NameModel> getBoardColumns(String organizationName, String projectIdOrName) throws HttpServiceException {
        String requestSpec = String.format("/%s/%s/_apis/work/boardcolumns", organizationName, projectIdOrName);
        Type responseType = new TypeToken<AzureArrayResponseModel<NameModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

}
