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

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;

import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public abstract class CustomEndpoint<R> {

    public CustomEndpoint(String fieldKey, CustomEndpointManager customEndpointManager) throws AlertException {
        customEndpointManager.registerFunction(fieldKey, this::createResponse);
    }

    protected Optional<ResponseEntity<String>> preprocessRequest(Map<String, FieldValueModel> fieldValueModels) {
        return Optional.empty();
    }

    protected abstract R createData(Map<String, FieldValueModel> fieldValueModels) throws AlertException;

    protected abstract ResponseEntity<String> createErrorResponse(Exception e);

    protected abstract ResponseEntity<String> createSuccessResponse(R response);

    public final ResponseEntity<String> createResponse(Map<String, FieldValueModel> fieldValueModels) {
        Optional<ResponseEntity<String>> processedFieldValueModels = preprocessRequest(fieldValueModels);
        if (processedFieldValueModels.isPresent()) {
            return processedFieldValueModels.get();
        }

        try {
            R response = createData(fieldValueModels);
            return createSuccessResponse(response);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
}
