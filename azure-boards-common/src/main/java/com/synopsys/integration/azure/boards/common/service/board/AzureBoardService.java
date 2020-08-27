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
