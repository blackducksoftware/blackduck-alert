/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.azure.boards.common.service.board.columns;

import java.lang.reflect.Type;

import com.blackduck.integration.alert.azure.boards.common.http.AzureHttpService;
import com.blackduck.integration.alert.azure.boards.common.http.HttpServiceException;
import com.blackduck.integration.alert.azure.boards.common.model.AzureArrayResponseModel;
import com.blackduck.integration.alert.azure.boards.common.model.NameModel;
import com.google.gson.reflect.TypeToken;

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
