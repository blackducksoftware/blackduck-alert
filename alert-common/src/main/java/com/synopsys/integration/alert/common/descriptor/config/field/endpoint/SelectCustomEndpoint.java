/**
 * alert-common
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
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.ResponseFactory;

public abstract class SelectCustomEndpoint extends CustomEndpoint<List<LabelValueSelectOption>> {
    private ResponseFactory responseFactory;
    private Gson gson;

    public SelectCustomEndpoint(String fieldKey, CustomEndpointManager customEndpointManager, ResponseFactory responseFactory, Gson gson) throws AlertException {
        super(fieldKey, customEndpointManager);
        this.responseFactory = responseFactory;
        this.gson = gson;
    }

    @Override
    protected ResponseEntity<String> createErrorResponse(Exception e) {
        return responseFactory.createInternalServerErrorResponse("", String.format("An internal issue occurred while trying to retrieve your select data: %s", e.getMessage()));
    }

    @Override
    protected ResponseEntity<String> createSuccessResponse(List<LabelValueSelectOption> response) {
        String providerOptionsConverted = gson.toJson(response);
        return responseFactory.createOkContentResponse(providerOptionsConverted);
    }

}
