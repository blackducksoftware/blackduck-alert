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
package com.synopsys.integration.azure.boards.common.service.board.columns;

import java.lang.reflect.Type;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.azure.boards.common.http.AzureHttpService;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.model.AzureArrayWithCountResponseModel;
import com.synopsys.integration.azure.boards.common.model.NameModel;
import com.synopsys.integration.azure.boards.common.util.AzureSpecTemplate;

public class BoardColumnService {
    public static final AzureSpecTemplate API_SPEC_ORGANIZATION_PROJECT_BOARDCOLUMNS = new AzureSpecTemplate("/{organization}/{project}/_apis/work/boardcolumns");

    private final AzureHttpService azureHttpService;

    public BoardColumnService(AzureHttpService azureHttpService) {
        this.azureHttpService = azureHttpService;
    }

    public AzureArrayWithCountResponseModel<NameModel> getColumns(String organizationName, String projectIdOrName) throws HttpServiceException {
        String requestSpec = API_SPEC_ORGANIZATION_PROJECT_BOARDCOLUMNS
                                 .defineReplacement("{organization}", organizationName)
                                 .defineReplacement("{project}", projectIdOrName)
                                 .populateSpec();
        Type responseType = new TypeToken<AzureArrayWithCountResponseModel<NameModel>>() {}.getType();
        return azureHttpService.get(requestSpec, responseType);
    }

}
